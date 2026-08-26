<script setup>
defineProps({
  wordmark: { type: Boolean, default: true },
  size: { type: String, default: 'md' }, // sm | md | lg
})

const markSize = {
  sm: 'h-8 w-8',
  md: 'h-9 w-9',
  lg: 'h-11 w-11',
}

const textSize = {
  sm: 'text-base',
  md: 'text-lg',
  lg: 'text-xl',
}
</script>

<template>
  <span class="inline-flex items-center gap-2.5 select-none">
    <span
      :class="markSize[size]"
      class="relative grid place-items-center rounded-xl bg-linear-to-br from-flow-400 to-signal-500 shadow-lg shadow-flow-500/25"
    >
      <svg viewBox="0 0 24 24" class="h-[60%] w-[60%]" aria-hidden="true">
        <!-- três colunas de dados que "fluem" para cima -->
        <rect x="2.5" y="13" width="4" height="8.5" rx="1.4" fill="#fff" fill-opacity=".62" />
        <rect x="9.5" y="8" width="4" height="13.5" rx="1.4" fill="#fff" fill-opacity=".82" />
        <rect x="16.5" y="3" width="4" height="18.5" rx="1.4" fill="#fff" />
        <path
          d="M3 10.2 10 6l4.4 2.3L21.2 2.6"
          fill="none"
          stroke="#fff"
          stroke-width="1.9"
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-opacity=".95"
        />
      </svg>
    </span>
    <span v-if="wordmark" :class="textSize[size]" class="font-semibold tracking-tight text-white">
      Insight<span class="text-flow-300">Flow</span>
    </span>
  </span>
</template>
