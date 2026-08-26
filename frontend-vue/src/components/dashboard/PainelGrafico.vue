<script setup>
/** Moldura comum dos gráficos: título, estado vazio e altura responsiva. */
defineProps({
  titulo: { type: String, required: true },
  descricao: { type: String, default: '' },
  vazio: { type: Boolean, default: false },
  altura: { type: String, default: 'h-64 sm:h-72' },
})
</script>

<template>
  <section class="rounded-2xl border border-white/10 bg-ink-900/60 p-5 sm:p-6">
    <header class="mb-4">
      <h3 class="text-sm font-semibold text-white">{{ titulo }}</h3>
      <p v-if="descricao" class="mt-0.5 text-xs text-mist-500">{{ descricao }}</p>
    </header>

    <p v-if="vazio" class="py-10 text-center text-sm text-mist-500">
      Sem dados para este recorte. Envie uma planilha para começar.
    </p>
    <div v-else :class="altura">
      <slot />
    </div>
  </section>
</template>
