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

// O faturamento da carteira chega na casa dos bilhoes e nao cabe por extenso
// num cartao que ocupa 1/4 da linha. Acima de um milhao o cartao mostra a forma
// curta e guarda o valor exato no title, que aparece ao passar o mouse.
const UNIDADES = [
  { limite: 1_000_000_000, sufixo: 'bi' },
  { limite: 1_000_000, sufixo: 'mi' },
]

function abreviarMoeda(numero) {
  const unidade = UNIDADES.find((u) => Math.abs(numero) >= u.limite)
  if (!unidade) return formatadores.moeda.format(numero)

  const reduzido = (numero / unidade.limite).toLocaleString('pt-BR', {
    maximumFractionDigits: 2,
  })
  return `R$ ${reduzido} ${unidade.sufixo}`
}

/** Numero saneado: props podem chegar como string ou nulas vindas da API. */
const numero = computed(() => {
  const bruto = Number(props.valor ?? 0)
  return Number.isFinite(bruto) ? bruto : 0
})

const valorFormatado = computed(() => {
  if (props.formato === 'texto') return props.valor
  if (props.formato === 'moeda') return abreviarMoeda(numero.value)
  return formatadores.numero.format(numero.value)
})

/** Só existe quando o valor foi abreviado — vira o tooltip do cartao. */
const valorExato = computed(() => {
  if (props.formato !== 'moeda') return null
  const exato = formatadores.moeda.format(numero.value)
  return exato === valorFormatado.value ? null : exato
})
</script>

<template>
  <div class="rounded-2xl border border-white/10 bg-ink-900/60 p-5">
    <p class="text-xs font-medium uppercase tracking-wide text-mist-500">{{ rotulo }}</p>

    <div v-if="carregando" class="mt-3 h-8 w-24 animate-pulse rounded bg-white/10" />
    <p
      v-else
      class="mt-2 text-2xl font-semibold tabular-nums text-white sm:text-3xl"
      :title="valorExato"
    >
      {{ valorFormatado }}
    </p>

    <p v-if="apoio" class="mt-1 text-xs text-mist-400">{{ apoio }}</p>
  </div>
</template>
