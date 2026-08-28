<script setup>
import { computed } from 'vue'
import { Bar } from 'vue-chartjs'
import PainelGrafico from './PainelGrafico.vue'
import { CORES, escalasPadrao, opcoesBase, separar } from './grafico'

/** Distribuição de clientes por segmento de atuação. */
const props = defineProps({
  dados: { type: Array, default: () => [] },
})

const grafico = computed(() => {
  const { rotulos, valores } = separar(props.dados)
  return {
    labels: rotulos,
    datasets: [
      {
        label: 'Clientes',
        data: valores,
        backgroundColor: CORES[0],
        hoverBackgroundColor: '#7dd8f0',
        borderRadius: 6,
        maxBarThickness: 42,
      },
    ],
  }
})

const opcoes = opcoesBase({ scales: escalasPadrao({ rotacao: 25 }) })
</script>

<template>
  <PainelGrafico
    titulo="Clientes por segmento"
    descricao="Onde a carteira está concentrada"
    :vazio="!dados.length"
  >
    <Bar :data="grafico" :options="opcoes" />
  </PainelGrafico>
</template>
