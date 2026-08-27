<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import BrandLogo from '../BrandLogo.vue'

const scrolled = ref(false)
const open = ref(false)

const links = [
  { label: 'Como funciona', href: '#como-funciona' },
  { label: 'Recursos', href: '#recursos' },
  { label: 'Segurança', href: '#seguranca' },
]

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

      <nav class="hidden items-center gap-1 lg:flex" aria-label="Navegação principal">
        <a
          v-for="link in links"
          :key="link.href"
          :href="link.href"
          class="rounded-lg px-3.5 py-2 text-sm font-medium text-mist-300 transition hover:bg-white/5 hover:text-white"
        >
          {{ link.label }}
        </a>
      </nav>

      <div class="hidden items-center gap-3 lg:flex">
        <RouterLink
          to="/login"
          class="rounded-lg px-4 py-2 text-sm font-medium text-mist-200 transition hover:text-white"
        >
          Entrar
        </RouterLink>
        <RouterLink
          to="/login"
          class="rounded-lg bg-white px-4 py-2.5 text-sm font-semibold text-ink-900 shadow-lg shadow-black/20 transition hover:bg-mist-100 hover:shadow-flow-500/10"
        >
          Solicitar demonstração
        </RouterLink>
      </div>

      <button
        type="button"
        class="grid h-10 w-10 place-items-center rounded-lg border border-white/10 text-mist-200 lg:hidden"
        :aria-expanded="open"
        aria-controls="menu-mobile"
        aria-label="Abrir menu"
        @click="open = !open"
      >
        <svg viewBox="0 0 24 24" class="h-5 w-5" fill="none" stroke="currentColor" stroke-width="2">
          <path v-if="!open" d="M4 7h16M4 12h16M4 17h16" stroke-linecap="round" />
          <path v-else d="M6 6l12 12M18 6L6 18" stroke-linecap="round" />
        </svg>
      </button>
    </div>

    <Transition
      enter-active-class="transition duration-200 ease-out"
      enter-from-class="-translate-y-3 opacity-0"
      leave-active-class="transition duration-150 ease-in"
      leave-to-class="-translate-y-3 opacity-0"
    >
      <div
        v-show="open"
        id="menu-mobile"
        class="border-t border-white/10 bg-ink-950/95 px-5 pb-6 pt-3 backdrop-blur-xl lg:hidden"
      >
        <a
          v-for="link in links"
          :key="link.href"
          :href="link.href"
          class="block rounded-lg px-3 py-3 text-sm font-medium text-mist-300 hover:bg-white/5 hover:text-white"
          @click="open = false"
        >
          {{ link.label }}
        </a>
        <div class="mt-3 grid gap-2 border-t border-white/10 pt-4">
          <RouterLink
            to="/login"
            class="rounded-lg border border-white/12 px-4 py-2.5 text-center text-sm font-medium text-mist-200"
          >
            Entrar
          </RouterLink>
          <RouterLink
            to="/login"
            class="rounded-lg bg-white px-4 py-2.5 text-center text-sm font-semibold text-ink-900"
          >
            Solicitar demonstração
          </RouterLink>
        </div>
      </div>
    </Transition>
  </header>
</template>
