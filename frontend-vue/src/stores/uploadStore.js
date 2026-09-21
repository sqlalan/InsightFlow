import { defineStore } from 'pinia'
import * as XLSX from 'xlsx'
import { enviarPlanilha } from '../services/api'

/**
 * Store da tela de upload.
 *
 * Fluxo: o navegador le a planilha e mostra uma previa para conferencia; so
 * depois o arquivo vai para a API, onde o modulo Python faz o tratamento que
 * vale. A previa serve para o usuario perceber que escolheu o arquivo errado
 * antes de enviar 10 MB para o servidor -- ela nao substitui o tratamento.
 */

const EXTENSOES = ['.xlsx', '.xls']
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
  data_contratacao: 'dataInicio',
  data_inicio: 'dataInicio',
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

/** "Linha 32 (CTI031)" ou "Linhas 32 (CTI031), 40 (CTI039) e mais 2". */
function ondeEsta(clientes) {
  const referencias = clientes.map((c) => (c.codigo ? `${c.linha} (${c.codigo})` : `${c.linha}`))
  const primeiras = referencias.slice(0, 3).join(', ')
  const resto = referencias.length > 3 ? ` e mais ${referencias.length - 3}` : ''
  return `${referencias.length > 1 ? 'Linhas' : 'Linha'} ${primeiras}${resto}`
}

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

export const useUploadStore = defineStore('upload', {
  state: () => ({
    arquivo: null,
    dadosOriginais: [],
    dadosTratados: [],
    // erros impedem o envio (arquivo inválido); avisos só informam o que o tratamento vai corrigir.
    erros: [],
    avisos: [],
    carregando: false,
    enviando: false,
    progresso: 0,
    resultado: null,
    // Separado de `erros`: aquilo é a prévia no navegador; isto é a API recusando o envio.
    erroEnvio: null,
  }),

  getters: {
    totalClientes: (state) => state.dadosTratados.length,
    totalErros: (state) => state.erros.length,
    totalAvisos: (state) => state.avisos.length,
    clientesNivelA: (state) => state.dadosTratados.filter((c) => c.nivel === 'A').length,
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
          // Célula vazia não é ajuste: vira aviso em validarConteudo().
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
        this.erros.push('Formato não suportado. Envie a planilha em .xlsx ou .xls.')
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

    /** Le a planilha no navegador e monta a previa. Nada sai da maquina aqui. */
    async processarPlanilha() {
      this.erros = []
      this.avisos = []
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
        this.validarConteudo()
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
        // Número da linha no Excel, para o aviso dizer onde corrigir. O SheetJS
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
        nome: padronizarNome(tratada.nome),
        consultor: padronizarNome(tratada.consultor),
        // Segmento fora do mapa ainda sai em uma grafia só, como no limpeza.py.
        segmento: SEGMENTOS[segmento] || padronizarNome(tratada.segmento),
        // "Nivel A", "classe b" e " C " chegam como A, B e C.
        nivel: String(tratada.nivel ?? '')
          .replace(/n[íi]vel|classe/gi, '')
          .trim()
          .charAt(0)
          .toUpperCase(),
        faturamento: tratada.faturamento ?? '',
        servicos: String(tratada.servicos ?? '').trim(),
      }
    },

    /** Aponta problemas no conteúdo. Não bloqueia: diz o que o tratamento vai fazer com cada um. */
    validarConteudo() {
      const linhasPorCodigo = new Map()
      const semCodigo = []
      const nivelInvalido = []
      const semFaturamento = []
      const semServico = []
      const semConsultor = []

      for (const cliente of this.dadosTratados) {
        if (!cliente.codigo) {
          semCodigo.push(cliente)
          continue
        }
        linhasPorCodigo.set(cliente.codigo, [...(linhasPorCodigo.get(cliente.codigo) ?? []), cliente.linha])
        if (!['A', 'B', 'C'].includes(cliente.nivel)) nivelInvalido.push(cliente)
        if (String(cliente.faturamento).trim() === '') semFaturamento.push(cliente)
        if (!cliente.servicos) semServico.push(cliente)
        if (!cliente.consultor) semConsultor.push(cliente)
      }

      const avisar = (clientes, problema) => {
        if (clientes.length) this.avisos.push(`${ondeEsta(clientes)}: ${problema}`)
      }

      avisar(semCodigo, 'sem código do cliente. Não entra na análise.')
      for (const [codigo, linhas] of linhasPorCodigo) {
        if (linhas.length > 1) {
          this.avisos.push(
            `${codigo} aparece nas linhas ${linhas.join(' e ')}. Vale a última (linha ${linhas.at(-1)}).`,
          )
        }
      }
      avisar(nivelInvalido, 'nível fora de A, B ou C. Será classificado como C.')
      avisar(semFaturamento, 'sem faturamento. Fica fora dos cálculos de faturamento.')
      avisar(semServico, 'sem serviço contratado. Nenhum contrato é registrado.')
      avisar(semConsultor, 'sem consultor. Fica como "Não informado".')
    },

    /**
     * Envia o arquivo original para a API. Vai o arquivo, não a prévia: quem
     * trata de verdade é o módulo Python, e ele precisa da planilha como veio.
     */
    async enviarParaBackend() {
      if (!this.arquivo || this.enviando) return null

      this.enviando = true
      this.progresso = 0
      this.erroEnvio = null
      try {
        this.resultado = await enviarPlanilha(this.arquivo, (valor) => {
          this.progresso = valor
        })
        return this.resultado
      } catch (falha) {
        this.erroEnvio = { mensagem: falha.message, detalhes: falha.detalhes ?? [] }
        return null
      } finally {
        this.enviando = false
      }
    },

    limpar() {
      this.arquivo = null
      this.dadosOriginais = []
      this.dadosTratados = []
      this.erros = []
      this.avisos = []
      this.progresso = 0
      this.resultado = null
      this.erroEnvio = null
    },
  },
})
