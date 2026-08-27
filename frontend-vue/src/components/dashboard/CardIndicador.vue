<script setup>
import { computed } from 'vue'

/**
 * Cartão de indicador reaproveitado em toda a linha superior do dashboard
 * (faturamento, nº de clientes, nº de contratos). Só recebe props — quem busca
 * os dados é a view.
 */
const props = defineProps({
  rotulo: { type: String, required: true },
  valor: { type: [Number, String], required: true },
  formato: { type: String, default: 'numero' }, // numero | moeda | texto
  apoio: { type: String, default: '' },
  carregando: { type: Boolean, default: false },
})

const formatadores = {
  numero: new Intl.NumberFormat('pt-BR'),
  moeda: new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
    maximumFractionDigits: 0,
  }),
}

const valorFormatado = computed(() => {
  if (props.formato === 'texto') return props.valor
  const numero = Number(props.valor ?? 0)
  return formatadores[props.formato].format(Number.isFinite(numero) ? numero : 0)
})
</script>

<template>
  <div class="@container rounded-2xl border border-white/10 bg-ink-900/60 p-5">
    <p class="text-xs font-medium uppercase tracking-wide text-mist-500">{{ rotulo }}</p>

    <div v-if="carregando" class="mt-3 h-8 w-24 animate-pulse rounded bg-white/10" />
    <!--
      O corpo do valor acompanha a largura do proprio cartao, nao a da tela:
      "R$ 1.735.180.000" a 30px estoura um cartao de 1/4 de linha, e encolher
      pela viewport erraria de novo assim que a lateral do painel muda o espaco
      disponivel. Com cqi os quatro cartoes tem a mesma largura, entao continuam
      com o mesmo corpo entre si.
    -->
    <p
      v-else
      class="mt-2 font-semibold tabular-nums text-white text-[clamp(1.375rem,11cqi,1.875rem)]"
    >
      {{ valorFormatado }}
    </p>

    <p v-if="apoio" class="mt-1 text-xs text-mist-400">{{ apoio }}</p>
  </div>
</template>
