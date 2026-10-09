import { defineStore } from 'pinia'
import { entrar, sair } from '../services/api'
import { getSession } from '../services/session'

/**
 * Store do login, no formato do store.txt da aula: state, getters e actions.
 *
 * Diferença da aula: lá qualquer usuário e senha preenchidos eram aceitos.
 * Aqui quem confere é o Spring Boot (POST /api/auth/login), que devolve um
 * token. O token fica guardado por services/session.js, e o api.js o envia
 * em toda requisição.
 */
export const useAuthStore = defineStore('auth', {
  // STATE = dados que ficam guardados enquanto a aplicação está em uso.
  // Começa pela sessão salva no navegador: recarregar a página não desloga.
  state: () => ({
    logado: getSession() !== null,
    usuario: getSession()?.email ?? '',
  }),

  // GETTERS = valores calculados a partir do state.
  getters: {
    nomeExibicao: (state) => state.usuario || 'Usuário',
  },

  // ACTIONS = ações que alteram o state.
  actions: {
    /** Se o servidor recusar, o erro sobe com a mensagem para a tela de login mostrar. */
    async login(email, senha, lembrar) {
      const sessao = await entrar(email, senha, lembrar)
      this.logado = true
      this.usuario = sessao.email
      return true
    },

    async logout() {
      try {
        await sair()
      } catch {
        // Mesmo sem resposta do servidor, a sessão local já foi apagada por sair().
      }
      this.logado = false
      this.usuario = ''
    },
  },
})
