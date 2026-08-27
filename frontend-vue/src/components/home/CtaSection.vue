<script setup>
import { RouterLink } from 'vue-router'
</script>

<template>
  <section class="relative border-t border-white/8 py-16 lg:py-20">
    <div class="mx-auto max-w-7xl px-5 lg:px-8">
      <div
        class="relative overflow-hidden rounded-3xl border border-white/10 bg-linear-to-br from-ink-800 via-ink-900 to-ink-950 px-8 py-12 text-center lg:px-14 lg:py-14"
      >
        <div
          class="pointer-events-none absolute inset-0 opacity-40 bg-[linear-gradient(to_right,rgba(148,176,214,0.07)_1px,transparent_1px),linear-gradient(to_bottom,rgba(148,176,214,0.07)_1px,transparent_1px)] bg-size-[56px_56px]"
          aria-hidden="true"
        />
        <div
          class="pointer-events-none absolute inset-0 bg-[radial-gradient(80%_100%_at_50%_0%,rgba(18,168,212,0.2),transparent_65%)]"
          aria-hidden="true"
        />

        <div class="relative mx-auto max-w-2xl">
          <h2 class="text-2xl font-semibold tracking-tight text-white sm:text-3xl">
            Comece pela planilha que mais consome o seu tempo
          </h2>
          <p class="mt-4 text-base leading-relaxed text-mist-300">
            Envie um arquivo real e veja o InsightFlow validar, calcular e apontar o que
            merece atenção.
          </p>

          <div class="mt-8 flex flex-col justify-center gap-3 sm:flex-row">
            <RouterLink
              to="/login"
              class="inline-flex items-center justify-center rounded-xl bg-white px-7 py-3.5 text-sm font-semibold text-ink-900 shadow-xl shadow-black/30 transition hover:bg-mist-100"
            >
              Entrar no sistema
            </RouterLink>
            <a
              href="#como-funciona"
              class="inline-flex items-center justify-center rounded-xl border border-white/15 px-7 py-3.5 text-sm font-semibold text-mist-100 transition hover:bg-white/8"
            >
              Ver como funciona
            </a>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
