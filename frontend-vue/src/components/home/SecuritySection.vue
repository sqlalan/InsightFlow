<script setup>
const pillars = [
  {
    title: 'Criptografia ponta a ponta',
    text: 'TLS 1.3 no transporte e AES-256 no armazenamento. Chaves rotacionadas e isoladas por organização.',
  },
  {
    title: 'Acesso por perfil',
    text: 'Permissões granulares por área, autenticação em dois fatores e sessões com expiração configurável.',
  },
  {
    title: 'Trilha de auditoria',
    text: 'Quem enviou, quem visualizou, o que mudou. Registro imutável de cada operação sobre os dados.',
  },
  {
    title: 'Aderência à LGPD',
    text: 'Base legal documentada, anonimização de campos sensíveis e exclusão definitiva sob demanda.',
  },
]

const guarantees = [
  'Seus arquivos nunca são usados para treinar modelos',
  'Processamento em infraestrutura hospedada no Brasil',
  'Backup diário com retenção configurável',
  'Exclusão completa da conta e dos dados em até 30 dias',
]
</script>

<template>
  <section id="seguranca" class="relative overflow-hidden border-t border-white/8 py-24 lg:py-32">
    <div
      class="pointer-events-none absolute inset-0 bg-[radial-gradient(60%_60%_at_20%_50%,rgba(16,185,129,0.10),transparent_65%)]"
      aria-hidden="true"
    />

    <div class="relative mx-auto grid max-w-7xl gap-16 px-5 lg:grid-cols-[0.95fr_1.05fr] lg:items-center lg:px-8">
      <div>
        <p class="text-xs font-semibold uppercase tracking-[0.2em] text-trust-400">Segurança</p>
        <h2 class="mt-4 text-3xl font-semibold tracking-tight text-white sm:text-4xl">
          Dado de operação é dado sensível. Tratamos como tal.
        </h2>
        <p class="mt-5 text-base leading-relaxed text-mist-400">
          A confiança na análise começa antes do primeiro gráfico: ela depende de como o
          arquivo é recebido, armazenado e acessado. Por isso a segurança do InsightFlow é
          estrutural, não um recurso opcional.
        </p>

        <ul class="mt-9 space-y-4">
          <li v-for="g in guarantees" :key="g" class="flex items-start gap-3">
            <span class="mt-0.5 grid h-5 w-5 shrink-0 place-items-center rounded-full bg-trust-500/15 text-trust-400">
              <svg viewBox="0 0 20 20" class="h-3 w-3" fill="none" stroke="currentColor" stroke-width="2.6">
                <path d="m5 10.4 3.2 3.1L15 6.6" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
            </span>
            <span class="text-sm leading-relaxed text-mist-300">{{ g }}</span>
          </li>
        </ul>

        <div class="mt-10 flex flex-wrap items-center gap-3">
          <span
            v-for="tag in ['LGPD', 'ISO 27001 — em processo', 'SSO / SAML', '2FA obrigatório']"
            :key="tag"
            class="rounded-lg border border-white/10 bg-white/5 px-3 py-1.5 text-xs font-medium text-mist-300"
          >
            {{ tag }}
          </span>
        </div>
      </div>

      <div class="grid gap-5 sm:grid-cols-2">
        <div
          v-for="(p, i) in pillars"
          :key="p.title"
          class="rounded-2xl border border-white/8 bg-white/3 p-6 transition hover:border-trust-400/25 hover:bg-white/6"
          :class="i % 2 === 1 ? 'sm:translate-y-6' : ''"
        >
          <span class="grid h-10 w-10 place-items-center rounded-xl bg-trust-500/12 text-trust-400 ring-1 ring-trust-400/20">
            <svg viewBox="0 0 24 24" class="h-5 w-5" fill="none" stroke="currentColor" stroke-width="1.7">
              <path d="M12 3.2 19 6v5.4c0 4.1-2.8 7.4-7 8.8-4.2-1.4-7-4.7-7-8.8V6z" stroke-linejoin="round" />
              <path d="M12 10.2v3.4" stroke-linecap="round" />
              <circle cx="12" cy="8.4" r=".4" fill="currentColor" />
            </svg>
          </span>
          <h3 class="mt-5 text-sm font-semibold text-white">{{ p.title }}</h3>
          <p class="mt-2 text-sm leading-relaxed text-mist-400">{{ p.text }}</p>
        </div>
      </div>
    </div>
  </section>
</template>
