<script setup>
const pillars = [
  { title: 'Criptografia', text: 'TLS 1.3 no transporte e AES-256 no armazenamento.' },
  { title: 'Acesso por perfil', text: 'Permissões por área, 2FA e sessões com expiração.' },
  { title: 'Trilha de auditoria', text: 'Registro de quem enviou, quem viu e o que mudou.' },
  { title: 'Aderência à LGPD', text: 'Anonimização de campos sensíveis e exclusão sob demanda.' },
]
</script>

<template>
  <section id="seguranca" class="relative overflow-hidden border-t border-white/8 py-16 lg:py-20">
    <div
      class="pointer-events-none absolute inset-0 bg-[radial-gradient(60%_60%_at_20%_50%,rgba(16,185,129,0.10),transparent_65%)]"
      aria-hidden="true"
    />

    <div class="relative mx-auto max-w-7xl px-5 lg:px-8">
      <div class="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div class="max-w-2xl">
          <p class="text-xs font-semibold uppercase tracking-[0.2em] text-trust-400">Segurança</p>
          <h2 class="mt-3 text-2xl font-semibold tracking-tight text-white sm:text-3xl">
            Dado de operação é dado sensível. Tratamos como tal.
          </h2>
        </div>
        <p class="max-w-sm text-sm leading-relaxed text-mist-400">
          Seus arquivos não treinam modelos e podem ser excluídos por completo a qualquer momento.
        </p>
      </div>

      <div class="mt-10 grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
        <div
          v-for="p in pillars"
          :key="p.title"
          class="rounded-2xl border border-white/8 bg-white/3 p-5 transition hover:border-trust-400/25 hover:bg-white/6"
        >
          <span
            class="grid h-10 w-10 place-items-center rounded-xl bg-trust-500/12 text-trust-400 ring-1 ring-trust-400/20"
          >
            <svg viewBox="0 0 24 24" class="h-5 w-5" fill="none" stroke="currentColor" stroke-width="1.7">
              <path d="M12 3.2 19 6v5.4c0 4.1-2.8 7.4-7 8.8-4.2-1.4-7-4.7-7-8.8V6z" stroke-linejoin="round" />
              <path d="M12 10.2v3.4" stroke-linecap="round" />
              <circle cx="12" cy="8.4" r=".4" fill="currentColor" />
            </svg>
          </span>
          <h3 class="mt-4 text-sm font-semibold text-white">{{ p.title }}</h3>
          <p class="mt-1.5 text-sm leading-relaxed text-mist-400">{{ p.text }}</p>
        </div>
      </div>

      <div class="mt-8 flex flex-wrap items-center gap-2.5">
        <span
          v-for="tag in ['LGPD', 'ISO 27001 — em processo', 'SSO / SAML', '2FA obrigatório']"
          :key="tag"
          class="rounded-lg border border-white/10 bg-white/5 px-3 py-1.5 text-xs font-medium text-mist-300"
        >
          {{ tag }}
        </span>
      </div>
    </div>
  </section>
</template>
