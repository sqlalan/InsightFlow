<script setup>
const steps = [
  {
    n: '01',
    title: 'Envie a planilha',
    text: 'Arraste seus arquivos .xlsx ou .xls. O InsightFlow identifica abas, cabeçalhos e tipos de coluna automaticamente.',
    detail: 'Até 500 mil linhas por arquivo',
  },
  {
    n: '02',
    title: 'Validação e limpeza',
    text: 'Duplicidades, células vazias, datas inconsistentes e valores fora do padrão são apontados antes de qualquer cálculo.',
    detail: 'Relatório de qualidade dos dados',
  },
  {
    n: '03',
    title: 'Análise automática',
    text: 'Indicadores, comparativos por período, tendências e desvios são gerados a partir do contexto da sua operação.',
    detail: 'Painéis prontos em segundos',
  },
  {
    n: '04',
    title: 'Decisão e compartilhamento',
    text: 'Exporte relatórios, agende envios recorrentes e compartilhe painéis com permissões por área.',
    detail: 'PDF, Excel e link controlado',
  },
]
</script>

<template>
  <section id="como-funciona" class="relative border-t border-white/8 py-24 lg:py-32">
    <div class="mx-auto max-w-7xl px-5 lg:px-8">
      <div class="max-w-2xl">
        <p class="text-xs font-semibold uppercase tracking-[0.2em] text-flow-400">Como funciona</p>
        <h2 class="mt-4 text-3xl font-semibold tracking-tight text-white sm:text-4xl">
          Da planilha bruta ao relatório confiável em quatro etapas
        </h2>
        <p class="mt-5 text-base leading-relaxed text-mist-400">
          Cada etapa é registrada e reversível. Você sempre sabe de onde veio cada número
          apresentado no painel.
        </p>
      </div>

      <ol class="mt-16 grid gap-6 md:grid-cols-2 lg:grid-cols-4">
        <li
          v-for="(step, i) in steps"
          :key="step.n"
          class="group relative rounded-2xl border border-white/8 bg-white/2 p-6 transition hover:border-flow-400/30 hover:bg-white/5"
        >
          <span
            class="inline-flex h-11 w-11 items-center justify-center rounded-xl border border-white/10 bg-ink-800 font-mono text-sm font-semibold text-flow-300 transition group-hover:border-flow-400/40"
          >
            {{ step.n }}
          </span>
          <h3 class="mt-5 text-base font-semibold text-white">{{ step.title }}</h3>
          <p class="mt-2.5 text-sm leading-relaxed text-mist-400">{{ step.text }}</p>
          <p class="mt-4 inline-flex rounded-md bg-flow-500/10 px-2.5 py-1 text-[11px] font-medium text-flow-300">
            {{ step.detail }}
          </p>

          <!-- conector entre passos -->
          <span
            v-if="i < steps.length - 1"
            class="pointer-events-none absolute right-0 top-11 hidden h-px w-6 translate-x-full bg-linear-to-r from-white/20 to-transparent lg:block"
            aria-hidden="true"
          />
        </li>
      </ol>
    </div>
  </section>
</template>
