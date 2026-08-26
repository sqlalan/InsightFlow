<script setup>
import { computed } from 'vue'
import { Line } from 'vue-chartjs'
import PainelGrafico from './PainelGrafico.vue'
import { escalasPadrao, opcoesBase, separar } from './grafico'

/** Série temporal: contratações fechadas mês a mês. */
const props = defineProps({
  dados: { type: Array, default: () => [] },
})

const grafico = computed(() => {
  const { rotulos, valores } = separar(props.dados)
  return {
    labels: rotulos,
    datasets: [
      {
        label: 'Contratações',
        data: valores,
        borderColor: '#4f8cff',
        backgroundColor: 'rgba(79, 140, 255, 0.18)',
        pointBackgroundColor: '#4f8cff',
        pointRadius: 3,
        pointHoverRadius: 5,
        borderWidth: 2,
        tension: 0.35,
        fill: true,
      },
    ],
  }
})

const opcoes = opcoesBase({ scales: escalasPadrao({ rotacao: 45 }) })
</script>

<template>
  <PainelGrafico
    titulo="Evolução das contratações"
    descricao="Contratos iniciados por mês"
    :vazio="!dados.length"
  >
    <Line :data="grafico" :options="opcoes" />
  </PainelGrafico>
</template>
