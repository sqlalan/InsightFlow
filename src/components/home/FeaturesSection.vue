<script setup>
const features = [
  {
    title: 'Leitura nativa de Excel',
    text: 'Múltiplas abas, células mescladas, fórmulas e tabelas dinâmicas interpretadas sem conversão manual.',
    icon: 'sheet',
    span: true,
  },
  {
    title: 'Qualidade de dados',
    text: 'Score de confiabilidade por arquivo, com apontamento de duplicidades e campos críticos vazios.',
    icon: 'check',
  },
  {
    title: 'Indicadores de operação',
    text: 'Produtividade, ruptura, custo por unidade e SLA calculados a partir das suas próprias colunas.',
    icon: 'gauge',
  },
  {
    title: 'Comparativos e tendências',
    text: 'Mês a mês, filial a filial, turno a turno. Desvios relevantes são destacados automaticamente.',
    icon: 'trend',
  },
  {
    title: 'Alertas por regra',
    text: 'Defina limites e receba avisos quando um indicador sair da faixa esperada.',
    icon: 'bell',
  },
  {
    title: 'Histórico versionado',
    text: 'Cada envio fica registrado com autor, data e resultado — auditoria completa da operação.',
    icon: 'history',
  },
]

const paths = {
  sheet: [
    'M4 5.5A1.5 1.5 0 0 1 5.5 4h13A1.5 1.5 0 0 1 20 5.5v13a1.5 1.5 0 0 1-1.5 1.5h-13A1.5 1.5 0 0 1 4 18.5z',
    'M4 9.5h16M4 14.5h16M9.5 9.5V20M14.5 9.5V20',
  ],
  check: ['M12 3.2 19 6v5.4c0 4.1-2.8 7.4-7 8.8-4.2-1.4-7-4.7-7-8.8V6z', 'm9 12 2.2 2.2L15.4 10'],
  gauge: ['M5 17a7.5 7.5 0 1 1 14 0', 'M12 17l3.5-4.5'],
  trend: ['M4 18.5 9.5 12l3.5 3.2L20 6.5', 'M15 6.5h5v5'],
  bell: ['M6.5 10a5.5 5.5 0 0 1 11 0c0 4 1.5 5.5 1.5 5.5H5S6.5 14 6.5 10Z', 'M10 19a2.2 2.2 0 0 0 4 0'],
  history: ['M4 12a8 8 0 1 0 2.6-5.9', 'M4 4.5V9h4.5', 'M12 8v4.4l3 1.8'],
}
</script>

<template>
  <section id="recursos" class="relative border-t border-white/8 py-24 lg:py-32">
    <div
      class="pointer-events-none absolute inset-0 bg-[radial-gradient(70%_50%_at_80%_0%,rgba(47,107,240,0.12),transparent_60%)]"
      aria-hidden="true"
    />

    <div class="relative mx-auto max-w-7xl px-5 lg:px-8">
      <div class="flex flex-col gap-6 md:flex-row md:items-end md:justify-between">
        <div class="max-w-2xl">
          <p class="text-xs font-semibold uppercase tracking-[0.2em] text-flow-400">Recursos</p>
          <h2 class="mt-4 text-3xl font-semibold tracking-tight text-white sm:text-4xl">
            Tudo o que sua operação precisa extrair de uma planilha
          </h2>
        </div>
        <p class="max-w-sm text-sm leading-relaxed text-mist-400">
          Construído para times de logística, suprimentos, financeiro e produção que já
          trabalham com Excel e não querem trocar de ferramenta.
        </p>
      </div>

      <div class="mt-16 grid gap-5 md:grid-cols-2 lg:grid-cols-3">
        <article
          v-for="f in features"
          :key="f.title"
          class="group relative overflow-hidden rounded-2xl border border-white/8 bg-linear-to-b from-white/4.5 to-white/1.5 p-7 transition duration-300 hover:-translate-y-1 hover:border-flow-400/30"
          :class="f.span ? 'lg:col-span-2' : ''"
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

          <h3 class="mt-6 text-base font-semibold text-white">{{ f.title }}</h3>
          <p class="mt-2.5 max-w-md text-sm leading-relaxed text-mist-400">{{ f.text }}</p>
        </article>
      </div>
    </div>
  </section>
</template>
