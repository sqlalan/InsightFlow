<script setup>
import { RouterLink } from 'vue-router'
import BrandLogo from '../BrandLogo.vue'

const links = [
  { to: '/upload', label: 'Enviar planilha' },
  { to: '/dashboard', label: 'Dashboard' },
]
</script>

<template>
  <header class="sticky top-0 z-40 border-b border-white/10 bg-ink-950/80 backdrop-blur-xl">
    <div class="mx-auto flex h-16 max-w-7xl items-center justify-between gap-4 px-5 lg:px-8">
      <RouterLink to="/" class="shrink-0" aria-label="InsightFlow — início">
        <BrandLogo />
      </RouterLink>

      <nav class="flex items-center gap-1" aria-label="Navegação do painel">
        <RouterLink
          v-for="link in links"
          :key="link.to"
          :to="link.to"
          class="rounded-lg px-3 py-2 text-sm font-medium text-mist-300 transition hover:bg-white/5 hover:text-white"
          active-class="bg-flow-500/15 text-flow-200"
        >
          {{ link.label }}
        </RouterLink>
      </nav>
    </div>
  </header>
</template>
