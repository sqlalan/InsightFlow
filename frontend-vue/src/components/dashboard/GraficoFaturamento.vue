<script setup>
import { computed } from 'vue'
import { Bar } from 'vue-chartjs'
import PainelGrafico from './PainelGrafico.vue'
import { CORES, opcoesBase, separar } from './grafico'

/** Quantos clientes caem em cada faixa de faturamento anual. */
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
        backgroundColor: CORES[2],
        hoverBackgroundColor: '#6ee7b7',
        borderRadius: 6,
        maxBarThickness: 28,
      },
    ],
  }
})

// Barras horizontais: os rótulos das faixas são longos demais para o eixo X.
const opcoes = opcoesBase({
  raiz: { indexAxis: 'y' },
  scales: {
    x: {
      beginAtZero: true,
      ticks: { color: '#8496b3', font: { size: 11 }, precision: 0 },
      grid: { color: 'rgba(255, 255, 255, 0.07)' },
      border: { display: false },
    },
    y: {
      ticks: { color: '#8496b3', font: { size: 11 } },
      grid: { display: false },
      border: { color: 'rgba(255, 255, 255, 0.07)' },
    },
  },
})
</script>

<template>
  <PainelGrafico
    titulo="Clientes por faixa de faturamento"
    descricao="Porte das empresas atendidas"
    :vazio="!dados.length"
  >
    <Bar :data="grafico" :options="opcoes" />
  </PainelGrafico>
</template>
