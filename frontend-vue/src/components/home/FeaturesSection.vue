<script setup>
// Tres frentes, uma por etapa do que o sistema realmente faz: ler, tratar e
// analisar. O resto do que a pagina listava antes e detalhe de dentro do painel.
const features = [
  {
    title: 'Leitura nativa de Excel',
    text: 'Múltiplas abas, células mescladas e fórmulas interpretadas sem conversão manual.',
    icon: 'sheet',
  },
  {
    title: 'Qualidade de dados',
    text: 'Duplicidades, campos críticos vazios e um relatório de confiabilidade por arquivo.',
    icon: 'check',
  },
  {
    title: 'Indicadores e tendências',
    text: 'Estatísticas, comparativos e desvios calculados a partir das suas próprias colunas.',
    icon: 'trend',
  },
]

const paths = {
  sheet: [
    'M4 5.5A1.5 1.5 0 0 1 5.5 4h13A1.5 1.5 0 0 1 20 5.5v13a1.5 1.5 0 0 1-1.5 1.5h-13A1.5 1.5 0 0 1 4 18.5z',
    'M4 9.5h16M4 14.5h16M9.5 9.5V20M14.5 9.5V20',
  ],
  check: ['M12 3.2 19 6v5.4c0 4.1-2.8 7.4-7 8.8-4.2-1.4-7-4.7-7-8.8V6z', 'm9 12 2.2 2.2L15.4 10'],
  trend: ['M4 18.5 9.5 12l3.5 3.2L20 6.5', 'M15 6.5h5v5'],
}
</script>

<template>
  <section id="recursos" class="relative border-t border-white/8 py-16 lg:py-20">
    <div
      class="pointer-events-none absolute inset-0 bg-[radial-gradient(70%_50%_at_80%_0%,rgba(47,107,240,0.12),transparent_60%)]"
      aria-hidden="true"
    />

    <div class="relative mx-auto max-w-7xl px-5 lg:px-8">
      <div class="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div class="max-w-2xl">
          <p class="text-xs font-semibold uppercase tracking-[0.2em] text-flow-400">Recursos</p>
          <h2 class="mt-3 text-2xl font-semibold tracking-tight text-white sm:text-3xl">
            Tudo o que sua operação precisa extrair de uma planilha
          </h2>
        </div>
        <p class="max-w-sm text-sm leading-relaxed text-mist-400">
          Para times que já trabalham com Excel e não querem trocar de ferramenta.
        </p>
      </div>

      <div class="mt-10 grid gap-5 md:grid-cols-3">
        <article
          v-for="f in features"
          :key="f.title"
          class="group relative overflow-hidden rounded-2xl border border-white/8 bg-linear-to-b from-white/4.5 to-white/1.5 p-6 transition duration-300 hover:-translate-y-1 hover:border-flow-400/30"
        >
          <span
            class="pointer-events-none absolute inset-x-0 -top-px h-px bg-linear-to-r from-transparent via-flow-400/50 to-transparent opacity-0 transition group-hover:opacity-100"
            aria-hidden="true"
          />

          <span
            class="grid h-11 w-11 place-items-center rounded-xl bg-linear-to-br from-flow-500/25 to-signal-500/15 text-flow-300 ring-1 ring-white/10"
          >
            <svg viewBox="0 0 24 24" class="h-5 w-5" fill="none" stroke="currentColor" stroke-width="1.7">
              <path
                v-for="(d, i) in paths[f.icon]"
                :key="i"
                :d="d"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </span>

          <h3 class="mt-5 text-base font-semibold text-white">{{ f.title }}</h3>
          <p class="mt-2.5 max-w-md text-sm leading-relaxed text-mist-400">{{ f.text }}</p>
        </article>
      </div>
    </div>
  </section>
</template>
