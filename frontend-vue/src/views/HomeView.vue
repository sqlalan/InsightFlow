<script setup>
import AppHeader from '../components/layout/AppHeader.vue'
import AppFooter from '../components/layout/AppFooter.vue'
import HeroSection from '../components/home/HeroSection.vue'
import FuncionalidadesSection from '../components/home/FuncionalidadesSection.vue'
</script>

<template>
  <div class="min-h-screen bg-ink-950">
    <AppHeader />
    <main>
      <HeroSection />
      <FuncionalidadesSection />
    </main>
    <AppFooter />
  </div>
</template>
