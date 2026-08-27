<script setup>
const steps = [
  {
    n: '01',
    title: 'Envie a planilha',
    text: 'Abas, cabeçalhos e tipos de coluna são identificados automaticamente.',
  },
  {
    n: '02',
    title: 'Validação e limpeza',
    text: 'Duplicidades, campos vazios e valores fora do padrão apontados antes do cálculo.',
  },
  {
    n: '03',
    title: 'Análise automática',
    text: 'Indicadores, tendências e desvios gerados a partir da sua operação.',
  },
  {
    n: '04',
    title: 'Decisão',
    text: 'Painéis prontos para exportar e compartilhar com permissão por área.',
  },
]
</script>

<template>
  <section id="como-funciona" class="relative border-t border-white/8 py-16 lg:py-20">
    <div class="mx-auto max-w-7xl px-5 lg:px-8">
      <div class="max-w-2xl">
        <p class="text-xs font-semibold uppercase tracking-[0.2em] text-flow-400">Como funciona</p>
        <h2 class="mt-3 text-2xl font-semibold tracking-tight text-white sm:text-3xl">
          Da planilha bruta ao relatório confiável em quatro etapas
        </h2>
      </div>

      <ol class="mt-10 grid gap-5 md:grid-cols-2 lg:grid-cols-4">
        <li
          v-for="(step, i) in steps"
          :key="step.n"
          class="group relative rounded-2xl border border-white/8 bg-white/2 p-5 transition hover:border-flow-400/30 hover:bg-white/5"
        >
          <span
            class="inline-flex h-10 w-10 items-center justify-center rounded-xl border border-white/10 bg-ink-800 font-mono text-sm font-semibold text-flow-300 transition group-hover:border-flow-400/40"
          >
            {{ step.n }}
          </span>
          <h3 class="mt-4 text-base font-semibold text-white">{{ step.title }}</h3>
          <p class="mt-2 text-sm leading-relaxed text-mist-400">{{ step.text }}</p>

          <!-- conector entre passos -->
          <span
            v-if="i < steps.length - 1"
            class="pointer-events-none absolute right-0 top-10 hidden h-px w-5 translate-x-full bg-linear-to-r from-white/20 to-transparent lg:block"
            aria-hidden="true"
          />
        </li>
      </ol>
    </div>
  </section>
</template>
