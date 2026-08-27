<script setup>
import { RouterLink } from 'vue-router'
import BrandLogo from '../BrandLogo.vue'

const year = new Date().getFullYear()
</script>

<template>
  <!-- Rodape de uma faixa so: as colunas de links apontavam para secoes que a
       landing curta nao tem mais. -->
  <footer class="border-t border-white/10 bg-ink-950">
    <div
      class="mx-auto flex max-w-7xl flex-col gap-6 px-5 py-8 lg:flex-row lg:items-center lg:justify-between lg:px-8"
    >
      <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:gap-6">
        <BrandLogo size="sm" />
        <div class="flex flex-wrap gap-2">
          <span
            v-for="badge in ['LGPD', 'Criptografia AES-256', 'Dados no Brasil']"
            :key="badge"
            class="rounded-full border border-white/10 bg-white/5 px-3 py-1 text-xs font-medium text-mist-300"
          >
            {{ badge }}
          </span>
        </div>
      </div>

      <div class="flex items-center gap-6">
        <p class="text-xs text-mist-500">© {{ year }} InsightFlow</p>
        <RouterLink to="/login" class="text-xs font-medium text-mist-300 hover:text-white">
          Acessar plataforma
        </RouterLink>
      </div>
    </div>
  </footer>
</template>
