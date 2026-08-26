<script setup>
import { RouterLink } from 'vue-router'

// Valores de referência — ajuste conforme a política comercial definida.
const plans = [
  {
    name: 'Essencial',
    price: 'R$ 249',
    period: '/mês',
    summary: 'Para uma área que quer sair das planilhas manuais.',
    features: [
      'Até 20 planilhas por mês',
      '3 usuários incluídos',
      'Painéis padrão e exportação em PDF',
      'Histórico de 6 meses',
    ],
    cta: 'Começar agora',
    highlight: false,
  },
  {
    name: 'Operação',
    price: 'R$ 690',
    period: '/mês',
    summary: 'Para times que dependem de dados todos os dias.',
    features: [
      'Planilhas ilimitadas',
      '15 usuários e permissões por área',
      'Alertas por regra e envios agendados',
      'Trilha de auditoria completa',
      'Suporte prioritário',
    ],
    cta: 'Falar com o time',
    highlight: true,
  },
  {
    name: 'Corporativo',
    price: 'Sob consulta',
    period: '',
    summary: 'Para empresas com exigências de compliance e SSO.',
    features: [
      'Usuários ilimitados',
      'SSO / SAML e política de sessão',
      'Ambiente dedicado',
      'Acordo de nível de serviço (SLA)',
      'Gerente de conta',
    ],
    cta: 'Solicitar proposta',
    highlight: false,
  },
]
</script>

<template>
  <section id="planos" class="relative border-t border-white/8 py-24 lg:py-32">
    <div class="mx-auto max-w-7xl px-5 lg:px-8">
      <div class="mx-auto max-w-2xl text-center">
        <p class="text-xs font-semibold uppercase tracking-[0.2em] text-flow-400">Planos</p>
        <h2 class="mt-4 text-3xl font-semibold tracking-tight text-white sm:text-4xl">
          Preço previsível, sem cobrança por linha processada
        </h2>
        <p class="mt-5 text-base leading-relaxed text-mist-400">
          Todos os planos incluem criptografia, backup diário e 14 dias de teste sem
          cartão de crédito.
        </p>
      </div>

      <div class="mt-16 grid items-start gap-6 lg:grid-cols-3">
        <div
          v-for="plan in plans"
          :key="plan.name"
          class="relative flex h-full flex-col rounded-2xl border p-8 transition"
          :class="
            plan.highlight
              ? 'border-flow-400/35 bg-linear-to-b from-flow-500/12 to-white/2 shadow-2xl shadow-flow-500/10 lg:-translate-y-3'
              : 'border-white/8 bg-white/2.5 hover:border-white/20'
          "
        >
          <span
            v-if="plan.highlight"
            class="absolute -top-3 left-8 rounded-full bg-linear-to-r from-flow-400 to-signal-500 px-3 py-1 text-[11px] font-semibold text-white"
          >
            Mais escolhido
          </span>

          <h3 class="text-sm font-semibold uppercase tracking-wide text-mist-300">
            {{ plan.name }}
          </h3>
          <p class="mt-4 flex items-baseline gap-1">
            <span class="text-3xl font-semibold tracking-tight text-white">{{ plan.price }}</span>
            <span v-if="plan.period" class="text-sm text-mist-400">{{ plan.period }}</span>
          </p>
          <p class="mt-3 text-sm leading-relaxed text-mist-400">{{ plan.summary }}</p>

          <ul class="mt-7 flex-1 space-y-3.5">
            <li v-for="f in plan.features" :key="f" class="flex items-start gap-2.5">
              <svg
                viewBox="0 0 20 20"
                class="mt-0.5 h-4 w-4 shrink-0"
                :class="plan.highlight ? 'text-flow-300' : 'text-mist-400'"
                fill="none"
                stroke="currentColor"
                stroke-width="2.2"
              >
                <path d="m4.5 10.4 3.2 3.1 7.8-7.6" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
              <span class="text-sm leading-relaxed text-mist-300">{{ f }}</span>
            </li>
          </ul>

          <RouterLink
            to="/login"
            class="mt-9 rounded-xl px-5 py-3 text-center text-sm font-semibold transition"
            :class="
              plan.highlight
                ? 'bg-linear-to-r from-flow-400 to-signal-500 text-white shadow-lg shadow-flow-500/25 hover:shadow-flow-500/40'
                : 'border border-white/12 bg-white/5 text-mist-100 hover:bg-white/10'
            "
          >
            {{ plan.cta }}
          </RouterLink>
        </div>
      </div>
    </div>
  </section>
</template>
