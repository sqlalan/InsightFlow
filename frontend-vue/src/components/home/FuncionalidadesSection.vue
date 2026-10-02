<script setup>
// Sem link para as telas: a área interna só é acessada pelo Login
const funcionalidades = [
  {
    nome: 'Upload',
    titulo: 'Envio da planilha',
    texto: 'Selecione o arquivo .xlsx ou .xls, confira a prévia e os avisos de validação antes de enviar.',
  },
  {
    nome: 'Dashboard',
    titulo: 'Indicadores da carteira',
    texto: 'Cards e gráficos com clientes por segmento, nível, faixa de faturamento e evolução das contratações.',
  },
  {
    nome: 'Relatórios',
    titulo: 'Resumo das análises',
    texto: 'Registros lidos, campos vazios e duplicidades, histórico de processamento e exportação em CSV.',
  },
]
</script>

<template>
  <section id="funcionalidades" class="relative border-t border-white/8 py-16 lg:py-20">
    <div class="mx-auto max-w-7xl px-5 lg:px-8">
      <div class="max-w-2xl">
        <p class="text-xs font-semibold uppercase tracking-[0.2em] text-flow-400">Funcionalidades</p>
        <h2 class="mt-3 text-2xl font-semibold tracking-tight text-white sm:text-3xl">
          Tudo o que o CTI Insights faz com a sua planilha
        </h2>
      </div>

      <ul class="mt-10 grid gap-5 md:grid-cols-3">
        <li
          v-for="item in funcionalidades"
          :key="item.nome"
          class="rounded-2xl border border-white/8 bg-white/2 p-5 transition hover:border-flow-400/30 hover:bg-white/5"
        >
          <span
            class="inline-flex rounded-lg border border-white/10 bg-ink-800 px-3 py-1.5 text-xs font-semibold text-flow-300"
          >
            {{ item.nome }}
          </span>
          <h3 class="mt-4 text-base font-semibold text-white">{{ item.titulo }}</h3>
          <p class="mt-2 text-sm leading-relaxed text-mist-400">{{ item.texto }}</p>
        </li>
      </ul>
    </div>
  </section>
</template>
