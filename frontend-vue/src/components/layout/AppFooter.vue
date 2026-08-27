<script setup>
import { RouterLink } from 'vue-router'
import BrandLogo from '../BrandLogo.vue'

const year = new Date().getFullYear()

// Marcas de contato, sem destino: a CTI ainda nao passou os enderecos. Ficam
// como simbolo ate existir link real -- link para "#" so finge que funciona.
const contatos = [
  { nome: 'Instagram', icone: 'instagram' },
  { nome: 'Facebook', icone: 'facebook' },
  { nome: 'WhatsApp', icone: 'whatsapp' },
  { nome: 'E-mail', icone: 'email' },
]
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

      <div class="flex flex-wrap items-center gap-x-6 gap-y-4">
        <ul class="flex items-center gap-2">
          <li v-for="contato in contatos" :key="contato.nome">
            <span
              class="grid h-9 w-9 place-items-center rounded-lg border border-white/10 bg-white/5 text-mist-300"
              role="img"
              :aria-label="contato.nome"
              :title="contato.nome"
            >
              <svg viewBox="0 0 24 24" class="h-4.5 w-4.5" aria-hidden="true">
                <g v-if="contato.icone === 'instagram'" fill="none" stroke="currentColor" stroke-width="1.7">
                  <rect x="3.5" y="3.5" width="17" height="17" rx="5" />
                  <circle cx="12" cy="12" r="3.8" />
                  <circle cx="16.9" cy="7.1" r="1" fill="currentColor" stroke="none" />
                </g>

                <path
                  v-else-if="contato.icone === 'facebook'"
                  fill="currentColor"
                  d="M13.1 21v-8.2h2.75l.41-3.19H13.1V7.57c0-.92.26-1.55 1.58-1.55h1.69V3.17c-.29-.04-1.3-.13-2.47-.13-2.44 0-4.11 1.49-4.11 4.23v2.34H7.03v3.19H9.8V21z"
                />

                <g v-else-if="contato.icone === 'whatsapp'" fill="none" stroke="currentColor" stroke-width="1.7">
                  <path
                    d="M3.8 20.2 5 16.5A8.2 8.2 0 1 1 8.1 19.5z"
                    stroke-linejoin="round"
                  />
                  <path
                    d="M9.1 8.2c.15-.35.3-.36.5-.37h.42c.14 0 .34 0 .5.42l.56 1.36c.07.2 0 .36-.07.5l-.35.42c-.14.14-.21.29-.07.5a5 5 0 0 0 2.26 1.98c.21.07.35.07.5-.07l.42-.5c.14-.14.28-.14.42-.07l1.27.64c.21.07.35.21.35.35v.42c0 .28-.21.64-.5.78-.7.43-1.62.29-2.68-.21a7.8 7.8 0 0 1-3.53-3.54c-.42-1-.56-1.9-.21-2.54Z"
                    stroke-width="1.3"
                  />
                </g>

                <g v-else fill="none" stroke="currentColor" stroke-width="1.7">
                  <rect x="3" y="5.5" width="18" height="13" rx="2.5" />
                  <path d="m3.8 7.2 7.3 5.2c.55.4 1.25.4 1.8 0l7.3-5.2" stroke-linecap="round" />
                </g>
              </svg>
            </span>
          </li>
        </ul>

        <div class="flex items-center gap-6">
          <p class="text-xs text-mist-500">© {{ year }} InsightFlow</p>
          <RouterLink to="/login" class="text-xs font-medium text-mist-300 hover:text-white">
            Acessar plataforma
          </RouterLink>
        </div>
      </div>
    </div>
  </footer>
</template>
