<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import PainelNav from '../components/layout/PainelNav.vue'
import ListaInsights from '../components/dashboard/ListaInsights.vue'
import { buscarInsights, buscarTelemetria, listarClientes } from '../services/api'

const insights = ref([])
const eventos = ref([])
const clientes = ref([])
const carregando = ref(true)
const erro = ref('')

/** Uma única carga: usada na montagem e no botão de atualizar. */
async function carregar() {
  carregando.value = true
  erro.value = ''
  try {
    const [dadosInsights, dadosEventos, dadosClientes] = await Promise.all([
      buscarInsights(),
      buscarTelemetria(),
      listarClientes(),
    ])
    insights.value = dadosInsights
    eventos.value = dadosEventos
    clientes.value = dadosClientes
  } catch (falha) {
    erro.value = falha.message
  } finally {
    carregando.value = false
  }
}

onMounted(carregar)

const ultimoProcessamento = computed(() => {
  const analise = eventos.value.find((e) => e.evento === 'ANALISE_PYTHON' && e.status === 'OK')
  return analise ? formatarData(analise.timestamp) : '—'
})

const falhas = computed(() => eventos.value.filter((e) => e.status !== 'OK').length)

// A API grava o evento como código; a tabela mostra o nome para o usuário.
const NOMES_EVENTO = {
  UPLOAD: 'Envio da planilha',
  ANALISE_PYTHON: 'Análise dos dados',
}

function nomeDoEvento(codigo) {
  return NOMES_EVENTO[codigo] ?? codigo
}

function formatarData(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })
}

function formatarDuracao(ms) {
  if (ms == null) return '—'
  return ms < 1000 ? `${ms} ms` : `${(ms / 1000).toFixed(1)} s`
}

/**
 * Exporta a carteira tratada em CSV.
 *
 * Separador ponto e vírgula e BOM no início: é o que faz o Excel em português
 * abrir o arquivo com as colunas separadas, em vez de jogar tudo na coluna A.
 */
function exportarCsv() {
  const cabecalho = [
    'Código do cliente',
    'Segmento',
    'Nível',
    'Faturamento anual',
    'Faixa de faturamento',
    'Consultor',
    'Contratos ativos',
  ]

  const linhas = clientes.value.map((c) =>
    [
      c.codigoCti,
      c.segmento,
      c.nivel,
      c.faturamentoAnual ?? '',
      c.faixaFaturamento,
      c.consultor ?? '',
      c.contratosAtivos,
    ]
      .map((campo) => `"${String(campo ?? '').replace(/"/g, '""')}"`)
      .join(';'),
  )

  const conteudo = '﻿' + [cabecalho.join(';'), ...linhas].join('\r\n')
  const url = URL.createObjectURL(new Blob([conteudo], { type: 'text/csv;charset=utf-8;' }))

  const link = document.createElement('a')
  link.href = url
  link.download = `relatorio-clientes-${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
  URL.revokeObjectURL(url)
}
</script>

<template>
  <div class="min-h-dvh bg-ink-950 lg:pl-64">
    <PainelNav />

    <main class="mx-auto max-w-7xl px-5 py-10 lg:px-8">
      <div class="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h1 class="text-2xl font-semibold text-white sm:text-3xl">Relatórios</h1>
          <p class="mt-2 text-sm text-mist-400">
            Insights gerados, histórico de processamento e exportação da carteira.
          </p>
        </div>
        <button
          type="button"
          class="self-start rounded-lg border border-white/10 px-4 py-2 text-sm font-medium text-mist-200 transition hover:bg-white/5 hover:text-white sm:self-auto"
          :disabled="carregando"
          @click="carregar"
        >
          Atualizar
        </button>
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

      <section class="mt-8 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <div class="rounded-2xl border border-white/10 bg-ink-900/60 p-5">
          <p class="text-xs font-medium uppercase tracking-wide text-mist-500">Insights gerados</p>
          <p class="mt-2 text-2xl font-semibold tabular-nums text-white sm:text-3xl">
            {{ insights.length }}
          </p>
        </div>
        <div class="rounded-2xl border border-white/10 bg-ink-900/60 p-5">
          <p class="text-xs font-medium uppercase tracking-wide text-mist-500">
            Clientes na carteira
          </p>
          <p class="mt-2 text-2xl font-semibold tabular-nums text-white sm:text-3xl">
            {{ clientes.length }}
          </p>
        </div>
        <div class="rounded-2xl border border-white/10 bg-ink-900/60 p-5">
          <p class="text-xs font-medium uppercase tracking-wide text-mist-500">
            Último processamento
          </p>
          <p class="mt-2 text-lg font-semibold text-white">{{ ultimoProcessamento }}</p>
        </div>
        <div class="rounded-2xl border border-white/10 bg-ink-900/60 p-5">
          <p class="text-xs font-medium uppercase tracking-wide text-mist-500">Execuções com erro</p>
          <p
            class="mt-2 text-2xl font-semibold tabular-nums sm:text-3xl"
            :class="falhas > 0 ? 'text-red-300' : 'text-white'"
          >
            {{ falhas }}
          </p>
        </div>
      </section>

      <!-- items-start: o histórico tem poucas linhas e não deve esticar só para
           acompanhar a altura da coluna de insights. -->
      <div class="mt-6 grid items-start gap-4 lg:grid-cols-3">
        <!-- Histórico vem da classe Telemetria: cada upload e cada execução do
             módulo Python deixam registro, com duração e status. -->
        <section
          class="rounded-2xl border border-white/10 bg-ink-900/60 p-5 sm:p-6 lg:col-span-2"
        >
          <header class="mb-4 flex items-end justify-between gap-4">
            <div>
              <h2 class="text-sm font-semibold text-white">Histórico de processamento</h2>
              <p class="mt-0.5 text-xs text-mist-500">
                Registro de cada envio de planilha e análise dos dados
              </p>
            </div>
            <button
              type="button"
              class="shrink-0 rounded-lg bg-white px-3 py-2 text-xs font-semibold text-ink-900 transition hover:bg-mist-100 disabled:cursor-not-allowed disabled:opacity-40"
              :disabled="!clientes.length"
              @click="exportarCsv"
            >
              Exportar carteira (CSV)
            </button>
          </header>

          <div v-if="carregando" class="space-y-3">
            <div v-for="n in 4" :key="n" class="h-12 animate-pulse rounded-lg bg-white/5" />
          </div>

          <p v-else-if="!eventos.length" class="py-8 text-center text-sm text-mist-500">
            Nenhum processamento registrado ainda. Envie uma planilha para começar.
          </p>

          <div v-else class="overflow-x-auto">
            <table class="w-full min-w-xl text-left text-sm">
              <thead>
                <tr class="border-b border-white/10 text-xs uppercase tracking-wide text-mist-500">
                  <th class="py-2 pr-4 font-medium">Evento</th>
                  <th class="py-2 pr-4 font-medium">Status</th>
                  <th class="py-2 pr-4 font-medium">Detalhe</th>
                  <th class="py-2 pr-4 font-medium">Duração</th>
                  <th class="py-2 font-medium">Quando</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="evento in eventos"
                  :key="evento.id"
                  class="border-b border-white/5 text-mist-300"
                >
                  <td class="py-2.5 pr-4 font-medium text-mist-100">{{ nomeDoEvento(evento.evento) }}</td>
                  <td class="py-2.5 pr-4">
                    <span
                      class="rounded-md px-2 py-0.5 text-xs font-semibold"
                      :class="
                        evento.status === 'OK'
                          ? 'bg-trust-500/15 text-trust-400'
                          : 'bg-red-500/15 text-red-300'
                      "
                    >
                      {{ evento.status === 'OK' ? 'Concluído' : 'Falhou' }}
                    </span>
                  </td>
                  <td class="py-2.5 pr-4">{{ evento.detalhe || '—' }}</td>
                  <td class="py-2.5 pr-4 tabular-nums">{{ formatarDuracao(evento.duracaoMs) }}</td>
                  <td class="py-2.5 tabular-nums">{{ formatarData(evento.timestamp) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <ListaInsights :insights="insights" :carregando="carregando" />
      </div>

      <p class="mt-8 text-center text-sm text-mist-500">
        Precisa atualizar a base?
        <RouterLink to="/upload" class="text-flow-300 underline">Envie uma nova planilha</RouterLink>.
      </p>
    </main>
  </div>
</template>
