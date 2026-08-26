<script setup>
import { computed } from 'vue'
import { Doughnut } from 'vue-chartjs'
import PainelGrafico from './PainelGrafico.vue'
import { CORES, opcoesBase, separar } from './grafico'

/** Proporção da carteira em cada nível A/B/C. */
const props = defineProps({
  dados: { type: Array, default: () => [] },
})

const grafico = computed(() => {
  const { rotulos, valores } = separar(props.dados)
  return {
    labels: rotulos.map((nivel) => `Nível ${nivel}`),
    datasets: [
      {
        data: valores,
        backgroundColor: CORES,
        borderColor: '#080d19',
        borderWidth: 3,
        hoverOffset: 6,
      },
    ],
  }
})

const opcoes = opcoesBase({
  raiz: { cutout: '62%' },
  plugins: {
    legend: {
      display: true,
      position: 'bottom',
      labels: { color: '#b3c1d6', boxWidth: 10, boxHeight: 10, padding: 16, usePointStyle: true },
    },
  },
})
</script>

<template>
  <PainelGrafico
    titulo="Distribuição por nível"
    descricao="Classificação A, B e C da carteira"
    :vazio="!dados.length"
  >
    <Doughnut :data="grafico" :options="opcoes" />
  </PainelGrafico>
</template>
