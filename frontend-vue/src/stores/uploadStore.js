import { defineStore } from 'pinia'
import * as XLSX from 'xlsx'
import { enviarPlanilha, validarClientes } from '../services/api'

/**
 * Store da tela de upload.
 *
 * Fluxo: o navegador lê a planilha, padroniza e separa as linhas em válidas e
 * inválidas (com o motivo). No envio, só as válidas seguem: primeiro para
 * /api/clientes/validar, onde o Spring Boot valida de novo; depois, a lista
 * aprovada pelo Java vira uma planilha nova que vai para a análise em Python.
 * Assim, uma linha recusada na tela nunca aparece no dashboard.
 */

const EXTENSOES = ['.xlsx', '.xls', '.csv']
const TAMANHO_MAXIMO = 10 * 1024 * 1024
const LINHAS_NA_PREVIA = 20

/**
 * Cabecalho da planilha -> campo usado na previa.
 * Aceita os dois modelos: o da aula (codigo_cliente) e o da CTI (Cliente CTI).
 * A chave e o cabecalho ja normalizado por normalizarCabecalho().
 */
const COLUNAS = {
  codigo_cliente: 'codigo',
  cliente_cti: 'codigo',
  cliente_cti_codigo_interno: 'codigo',
  nome_cliente: 'nome',
  consultor: 'consultor',
  consultor_responsavel: 'consultor',
  segmento: 'segmento',
  segmento_de_atuacao: 'segmento',
  nivel_cliente: 'nivel',
  nivel_do_cliente: 'nivel',
  faturamento_anual: 'faturamento',
  faixa_de_faturamento: 'faturamento',
  faixa_de_faturamento_anual: 'faturamento',
  servicos_contratados: 'servicos',
  data_contratacao: 'dataContratacao',
  data_inicio: 'dataContratacao',
  cidade: 'cidade',
  uf: 'uf',
}

/** Variacoes de escrita do segmento -> forma unica exibida na previa. */
const SEGMENTOS = {
  IND: 'Indústria',
  INDUSTRIA: 'Indústria',
  INDUSTRIAL: 'Indústria',
  COM: 'Comércio',
  COMERCIO: 'Comércio',
  COMERCIAL: 'Comércio',
  VAREJO: 'Comércio',
  SERV: 'Serviços',
  SERVICO: 'Serviços',
  SERVICOS: 'Serviços',
  GOV: 'Governo',
  GOVERNO: 'Governo',
  EDU: 'Educação',
  EDUCACAO: 'Educação',
  SAUDE: 'Saúde',
  AGRO: 'Agronegócio',
  AGRONEGOCIO: 'Agronegócio',
  TEC: 'Tecnologia',
  TECNOLOGIA: 'Tecnologia',
}

/** Campos que a prévia compara com a planilha original para listar os ajustes. */
const CAMPOS_PADRONIZADOS = [
  { campo: 'segmento', rotulo: 'Segmento' },
  { campo: 'consultor', rotulo: 'Consultor' },
  { campo: 'nivel', rotulo: 'Nível' },
]

/** Tira acento, pontuacao e espaco para comparar textos escritos de formas diferentes. */
function normalizarCabecalho(texto) {
  return String(texto ?? '')
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '_')
    .replace(/^_+|_+$/g, '')
}

/** Mesma normalizacao, mas em caixa alta: usada para casar o segmento no mapa. */
function chaveDeSegmento(valor) {
  return normalizarCabecalho(valor).replace(/_/g, ' ').trim().toUpperCase()
}

/** "ANA SOUZA" e "ana souza" viram "Ana Souza": o mesmo consultor conta uma vez so. */
function padronizarNome(valor) {
  return String(valor ?? '')
    .trim()
    .replace(/\s+/g, ' ')
    .toLowerCase()
    .replace(/(^|\s)\p{L}/gu, (letra) => letra.toUpperCase())
}

/** 1850000 continua número; "1850000,50" vira 1850000.5; o que não for número vira null. */
function converterFaturamento(valor) {
  if (typeof valor === 'number') return valor
  const texto = String(valor ?? '').trim().replace(',', '.')
  if (texto === '') return null
  const numero = Number(texto)
  return Number.isNaN(numero) ? null : numero
}

const doisDigitos = (n) => String(n).padStart(2, '0')

/**
 * Data no formato que o Java entende (LocalDate): "2025-01-15".
 * O Excel guarda data como número de dias (45672); a planilha digitada à mão
 * pode trazer "15/01/2025". Data que não existe (31/02) volta vazia.
 */
function converterData(valor) {
  let ano, mes, dia
  const texto = String(valor ?? '').trim()
  const brasileira = texto.match(/^(\d{1,2})\/(\d{1,2})\/(\d{4})$/)
  const iso = texto.match(/^(\d{4})-(\d{2})-(\d{2})$/)

  if (typeof valor === 'number') {
    const data = XLSX.SSF.parse_date_code(valor)
    if (!data) return ''
    ano = data.y
    mes = data.m
    dia = data.d
  } else if (brasileira) {
    dia = Number(brasileira[1])
    mes = Number(brasileira[2])
    ano = Number(brasileira[3])
  } else if (iso) {
    ano = Number(iso[1])
    mes = Number(iso[2])
    dia = Number(iso[3])
  } else {
    return ''
  }
  // Date "corrige" 31/02 para 03/03; se o mês ou o dia mudou, a data não existia.
  const conferida = new Date(Date.UTC(ano, mes - 1, dia))
  if (conferida.getUTCMonth() !== mes - 1 || conferida.getUTCDate() !== dia) return ''
  return `${ano}-${doisDigitos(mes)}-${doisDigitos(dia)}`
}

/** Linha da prévia -> objeto com os nomes de campo do ClienteDTO do Java. */
function paraDto(cliente) {
  return {
    codigoCliente: cliente.codigo,
    nomeCliente: cliente.nome,
    consultor: cliente.consultor,
    segmento: cliente.segmento,
    nivelCliente: cliente.nivel,
    faturamentoAnual: cliente.faturamento,
    servicosContratados: cliente.servicos,
    dataContratacao: cliente.dataContratacao,
    cidade: cliente.cidade,
    uf: cliente.uf,
  }
}

/**
 * Monta um .xlsx com os clientes aprovados pelo Java, nas colunas do modelo da
 * aula. É este arquivo, e não o original, que vai para a análise em Python.
 */
function montarPlanilha(clientes, nomeOriginal) {
  const linhas = clientes.map((c) => ({
    codigo_cliente: c.codigoCliente,
    nome_cliente: c.nomeCliente,
    consultor: c.consultor,
    segmento: c.segmento,
    nivel_cliente: c.nivelCliente,
    faturamento_anual: c.faturamentoAnual,
    servicos_contratados: c.servicosContratados,
    data_contratacao: c.dataContratacao,
    cidade: c.cidade,
    uf: c.uf,
  }))
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, XLSX.utils.json_to_sheet(linhas), 'clientes')
  const bytes = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' })
  const nome = nomeOriginal.replace(/\.(xlsx|xls|csv)$/i, '') + '.xlsx'
  return new File([bytes], nome, {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  })
}

export const useUploadStore = defineStore('upload', {
  state: () => ({
    arquivo: null,
    dadosOriginais: [],
    dadosTratados: [],
    // Resultado da validação: as válidas seguem no envio, as inválidas ficam de fora com o motivo.
    dadosValidos: [],
    dadosInvalidos: [],
    // Problemas do arquivo inteiro (formato, tamanho, aba vazia): impedem qualquer envio.
    erros: [],
    carregando: false,
    enviando: false,
    validando: false,
    progresso: 0,
    resultado: null,
    // Separado de `erros`: aquilo é a prévia no navegador; isto é a API recusando o envio.
    erroEnvio: null,
  }),

  getters: {
    quantidadeLinhas: (state) => state.dadosTratados.length,
    quantidadeValidas: (state) => state.dadosValidos.length,
    quantidadeInvalidas: (state) => state.dadosInvalidos.length,
    percentualValidos: (state) =>
      state.dadosTratados.length === 0
        ? 0
        : Math.round((state.dadosValidos.length / state.dadosTratados.length) * 100),
    /** Como na aula: totalClientes conta só as linhas válidas após o tratamento. */
    totalClientes: (state) => state.dadosValidos.length,
    totalErros: (state) => state.erros.length,
    totalFaturamento: (state) => state.dadosValidos.reduce((soma, c) => soma + c.faturamento, 0),
    clientesNivelA: (state) => state.dadosValidos.filter((c) => c.nivel === 'A').length,
    temDados: (state) => state.dadosTratados.length > 0,
    /** A tabela mostra só o começo: 500 linhas na tela não ajudam a conferir nada. */
    previa: (state) => state.dadosTratados.slice(0, LINHAS_NA_PREVIA),

    /**
     * O que o tratamento corrige sozinho, campo a campo: quantas linhas mudaram
     * e alguns exemplos de "como veio" -> "como fica".
     */
    ajustes: (state) =>
      CAMPOS_PADRONIZADOS.map(({ campo, rotulo }) => {
        const trocas = new Map()
        let linhas = 0
        for (const cliente of state.dadosTratados) {
          const original = String(cliente.original[campo] ?? '')
          // Célula vazia não é ajuste: é problema, e aparece em dadosInvalidos.
          if (!original.trim() || original === cliente[campo]) continue
          linhas += 1
          trocas.set(original, cliente[campo])
        }
        const exemplos = [...trocas].slice(0, 3).map(([de, para]) => `"${de}" → ${para}`)
        return { rotulo, linhas, exemplos }
      }).filter((ajuste) => ajuste.linhas > 0),
  },

  actions: {
    selecionarArquivo(arquivo) {
      this.limpar()
      this.arquivo = arquivo
    },

    /** Recusa no navegador o que a API recusaria depois. */
    validarArquivo() {
      if (!this.arquivo) {
        this.erros.push('Selecione uma planilha.')
        return false
      }

      const nome = this.arquivo.name.toLowerCase()
      if (!EXTENSOES.some((extensao) => nome.endsWith(extensao))) {
        this.erros.push('Formato não suportado. Envie a planilha em .xlsx, .xls ou .csv.')
        return false
      }
      if (this.arquivo.size > TAMANHO_MAXIMO) {
        this.erros.push('O arquivo passa de 10 MB. Exporte apenas a aba de clientes.')
        return false
      }
      if (this.arquivo.size === 0) {
        this.erros.push('O arquivo está vazio.')
        return false
      }
      return true
    },

    /** Le a planilha no navegador, padroniza e valida. Nada sai da maquina aqui. */
    async processarPlanilha() {
      this.erros = []
      if (!this.validarArquivo()) return false

      this.carregando = true
      try {
        const buffer = await this.arquivo.arrayBuffer()
        const workbook = XLSX.read(buffer)
        const primeiraAba = workbook.SheetNames[0]
        const linhas = XLSX.utils.sheet_to_json(workbook.Sheets[primeiraAba])

        if (linhas.length === 0) {
          this.erros.push('A primeira aba da planilha não tem nenhuma linha de dados.')
          return false
        }

        this.dadosOriginais = linhas
        this.dadosTratados = linhas.map((linha, indice) => this.tratarLinha(linha, indice))
        this.validarDados()
        return true
      } catch {
        this.erros.push('Não foi possível ler a planilha. O arquivo pode estar corrompido.')
        return false
      } finally {
        this.carregando = false
      }
    },

    /** Renomeia as colunas para um nome só e padroniza os campos de texto. */
    tratarLinha(linha, indice) {
      const tratada = {}

      for (const [cabecalho, valor] of Object.entries(linha)) {
        const campo = COLUNAS[normalizarCabecalho(cabecalho)]
        if (campo) tratada[campo] = valor
      }

      const segmento = chaveDeSegmento(tratada.segmento)

      return {
        // Número da linha no Excel, para o erro dizer onde corrigir. O SheetJS
        // guarda a posição em __rowNum__ (começa em 0); sem ele, conta a partir
        // da linha 2, logo abaixo do cabeçalho.
        linha: (linha.__rowNum__ ?? indice + 1) + 1,
        // Valores como vieram, para a tela mostrar o que foi padronizado.
        original: {
          segmento: tratada.segmento,
          consultor: tratada.consultor,
          nivel: tratada.nivel,
        },
        codigo: String(tratada.codigo ?? '').trim().toUpperCase(),
        nome: String(tratada.nome ?? '').trim().replace(/\s+/g, ' '),
        consultor: padronizarNome(tratada.consultor),
        // Segmento fora do mapa ainda sai em uma grafia só, como no limpeza.py.
        segmento: SEGMENTOS[segmento] || padronizarNome(tratada.segmento),
        // "Nivel A", "classe b" e " C " chegam como A, B e C.
        nivel: String(tratada.nivel ?? '')
          .replace(/n[íi]vel|classe/gi, '')
          .trim()
          .charAt(0)
          .toUpperCase(),
        faturamento: converterFaturamento(tratada.faturamento),
        servicos: String(tratada.servicos ?? '').trim(),
        dataContratacao: converterData(tratada.dataContratacao),
        cidade: String(tratada.cidade ?? '').trim(),
        uf: String(tratada.uf ?? '').trim().toUpperCase(),
        problemas: [],
      }
    },

    /**
     * Mesmas regras do ClienteDTO do Java, conferidas linha a linha. Cada linha
     * vai para dadosValidos ou para dadosInvalidos com a lista de problemas.
     */
    validarDados() {
      this.dadosValidos = []
      this.dadosInvalidos = []
      // Código -> primeira linha em que apareceu, para apontar o repetido.
      const primeiraLinhaDoCodigo = new Map()

      for (const cliente of this.dadosTratados) {
        const problemas = []
        if (!cliente.codigo) problemas.push('código vazio')
        if (!cliente.nome) problemas.push('nome vazio')
        if (!cliente.consultor) problemas.push('consultor vazio')
        if (!cliente.segmento) problemas.push('segmento vazio')
        if (!['A', 'B', 'C'].includes(cliente.nivel)) problemas.push('nível precisa ser A, B ou C')
        if (cliente.faturamento === null || cliente.faturamento <= 0) {
          problemas.push('faturamento precisa ser um número maior que zero')
        }
        if (!cliente.servicos) problemas.push('serviços contratados vazio')
        if (!cliente.dataContratacao) problemas.push('data de contratação vazia ou inválida')
        if (cliente.uf && !/^[A-Z]{2}$/.test(cliente.uf)) problemas.push('UF deve ter 2 letras')

        if (cliente.codigo) {
          if (primeiraLinhaDoCodigo.has(cliente.codigo)) {
            problemas.push(`código repetido (já aparece na linha ${primeiraLinhaDoCodigo.get(cliente.codigo)})`)
          } else {
            primeiraLinhaDoCodigo.set(cliente.codigo, cliente.linha)
          }
        }

        cliente.problemas = problemas
        if (problemas.length === 0) this.dadosValidos.push(cliente)
        else this.dadosInvalidos.push(cliente)
      }
    },

    /**
     * Envia só as linhas válidas, em duas etapas:
     * 1. JSON para /api/clientes/validar: o Java valida de novo e padroniza;
     * 2. a lista aprovada vira um .xlsx que vai para /api/planilhas (Python + banco).
     */
    async enviarParaBackend() {
      if (this.dadosValidos.length === 0 || this.enviando) return null

      this.enviando = true
      this.validando = true
      this.progresso = 0
      this.erroEnvio = null
      try {
        const aprovados = await validarClientes(this.dadosValidos.map(paraDto))
        this.validando = false

        const planilha = montarPlanilha(aprovados, this.arquivo.name)
        this.resultado = await enviarPlanilha(planilha, (valor) => {
          this.progresso = valor
        })
        return this.resultado
      } catch (falha) {
        // "indice" vem da validação do Java e é a posição na lista enviada:
        // traduzimos para a linha do Excel, que é o que a pessoa consegue achar.
        const linha = this.dadosValidos[Number(falha.indice)]?.linha
        const detalhes = (falha.detalhes ?? []).map((texto) => (linha ? `Linha ${linha}: ${texto}` : texto))
        this.erroEnvio = { mensagem: falha.message, detalhes }
        return null
      } finally {
        this.enviando = false
        this.validando = false
      }
    },

    limpar() {
      this.arquivo = null
      this.dadosOriginais = []
      this.dadosTratados = []
      this.dadosValidos = []
      this.dadosInvalidos = []
      this.erros = []
      this.progresso = 0
      this.resultado = null
      this.erroEnvio = null
    },
  },
})
