<script setup>
import AppHeader from '../components/layout/AppHeader.vue'
import AppFooter from '../components/layout/AppFooter.vue'
import HeroSection from '../components/home/HeroSection.vue'
import HowItWorks from '../components/home/HowItWorks.vue'
import FeaturesSection from '../components/home/FeaturesSection.vue'
import SecuritySection from '../components/home/SecuritySection.vue'
import CtaSection from '../components/home/CtaSection.vue'
</script>

<template>
  <div class="min-h-screen bg-ink-950">
    <AppHeader />
    <main>
      <HeroSection />
      <HowItWorks />
      <FeaturesSection />
      <SecuritySection />
      <CtaSection />
    </main>
    <AppFooter />
  </div>
</template>
