<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <RouterView v-slot="{ Component }">
    <Transition
      mode="out-in"
      enter-active-class="transition duration-250 ease-out motion-reduce:transition-none"
      enter-from-class="translate-y-2 opacity-0"
      leave-active-class="transition duration-200 ease-in motion-reduce:transition-none"
      leave-to-class="opacity-0"
    >
      <component :is="Component" />
    </Transition>
  </RouterView>
</template>
