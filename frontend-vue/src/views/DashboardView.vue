<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import PainelNav from '../components/layout/PainelNav.vue'
import CardIndicador from '../components/dashboard/CardIndicador.vue'
import GraficoEvolucao from '../components/dashboard/GraficoEvolucao.vue'
import GraficoFaturamento from '../components/dashboard/GraficoFaturamento.vue'
import GraficoNivel from '../components/dashboard/GraficoNivel.vue'
import GraficoSegmento from '../components/dashboard/GraficoSegmento.vue'
import ListaInsights from '../components/dashboard/ListaInsights.vue'
import TabelaClientes from '../components/dashboard/TabelaClientes.vue'
import { buscarIndicadores, buscarInsights, listarClientes } from '../services/api'
import {
  clientesSimulados,
  indicadoresSimulados,
  insightsSimulados,
} from '../services/dadosSimulados'

const indicadores = ref(null)
const insights = ref([])
const clientes = ref([])
const carregando = ref(true)
const erro = ref('')
const simulado = ref(false)

const ticketMedio = computed(() => {
  const total = indicadores.value?.totalContratos ?? 0
  return total ? (indicadores.value.faturamentoTotal ?? 0) / total : 0
})

/** Uma única função de carga: usada na montagem e depois de editar a carteira. */
async function carregar() {
  carregando.value = true
  erro.value = ''
  simulado.value = false
  try {
    const [dadosIndicadores, dadosInsights, dadosClientes] = await Promise.all([
      buscarIndicadores(),
      buscarInsights(),
      listarClientes(),
    ])
    indicadores.value = dadosIndicadores
    insights.value = dadosInsights
    clientes.value = dadosClientes
  } catch (falha) {
    // status 0 = servidor fora do ar: mostra os dados simulados em vez de tela vazia
    if (falha.status === 0) {
      indicadores.value = indicadoresSimulados
      insights.value = insightsSimulados
      clientes.value = clientesSimulados
      simulado.value = true
    } else {
      erro.value = falha.message
    }
  } finally {
    carregando.value = false
  }
}

onMounted(carregar)
</script>

<template>
  <div class="min-h-dvh bg-ink-950 lg:pl-64">
    <PainelNav />

    <main class="mx-auto max-w-7xl px-5 py-10 lg:px-8">
      <div class="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h1 class="text-2xl font-semibold text-white sm:text-3xl">Dashboard da carteira</h1>
          <p class="mt-2 text-sm text-mist-400">
            Indicadores da carteira de clientes da CTI.
          </p>
        </div>
        <button
          type="button"
          :aria-busy="carregando"
          class="self-start rounded-lg border border-white/10 px-4 py-2 text-sm font-medium text-mist-200 transition hover:bg-white/5 hover:text-white sm:self-auto"
          :disabled="carregando"
          @click="carregar"
        >
          {{ carregando ? 'Atualizando...' : 'Atualizar' }}
        </button>
      </div>

      <div
        v-if="simulado"
        role="status"
        class="mt-6 rounded-xl border border-signal-500/30 bg-signal-500/10 px-5 py-4"
      >
        <p class="text-sm font-medium text-signal-400">Exibindo dados simulados</p>
        <p class="mt-1 text-xs text-mist-400">
          A API não está no ar. Os números abaixo são fictícios, só para demonstrar a tela.
        </p>
      </div>

      <div
        v-if="erro"
        role="alert"
        class="mt-6 rounded-xl border border-red-500/30 bg-red-500/10 px-5 py-4"
      >
        <p class="text-sm font-medium text-red-200">{{ erro }}</p>
        <p class="mt-1 text-xs text-red-200/70">
          Clique em Atualizar para tentar de novo.
        </p>
      </div>

      <!-- Quatro colunas so a partir de xl: com a lateral de 256px ocupando a
           esquerda, abaixo disso o cartao fica estreito demais. -->
      <section class="mt-8 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <CardIndicador
          rotulo="Clientes na carteira"
          :valor="indicadores?.totalClientes ?? 0"
          :carregando="carregando"
        />
        <CardIndicador
          rotulo="Contratos"
          :valor="indicadores?.totalContratos ?? 0"
          :carregando="carregando"
        />
        <CardIndicador
          rotulo="Faturamento total"
          formato="moeda"
          :valor="indicadores?.faturamentoTotal ?? 0"
          :carregando="carregando"
        />
        <CardIndicador
          rotulo="Faturamento médio"
          formato="moeda"
          :valor="indicadores?.faturamentoMedio ?? 0"
          :apoio="`Ticket por contrato: R$ ${Math.round(ticketMedio).toLocaleString('pt-BR')}`"
          :carregando="carregando"
        />
      </section>

      <section class="mt-6 grid gap-4 lg:grid-cols-2">
        <GraficoSegmento :dados="indicadores?.clientesPorSegmento ?? []" />
        <GraficoNivel :dados="indicadores?.clientesPorNivel ?? []" />
        <GraficoFaturamento :dados="indicadores?.clientesPorFaixaFaturamento ?? []" />
        <GraficoEvolucao :dados="indicadores?.evolucaoContratacoes ?? []" />
      </section>

      <div class="mt-6 grid gap-4 lg:grid-cols-3">
        <div class="lg:col-span-2">
          <TabelaClientes :clientes="clientes" :carregando="carregando" @alterado="carregar" />
        </div>
        <ListaInsights :insights="insights" :carregando="carregando" />
      </div>

      <p class="mt-8 text-center text-sm text-mist-500">
        Precisa atualizar a base?
        <RouterLink to="/upload" class="text-flow-300 underline">Envie uma nova planilha</RouterLink>.
      </p>
    </main>
  </div>
</template>
