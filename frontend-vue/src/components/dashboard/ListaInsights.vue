<script setup>
/**
 * Lista de insights em texto simples — o resultado final do módulo de Ciência de
 * Dados. O texto já vem pronto do Back-end (`gerarResumo()` de cada subtipo de
 * Insight); aqui só se decide como ele aparece.
 */
defineProps({
  insights: { type: Array, default: () => [] },
  carregando: { type: Boolean, default: false },
})

const CORES_TIPO = {
  Segmento: 'bg-flow-500/15 text-flow-300',
  Faturamento: 'bg-signal-500/15 text-signal-400',
  Servico: 'bg-trust-500/15 text-trust-400',
}
</script>

<template>
  <section class="rounded-2xl border border-white/10 bg-ink-900/60 p-5 sm:p-6">
    <header class="mb-4">
      <h3 class="text-sm font-semibold text-white">Insights estratégicos</h3>
      <p class="mt-0.5 text-xs text-mist-500">Leitura da carteira gerada no último processamento</p>
    </header>

    <div v-if="carregando" class="space-y-3">
      <div v-for="n in 3" :key="n" class="h-14 animate-pulse rounded-lg bg-white/5" />
    </div>

    <p v-else-if="!insights.length" class="py-8 text-center text-sm text-mist-500">
      Nenhum insight gerado ainda. Envie uma planilha para o sistema analisar.
    </p>

    <ul v-else class="space-y-3">
      <li
        v-for="insight in insights"
        :key="insight.id"
        class="rounded-lg border border-white/5 bg-ink-950/50 p-4"
      >
        <span
          class="inline-block rounded-md px-2 py-0.5 text-[0.7rem] font-semibold uppercase tracking-wide"
          :class="CORES_TIPO[insight.tipo] ?? 'bg-white/5 text-mist-300'"
        >
          {{ insight.tipo }}
        </span>
        <p class="mt-2 text-sm leading-relaxed text-mist-200">{{ insight.resumo }}</p>
      </li>
    </ul>
  </section>
</template>
