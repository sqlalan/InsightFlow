<script setup>
import { RouterLink } from 'vue-router'
import BrandLogo from '../BrandLogo.vue'

const year = new Date().getFullYear()

const columns = [
  {
    title: 'Produto',
    items: [
      { label: 'Como funciona', href: '#como-funciona' },
      { label: 'Recursos', href: '#recursos' },
      { label: 'Segurança', href: '#seguranca' },
    ],
  },
  {
    title: 'Empresa',
    items: [
      { label: 'Sobre', href: '#' },
      { label: 'Contato', href: '#' },
      { label: 'Termos de uso', href: '#' },
    ],
  },
]
</script>

<template>
  <footer class="border-t border-white/10 bg-ink-950">
    <div class="mx-auto max-w-7xl px-5 py-12 lg:px-8">
      <div class="grid gap-10 lg:grid-cols-[1.6fr_repeat(2,1fr)]">
        <div class="max-w-sm">
          <BrandLogo />
          <p class="mt-4 text-sm leading-relaxed text-mist-400">
            Análise de planilhas Excel para times de operações que precisam de resposta
            rápida, rastreável e segura.
          </p>
          <div class="mt-5 flex flex-wrap gap-2">
            <span
              v-for="badge in ['LGPD', 'Criptografia AES-256', 'Dados no Brasil']"
              :key="badge"
              class="rounded-full border border-white/10 bg-white/5 px-3 py-1 text-xs font-medium text-mist-300"
            >
              {{ badge }}
            </span>
          </div>
        </div>

        <div v-for="col in columns" :key="col.title">
          <h3 class="text-sm font-semibold text-white">{{ col.title }}</h3>
          <ul class="mt-4 space-y-3">
            <li v-for="item in col.items" :key="item.label">
              <a :href="item.href" class="text-sm text-mist-400 transition hover:text-flow-300">
                {{ item.label }}
              </a>
            </li>
          </ul>
        </div>
      </div>

      <div
        class="mt-10 flex flex-col gap-4 border-t border-white/10 pt-6 sm:flex-row sm:items-center sm:justify-between"
      >
        <p class="text-xs text-mist-500">
          © {{ year }} InsightFlow. Todos os direitos reservados.
        </p>
        <div class="flex items-center gap-6">
          <span class="inline-flex items-center gap-2 text-xs text-mist-500">
            <span class="relative flex h-2 w-2">
              <span class="absolute inline-flex h-full w-full animate-ping rounded-full bg-trust-400 opacity-75" />
              <span class="relative inline-flex h-2 w-2 rounded-full bg-trust-500" />
            </span>
            Todos os sistemas operacionais
          </span>
          <RouterLink to="/login" class="text-xs font-medium text-mist-300 hover:text-white">
            Acessar plataforma
          </RouterLink>
        </div>
      </div>
    </div>
  </footer>
</template>
