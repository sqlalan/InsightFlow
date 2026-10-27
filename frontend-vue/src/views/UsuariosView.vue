<script setup>
import { computed, onMounted, ref } from 'vue'
import PainelNav from '../components/layout/PainelNav.vue'
import { alterarStatusUsuario, listarUsuarios } from '../services/api'

/**
 * Aprovação de contas, só para o admin. Toda conta pedida em /cadastro chega
 * aqui como PENDENTE; o admin aprova (ATIVO) ou recusa (BLOQUEADO), e pode
 * desbloquear depois. A API é quem garante que só o admin chega a esta rota.
 */
const usuarios = ref([])
const carregando = ref(true)
const erro = ref('')
// Id da conta cujo botão foi clicado: desabilita só aquela linha enquanto salva.
const salvando = ref(null)
const filtro = ref('PENDENTE')

async function carregar() {
  carregando.value = true
  erro.value = ''
  try {
    usuarios.value = await listarUsuarios()
  } catch (falha) {
    erro.value = falha.message
  } finally {
    carregando.value = false
  }
}

onMounted(carregar)

const FILTROS = [
  { status: 'PENDENTE', label: 'Pendentes' },
  { status: 'ATIVO', label: 'Ativos' },
  { status: 'BLOQUEADO', label: 'Bloqueados' },
  { status: 'TODOS', label: 'Todos' },
]

const contagem = computed(() => {
  const total = { TODOS: usuarios.value.length, PENDENTE: 0, ATIVO: 0, BLOQUEADO: 0 }
  for (const usuario of usuarios.value) total[usuario.status] += 1
  return total
})

const visiveis = computed(() =>
  filtro.value === 'TODOS' ? usuarios.value : usuarios.value.filter((u) => u.status === filtro.value),
)

const ROTULO_STATUS = { PENDENTE: 'Pendente', ATIVO: 'Ativo', BLOQUEADO: 'Bloqueado' }
const COR_STATUS = {
  PENDENTE: 'bg-signal-500/15 text-signal-400',
  ATIVO: 'bg-trust-500/15 text-trust-400',
  BLOQUEADO: 'bg-red-500/15 text-red-300',
}

/** Botões de cada linha, conforme o status atual. A conta do admin não tem botão. */
function acoes(usuario) {
  if (usuario.perfil === 'ADMIN') return []
  if (usuario.status === 'PENDENTE') {
    return [
      { status: 'ATIVO', label: 'Aprovar', destaque: true },
      { status: 'BLOQUEADO', label: 'Recusar', confirmar: `Recusar o pedido de ${usuario.nome}?` },
    ]
  }
  if (usuario.status === 'ATIVO') {
    return [{ status: 'BLOQUEADO', label: 'Bloquear', confirmar: `Bloquear ${usuario.nome}? A sessão aberta será encerrada.` }]
  }
  return [{ status: 'ATIVO', label: 'Desbloquear', destaque: true }]
}

async function alterar(usuario, acao) {
  if (acao.confirmar && !window.confirm(acao.confirmar)) return
  salvando.value = usuario.id
  erro.value = ''
  try {
    const atualizado = await alterarStatusUsuario(usuario.id, acao.status)
    usuario.status = atualizado.status
  } catch (falha) {
    erro.value = falha.message
  } finally {
    salvando.value = null
  }
}

function formatarData(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })
}
</script>

<template>
  <div class="min-h-dvh bg-ink-950 lg:pl-64">
    <PainelNav />

    <main class="mx-auto max-w-7xl px-5 py-10 lg:px-8">
      <div class="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h1 class="text-2xl font-semibold text-white sm:text-3xl">Usuários</h1>
          <p class="mt-2 text-sm text-mist-400">
            Aprove os pedidos de conta dos consultores e bloqueie quem não deve mais acessar.
          </p>
        </div>
        <button
          type="button"
          class="self-start rounded-lg border border-white/10 px-4 py-2 text-sm font-medium text-mist-200 transition hover:bg-white/5 hover:text-white sm:self-auto"
          :disabled="carregando"
          @click="carregar"
        >
          Atualizar
        </button>
      </div>

      <div
        v-if="erro"
        role="alert"
        class="mt-6 rounded-xl border border-red-500/30 bg-red-500/10 px-5 py-4"
      >
        <p class="text-sm font-medium text-red-200">{{ erro }}</p>
      </div>

      <section class="mt-6 rounded-2xl border border-white/10 bg-ink-900/60 p-5 sm:p-6">
        <div class="mb-5 flex flex-wrap gap-2" role="tablist" aria-label="Filtrar por status">
          <button
            v-for="opcao in FILTROS"
            :key="opcao.status"
            type="button"
            role="tab"
            :aria-selected="filtro === opcao.status"
            class="rounded-lg px-3 py-1.5 text-sm font-medium transition"
            :class="
              filtro === opcao.status
                ? 'bg-flow-500/15 text-flow-200'
                : 'text-mist-400 hover:bg-white/5 hover:text-white'
            "
            @click="filtro = opcao.status"
          >
            {{ opcao.label }}
            <span class="ml-1 tabular-nums text-mist-500">{{ contagem[opcao.status] }}</span>
          </button>
        </div>

        <div v-if="carregando" class="space-y-3">
          <div v-for="n in 3" :key="n" class="h-12 animate-pulse rounded-lg bg-white/5" />
        </div>

        <p v-else-if="!visiveis.length" class="py-8 text-center text-sm text-mist-500">
          {{ filtro === 'PENDENTE' ? 'Nenhum pedido aguardando aprovação.' : 'Nenhuma conta neste filtro.' }}
        </p>

        <div v-else class="overflow-x-auto">
          <table class="w-full min-w-2xl text-left text-sm">
            <thead>
              <tr class="border-b border-white/10 text-xs uppercase tracking-wide text-mist-500">
                <th class="py-2 pr-4 font-medium">Nome</th>
                <th class="py-2 pr-4 font-medium">E-mail</th>
                <th class="py-2 pr-4 font-medium">Perfil</th>
                <th class="py-2 pr-4 font-medium">Status</th>
                <th class="py-2 pr-4 font-medium">Pedido em</th>
                <th class="py-2 text-right font-medium">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="usuario in visiveis" :key="usuario.id" class="border-b border-white/5 text-mist-300">
                <td class="py-2.5 pr-4 font-medium text-mist-100">{{ usuario.nome }}</td>
                <td class="py-2.5 pr-4">{{ usuario.email }}</td>
                <td class="py-2.5 pr-4">{{ usuario.perfil === 'ADMIN' ? 'Administrador' : 'Consultor' }}</td>
                <td class="py-2.5 pr-4">
                  <span class="rounded-md px-2 py-0.5 text-xs font-semibold" :class="COR_STATUS[usuario.status]">
                    {{ ROTULO_STATUS[usuario.status] }}
                  </span>
                </td>
                <td class="py-2.5 pr-4 tabular-nums">{{ formatarData(usuario.criadoEm) }}</td>
                <td class="py-2.5">
                  <div class="flex justify-end gap-2">
                    <span v-if="!acoes(usuario).length" class="text-mist-500">—</span>
                    <button
                      v-for="acao in acoes(usuario)"
                      :key="acao.label"
                      type="button"
                      :disabled="salvando === usuario.id"
                      class="rounded-lg px-3 py-1.5 text-xs font-semibold transition disabled:opacity-60"
                      :class="
                        acao.destaque
                          ? 'bg-flow-500/20 text-flow-200 hover:bg-flow-500/30'
                          : 'border border-white/10 text-mist-300 hover:bg-white/5 hover:text-white'
                      "
                      @click="alterar(usuario, acao)"
                    >
                      {{ acao.label }}
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </main>
  </div>
</template>
