<script setup>
import { RouterLink } from 'vue-router'

const kpis = [
  { label: 'Receita', value: 'R$ 1,84 mi', delta: '+12,4%', up: true },
  { label: 'Margem', value: '31,2%', delta: '+2,1 p.p.', up: true },
  { label: 'Ruptura', value: '3,7%', delta: '-1,8 p.p.', up: false },
]

// série do gráfico do mock (altura relativa em %)
const bars = [38, 52, 45, 63, 58, 74, 69, 88, 81, 96]
</script>

<template>
  <section class="relative overflow-hidden pt-32 pb-20 lg:pt-40 lg:pb-28">
    <!-- fundo -->
    <div
      class="pointer-events-none absolute inset-0 opacity-60 bg-[linear-gradient(to_right,rgba(148,176,214,0.07)_1px,transparent_1px),linear-gradient(to_bottom,rgba(148,176,214,0.07)_1px,transparent_1px)] bg-size-[56px_56px]"
      aria-hidden="true"
    />
    <div
      class="pointer-events-none absolute inset-0 bg-[radial-gradient(120%_80%_at_50%_-10%,rgba(18,168,212,0.22),transparent_60%)]"
      aria-hidden="true"
    />
    <div
      class="pointer-events-none absolute -left-40 top-40 h-96 w-96 rounded-full bg-signal-500/15 blur-[120px]"
      aria-hidden="true"
    />
    <div
      class="pointer-events-none absolute inset-x-0 bottom-0 h-40 bg-linear-to-b from-transparent to-ink-950"
      aria-hidden="true"
    />

    <div class="relative mx-auto grid max-w-7xl items-center gap-16 px-5 lg:grid-cols-2 lg:px-8">
      <!-- coluna de texto -->
      <div class="animate-rise">
        <span
          class="inline-flex items-center gap-2 rounded-full border border-flow-400/25 bg-flow-500/10 px-3.5 py-1.5 text-xs font-medium text-flow-300"
        >
          <span class="h-1.5 w-1.5 rounded-full bg-flow-400" />
          Análise de planilhas Excel com governança de ponta a ponta
        </span>

        <h1 class="mt-6 text-4xl font-semibold leading-[1.08] tracking-tight text-white sm:text-5xl lg:text-6xl">
          Suas planilhas viram
          <span
            class="bg-linear-to-r from-flow-300 via-flow-400 to-signal-400 bg-clip-text text-transparent"
            >decisões de operação</span
          >
          em minutos.
        </h1>

        <p class="mt-6 max-w-xl text-lg leading-relaxed text-mist-300">
          O InsightFlow lê seus arquivos <span class="font-medium text-mist-100">.xlsx</span> e
          <span class="font-medium text-mist-100">.xls</span>, valida a consistência dos dados e
          entrega indicadores, tendências e alertas prontos — sem macro, sem retrabalho e sem
          expor informação sensível.
        </p>

        <div class="mt-9 flex flex-col gap-3 sm:flex-row">
          <RouterLink
            to="/login"
            class="group inline-flex items-center justify-center gap-2 rounded-xl bg-linear-to-r from-flow-400 to-signal-500 px-6 py-3.5 text-sm font-semibold text-white shadow-xl shadow-flow-500/25 transition hover:shadow-flow-500/40"
          >
            Começar análise
            <svg viewBox="0 0 20 20" class="h-4 w-4 transition group-hover:translate-x-0.5" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M4 10h11m0 0-4.5-4.5M15 10l-4.5 4.5" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </RouterLink>
          <a
            href="#como-funciona"
            class="inline-flex items-center justify-center gap-2 rounded-xl border border-white/12 bg-white/5 px-6 py-3.5 text-sm font-semibold text-mist-100 backdrop-blur transition hover:border-white/25 hover:bg-white/10"
          >
            Ver como funciona
          </a>
        </div>

        <dl class="mt-12 grid max-w-lg grid-cols-3 gap-6 border-t border-white/10 pt-8">
          <div v-for="stat in [
            { k: '< 60s', v: 'para o primeiro relatório' },
            { k: '500 mil', v: 'linhas por arquivo' },
            { k: '100%', v: 'do processamento auditado' },
          ]" :key="stat.k">
            <dt class="text-2xl font-semibold text-white">{{ stat.k }}</dt>
            <dd class="mt-1 text-xs leading-relaxed text-mist-400">{{ stat.v }}</dd>
          </div>
        </dl>
      </div>

      <!-- mock do produto -->
      <div class="relative animate-rise [animation-delay:120ms]">
        <div
          class="pointer-events-none absolute -inset-6 rounded-4xl bg-linear-to-tr from-flow-500/20 via-transparent to-signal-500/20 blur-2xl"
          aria-hidden="true"
        />

        <div
          class="relative rounded-2xl border border-white/10 bg-white/3.5 p-2 shadow-2xl shadow-black/60 backdrop-blur-xl"
        >
          <div class="rounded-xl bg-ink-900/90 ring-1 ring-white/5">
            <!-- barra da janela -->
            <div class="flex items-center gap-2 border-b border-white/8 px-4 py-3">
              <span class="h-2.5 w-2.5 rounded-full bg-white/15" />
              <span class="h-2.5 w-2.5 rounded-full bg-white/15" />
              <span class="h-2.5 w-2.5 rounded-full bg-white/15" />
              <span class="ml-3 text-xs font-medium text-mist-400">Painel de operações</span>
              <span class="ml-auto inline-flex items-center gap-1.5 rounded-full bg-trust-500/12 px-2.5 py-1 text-[11px] font-medium text-trust-400">
                <span class="h-1.5 w-1.5 rounded-full bg-trust-400" /> Sincronizado
              </span>
            </div>

            <div class="space-y-4 p-4">
              <!-- arquivo processado -->
              <div class="flex items-center gap-3 rounded-lg border border-white/8 bg-white/3 p-3">
                <span class="grid h-9 w-9 shrink-0 place-items-center rounded-lg bg-trust-500/15 text-trust-400">
                  <svg viewBox="0 0 24 24" class="h-4.5 w-4.5" fill="none" stroke="currentColor" stroke-width="1.8">
                    <path d="M14 3v5h5" stroke-linecap="round" stroke-linejoin="round" />
                    <path d="M19 8.5V20a1 1 0 0 1-1 1H6a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1h8z" stroke-linejoin="round" />
                    <path d="m9.5 12 5 5m0-5-5 5" stroke-linecap="round" />
                  </svg>
                </span>
                <div class="min-w-0 flex-1">
                  <p class="truncate text-xs font-medium text-mist-100">operacao_q1_2026.xlsx</p>
                  <p class="mt-0.5 text-[11px] text-mist-500">128.430 linhas · 6 abas · validado</p>
                </div>
                <span class="text-[11px] font-semibold text-trust-400">100%</span>
              </div>

              <!-- kpis -->
              <div class="grid grid-cols-3 gap-3">
                <div
                  v-for="kpi in kpis"
                  :key="kpi.label"
                  class="rounded-lg border border-white/8 bg-white/3 p-3"
                >
                  <p class="text-[11px] text-mist-500">{{ kpi.label }}</p>
                  <p class="mt-1.5 text-sm font-semibold text-white">{{ kpi.value }}</p>
                  <p
                    class="mt-1 text-[11px] font-medium"
                    :class="kpi.up ? 'text-trust-400' : 'text-flow-300'"
                  >
                    {{ kpi.delta }}
                  </p>
                </div>
              </div>

              <!-- gráfico -->
              <div class="rounded-lg border border-white/8 bg-white/3 p-4">
                <div class="mb-3 flex items-center justify-between">
                  <p class="text-[11px] font-medium text-mist-300">Volume processado por semana</p>
                  <p class="text-[11px] text-mist-500">últimas 10</p>
                </div>
                <div class="flex h-24 items-end gap-1.5" role="img" aria-label="Gráfico de barras com tendência de crescimento">
                  <div
                    v-for="(h, i) in bars"
                    :key="i"
                    class="flex-1 rounded-t bg-linear-to-t from-flow-600/40 to-flow-400"
                    :style="{ height: h + '%' }"
                  />
                </div>
              </div>

              <!-- insight -->
              <div class="flex gap-3 rounded-lg border border-flow-400/20 bg-flow-500/8 p-3">
                <svg viewBox="0 0 24 24" class="h-4.5 w-4.5 shrink-0 text-flow-300" fill="none" stroke="currentColor" stroke-width="1.8">
                  <path d="M12 3a6 6 0 0 0-3.5 10.9V16h7v-2.1A6 6 0 0 0 12 3Z" stroke-linejoin="round" />
                  <path d="M9.5 19h5" stroke-linecap="round" />
                </svg>
                <p class="text-[11px] leading-relaxed text-mist-200">
                  <span class="font-semibold text-white">Insight:</span> a filial Sul concentra
                  <span class="font-medium text-flow-300">62%</span> das divergências de estoque —
                  revise o processo de contagem cíclica.
                </p>
              </div>
            </div>
          </div>
        </div>

        <!-- selo flutuante -->
        <div
          class="absolute -bottom-5 -left-4 hidden animate-float items-center gap-2.5 rounded-xl border border-white/10 bg-white/3.5 px-4 py-3 shadow-xl shadow-black/40 backdrop-blur-xl motion-reduce:animate-none sm:flex"
        >
          <span class="grid h-8 w-8 place-items-center rounded-lg bg-trust-500/15 text-trust-400">
            <svg viewBox="0 0 24 24" class="h-4 w-4" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 3l7 3v5.5c0 4.2-2.9 7.6-7 9-4.1-1.4-7-4.8-7-9V6z" stroke-linejoin="round" />
              <path d="m9.2 12 2 2 3.6-3.8" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </span>
          <div>
            <p class="text-[11px] font-semibold text-white">Dados criptografados</p>
            <p class="text-[10px] text-mist-400">em trânsito e em repouso</p>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
