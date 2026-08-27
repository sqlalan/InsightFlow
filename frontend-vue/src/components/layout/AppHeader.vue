<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import BrandLogo from '../BrandLogo.vue'

// A landing tem uma secao so, entao o cabecalho nao precisa de navegacao nem
// de menu no celular: sobra a marca e a entrada para o sistema.
const scrolled = ref(false)

function onScroll() {
  scrolled.value = window.scrollY > 12
}

onMounted(() => {
  onScroll()
  window.addEventListener('scroll', onScroll, { passive: true })
})
onUnmounted(() => window.removeEventListener('scroll', onScroll))
</script>

<template>
  <header
    class="fixed inset-x-0 top-0 z-50 transition-all duration-300"
    :class="scrolled ? 'border-b border-white/10 bg-ink-950/80 backdrop-blur-xl' : 'border-b border-transparent'"
  >
    <div class="mx-auto flex h-18 max-w-7xl items-center justify-between px-5 lg:px-8">
      <RouterLink to="/" class="shrink-0" aria-label="InsightFlow — início">
        <BrandLogo />
      </RouterLink>

      <RouterLink
        to="/login"
        class="group inline-flex items-center gap-2 rounded-lg bg-white px-5 py-2.5 text-sm font-semibold text-ink-900 shadow-lg shadow-black/20 transition hover:bg-mist-100 hover:shadow-flow-500/10"
      >
        Entrar
        <svg
          viewBox="0 0 20 20"
          class="h-4 w-4 transition group-hover:translate-x-0.5"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          aria-hidden="true"
        >
          <path d="M4 10h11m0 0-4.5-4.5M15 10l-4.5 4.5" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
      </RouterLink>
    </div>
  </header>
</template>
