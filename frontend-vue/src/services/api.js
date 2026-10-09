import axios from 'axios'
import { getSession, saveSession, clearSession } from './session'

/**
 * Cliente HTTP único do projeto.
 *
 * A URL da API vem de VITE_API_URL: em desenvolvimento aponta para o Spring Boot
 * local, em produção para o serviço publicado no Render. Nada de domínio fixo no
 * código — é o mesmo motivo pelo qual o CORS do Back-end lê a origem de variável
 * de ambiente.
 */
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080',
  timeout: 180_000, // o tratamento em Python pode demorar em planilhas grandes
})

api.interceptors.request.use((config) => {
  const session = getSession()
  if (session) config.headers.Authorization = `Bearer ${session.token}`
  return config
})

export async function entrar(email, password, remember) {
  const { data } = await api.post('/api/auth/login', { email, password, remember })
  saveSession(data, remember)
  return data
}

export async function sair() {
  try {
    await api.post('/api/auth/logout')
  } finally {
    clearSession()
  }
}

/** Mensagens para os casos em que a resposta não traz corpo de erro. */
const MENSAGENS_POR_STATUS = {
  0: 'Não foi possível falar com o servidor. Verifique sua conexão.',
  404: 'Recurso não encontrado.',
  413: 'A planilha passou do limite de 10 MB.',
  500: 'O servidor encontrou um erro inesperado. Tente novamente em instantes.',
  503: 'O serviço de análise está indisponível no momento.',
}

/**
 * Erro já traduzido para a tela: o componente exibe `mensagem` e, quando existe,
 * a lista `detalhes` (ex.: quais colunas faltaram na planilha).
 */
export class ErroDaApi extends Error {
  constructor(mensagem, { status = 0, codigo = null, detalhes = [] } = {}) {
    super(mensagem)
    this.name = 'ErroDaApi'
    this.status = status
    this.codigo = codigo
    this.detalhes = detalhes
  }
}

api.interceptors.response.use(
  (resposta) => resposta,
  (erro) => {
    if (axios.isCancel(erro) || erro.code === 'ECONNABORTED') {
      return Promise.reject(
        new ErroDaApi('O processamento demorou mais do que o esperado e foi interrompido.'),
      )
    }

    const status = erro.response?.status ?? 0
    if (status === 401 && !erro.config?.url?.endsWith('/auth/login')) {
      clearSession()
      if (window.location.pathname !== '/login') window.location.assign('/login')
    }
    const corpo = erro.response?.data

    return Promise.reject(
      new ErroDaApi(
        corpo?.mensagem ?? MENSAGENS_POR_STATUS[status] ?? 'Não foi possível concluir a operação.',
        { status, codigo: corpo?.erro ?? null, detalhes: corpo?.detalhes ?? [] },
      ),
    )
  },
)

/** Envia a planilha Excel e devolve o resumo do processamento. */
export function enviarPlanilha(arquivo, aoProgredir) {
  const dados = new FormData()
  dados.append('arquivo', arquivo)

  return api
    .post('/api/planilhas', dados, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (evento) => {
        if (aoProgredir && evento.total) {
          aoProgredir(Math.round((evento.loaded * 100) / evento.total))
        }
      },
    })
    .then((resposta) => resposta.data)
}

export function buscarIndicadores() {
  return api.get('/api/indicadores').then((resposta) => resposta.data)
}

export function buscarInsights() {
  return api.get('/api/insights').then((resposta) => resposta.data)
}

/** Histórico de uploads e execuções do módulo de análise. */
export function buscarTelemetria() {
  return api.get('/api/telemetria').then((resposta) => resposta.data)
}

export function listarClientes(segmento) {
  return api
    .get('/api/clientes', { params: segmento ? { segmento } : {} })
    .then((resposta) => resposta.data)
}

export function criarCliente(cliente) {
  return api.post('/api/clientes', cliente).then((resposta) => resposta.data)
}

export function atualizarCliente(id, cliente) {
  return api.put(`/api/clientes/${id}`, cliente).then((resposta) => resposta.data)
}

export function reclassificarCliente(id, nivel) {
  return api.patch(`/api/clientes/${id}/nivel`, { nivel }).then((resposta) => resposta.data)
}

export function removerCliente(id) {
  return api.delete(`/api/clientes/${id}`).then(() => true)
}

export default api
