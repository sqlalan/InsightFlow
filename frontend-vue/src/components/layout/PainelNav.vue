<script setup>
import { ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { sair } from '../../services/api'
import BrandLogo from '../BrandLogo.vue'

const route = useRoute()
const router = useRouter()
const saindo = ref(false)
async function encerrarSessao() {
  saindo.value = true
  try { await sair() } catch { /* A sessao local ja foi removida. */ }
  await router.push('/login')
  saindo.value = false
}
const aberto = ref(false)

const links = [
  { to: '/upload', label: 'Enviar planilha', icone: 'upload' },
  { to: '/dashboard', label: 'Dashboard', icone: 'grafico' },
  { to: '/relatorios', label: 'Relatórios', icone: 'relatorio' },
]

// no celular a lateral vira gaveta: navegar precisa fechar o que cobre a tela
watch(() => route.path, () => (aberto.value = false))
</script>

<template>
  <!-- barra superior: existe so no celular, onde a lateral fica recolhida -->
  <header
    class="sticky top-0 z-40 flex h-16 items-center gap-3 border-b border-white/10 bg-ink-950/80 px-5 backdrop-blur-xl lg:hidden"
  >
    <button
      type="button"
      class="grid h-10 w-10 shrink-0 place-items-center rounded-lg border border-white/10 text-mist-200 transition hover:bg-white/5 hover:text-white"
      :aria-expanded="aberto"
      aria-controls="menu-painel"
      aria-label="Abrir menu do painel"
      @click="aberto = true"
    >
      <svg viewBox="0 0 24 24" class="h-5 w-5" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M4 7h16M4 12h16M4 17h16" stroke-linecap="round" />
      </svg>
    </button>

    <RouterLink to="/" aria-label="InsightFlow — início">
      <BrandLogo size="sm" />
    </RouterLink>
  </header>

  <!-- fundo escurecido da gaveta -->
  <Transition
    enter-active-class="transition duration-200 ease-out"
    enter-from-class="opacity-0"
    leave-active-class="transition duration-150 ease-in"
    leave-to-class="opacity-0"
  >
    <div
      v-show="aberto"
      class="fixed inset-0 z-40 bg-black/60 backdrop-blur-sm lg:hidden"
      aria-hidden="true"
      @click="aberto = false"
    />
  </Transition>

  <!-- Lateral: fixa no desktop, gaveta no celular. Recolhida, "invisible" a tira
       do foco do teclado; a partir do lg as variantes vencem a base por virem
       depois na folha de estilo, e a lateral fica sempre aberta. -->
  <aside
    id="menu-painel"
    class="fixed inset-y-0 left-0 z-50 flex w-64 flex-col border-r border-white/10 bg-ink-900/95 backdrop-blur-xl transition-transform duration-250 ease-out motion-reduce:transition-none lg:visible lg:translate-x-0 lg:bg-ink-900/40"
    :class="aberto ? 'visible translate-x-0' : 'invisible -translate-x-full'"
    aria-label="Navegação do painel"
  >
    <div class="flex h-16 shrink-0 items-center justify-between gap-2 px-5">
      <RouterLink to="/" aria-label="InsightFlow — início">
        <BrandLogo size="sm" />
      </RouterLink>

      <button
        type="button"
        class="grid h-9 w-9 place-items-center rounded-lg text-mist-400 transition hover:bg-white/5 hover:text-white lg:hidden"
        aria-label="Fechar menu do painel"
        @click="aberto = false"
      >
        <svg viewBox="0 0 24 24" class="h-5 w-5" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M6 6l12 12M18 6L6 18" stroke-linecap="round" />
        </svg>
      </button>
    </div>

    <nav class="flex-1 space-y-1 px-3 py-4">
      <p class="px-3 pb-2 text-xs font-semibold uppercase tracking-wide text-mist-500">Painel</p>

      <RouterLink
        v-for="link in links"
        :key="link.to"
        :to="link.to"
        class="flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-mist-300 transition hover:bg-white/5 hover:text-white"
        active-class="bg-flow-500/15 text-flow-200"
      >
        <svg
          viewBox="0 0 24 24"
          class="h-4.5 w-4.5 shrink-0"
          fill="none"
          stroke="currentColor"
          stroke-width="1.8"
          aria-hidden="true"
        >
          <template v-if="link.icone === 'upload'">
            <path d="M12 16V4m0 0L8 8m4-4 4 4" stroke-linecap="round" stroke-linejoin="round" />
            <path
              d="M4 16v2.5A1.5 1.5 0 0 0 5.5 20h13a1.5 1.5 0 0 0 1.5-1.5V16"
              stroke-linecap="round"
            />
          </template>
          <template v-else-if="link.icone === 'grafico'">
            <path d="M4 20h16" stroke-linecap="round" />
            <rect x="5" y="12" width="3.5" height="6" rx="1" />
            <rect x="10.25" y="8" width="3.5" height="10" rx="1" />
            <rect x="15.5" y="4" width="3.5" height="14" rx="1" />
          </template>
          <template v-else>
            <path d="M14 3v4.5h4.5" stroke-linecap="round" stroke-linejoin="round" />
            <path
              d="M19 8.5V19a1.5 1.5 0 0 1-1.5 1.5h-11A1.5 1.5 0 0 1 5 19V5a1.5 1.5 0 0 1 1.5-1.5H14z"
              stroke-linejoin="round"
            />
            <path d="M8.5 12.5h7M8.5 16h4.5" stroke-linecap="round" />
          </template>
        </svg>
        {{ link.label }}
      </RouterLink>
    </nav>

    <div class="border-t border-white/10 p-3">
      <button type="button" :disabled="saindo" @click="encerrarSessao"
        class="w-full rounded-lg px-3 py-2.5 text-left text-sm text-mist-300 hover:bg-white/5">
        {{ saindo ? 'Saindo...' : 'Sair da plataforma' }}
      </button>
      <RouterLink
        to="/"
        class="flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-mist-400 transition hover:bg-white/5 hover:text-white"
      >
        <svg
          viewBox="0 0 24 24"
          class="h-4.5 w-4.5 shrink-0"
          fill="none"
          stroke="currentColor"
          stroke-width="1.8"
          aria-hidden="true"
        >
          <path d="M19 12H5m0 0 5-5m-5 5 5 5" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
        Sair
      </RouterLink>
    </div>
  </aside>
</template>
