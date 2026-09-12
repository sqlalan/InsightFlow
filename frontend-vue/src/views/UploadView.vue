<script setup>
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import PainelNav from '../components/layout/PainelNav.vue'
import UploadPlanilha from '../components/upload/UploadPlanilha.vue'
import { useUploadStore } from '../stores/uploadStore'

const upload = useUploadStore()
const resultado = ref(null)

const COLUNAS_ESPERADAS = [
  'Consultor responsável',
  'Cliente CTI (código interno)',
  'Segmento de atuação',
  'Nível do cliente (A, B ou C)',
  'Faixa de faturamento anual',
  'Serviços contratados',
]

function aoProcessar(resposta) {
  resultado.value = resposta
}
</script>

<template>
  <div class="min-h-dvh bg-ink-950 lg:pl-64">
    <PainelNav />

    <main class="mx-auto max-w-5xl px-5 py-10 lg:px-8">
      <h1 class="text-2xl font-semibold text-white sm:text-3xl">Enviar planilha</h1>
      <p class="mt-2 max-w-2xl text-sm text-mist-400">
        A planilha é validada aqui, tratada por rotinas em Python no servidor e gravada no banco.
        Variações de escrita, espaços sobrando e células em branco são resolvidos automaticamente.
      </p>

      <div class="mt-8 grid gap-6 lg:grid-cols-5">
        <div class="lg:col-span-3">
          <UploadPlanilha @processada="aoProcessar" />
        </div>

        <aside class="rounded-2xl border border-white/10 bg-ink-900/40 p-6 lg:col-span-2">
          <h2 class="text-sm font-semibold text-white">Colunas esperadas</h2>
          <ul class="mt-3 space-y-2 text-sm text-mist-300">
            <li v-for="coluna in COLUNAS_ESPERADAS" :key="coluna" class="flex gap-2">
              <span class="mt-1.5 h-1.5 w-1.5 shrink-0 rounded-full bg-flow-400" aria-hidden="true" />
              {{ coluna }}
            </li>
          </ul>
          <p class="mt-4 text-xs leading-relaxed text-mist-500">
            O nome da coluna não precisa estar exato: maiúsculas, acentos e espaços são
            normalizados antes da validação.
          </p>
        </aside>
      </div>

      <!-- Prévia lida no navegador: confere o arquivo antes de mandar ao servidor. -->
      <section
        v-if="upload.temDados"
        class="mt-8 rounded-2xl border border-white/10 bg-ink-900/40 p-6"
      >
        <div class="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <h2 class="text-sm font-semibold text-white">Prévia dos dados tratados</h2>
            <p class="mt-1 text-xs text-mist-500">
              Lida no navegador, antes do envio. O tratamento definitivo é feito no servidor.
            </p>
          </div>
          <dl class="flex gap-6">
            <div>
              <dt class="text-xs text-mist-500">Linhas</dt>
              <dd class="text-lg font-semibold tabular-nums text-white">
                {{ upload.totalClientes }}
              </dd>
            </div>
            <div>
              <dt class="text-xs text-mist-500">Nível A</dt>
              <dd class="text-lg font-semibold tabular-nums text-white">
                {{ upload.clientesNivelA }}
              </dd>
            </div>
          </dl>
        </div>

        <div class="mt-4 overflow-x-auto">
          <table class="w-full min-w-xl text-left text-sm">
            <thead>
              <tr class="border-b border-white/10 text-xs uppercase tracking-wide text-mist-500">
                <th class="py-2 pr-4 font-medium">Código</th>
                <th class="py-2 pr-4 font-medium">Consultor</th>
                <th class="py-2 pr-4 font-medium">Segmento</th>
                <th class="py-2 font-medium">Nível</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="(cliente, indice) in upload.previa"
                :key="cliente.codigo || indice"
                class="border-b border-white/5 text-mist-300"
              >
                <td class="py-2 pr-4 font-medium text-mist-100">{{ cliente.codigo || '—' }}</td>
                <td class="py-2 pr-4">{{ cliente.consultor || '—' }}</td>
                <td class="py-2 pr-4">{{ cliente.segmento || '—' }}</td>
                <td class="py-2">{{ cliente.nivel || '—' }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <p v-if="upload.totalClientes > upload.previa.length" class="mt-3 text-xs text-mist-500">
          Mostrando as {{ upload.previa.length }} primeiras de
          {{ upload.totalClientes }} linhas.
        </p>
      </section>

      <section
        v-if="resultado"
        class="mt-8 rounded-2xl border border-trust-400/25 bg-trust-500/[0.07] p-6"
        aria-live="polite"
      >
        <h2 class="text-sm font-semibold text-trust-400">Planilha processada</h2>

        <dl class="mt-4 grid grid-cols-2 gap-4 sm:grid-cols-4">
          <div>
            <dt class="text-xs text-mist-500">Linhas lidas</dt>
            <dd class="text-lg font-semibold tabular-nums text-white">{{ resultado.linhasLidas }}</dd>
          </div>
          <div>
            <dt class="text-xs text-mist-500">Clientes novos</dt>
            <dd class="text-lg font-semibold tabular-nums text-white">{{ resultado.clientesImportados }}</dd>
          </div>
          <div>
            <dt class="text-xs text-mist-500">Atualizados</dt>
            <dd class="text-lg font-semibold tabular-nums text-white">{{ resultado.clientesAtualizados }}</dd>
          </div>
          <div>
            <dt class="text-xs text-mist-500">Contratos</dt>
            <dd class="text-lg font-semibold tabular-nums text-white">{{ resultado.contratosImportados }}</dd>
          </div>
        </dl>

        <div v-if="resultado.avisos?.length" class="mt-5">
          <h3 class="text-xs font-semibold uppercase tracking-wide text-mist-400">
            Avisos do tratamento
          </h3>
          <ul class="mt-2 list-disc space-y-1 pl-5 text-sm text-mist-300">
            <li v-for="(aviso, indice) in resultado.avisos.slice(0, 8)" :key="indice">{{ aviso }}</li>
          </ul>
        </div>

        <div v-if="resultado.insights?.length" class="mt-5">
          <h3 class="text-xs font-semibold uppercase tracking-wide text-mist-400">
            Primeiros insights
          </h3>
          <ul class="mt-2 space-y-2 text-sm text-mist-200">
            <li v-for="insight in resultado.insights.slice(0, 3)" :key="insight.id">
              {{ insight.resumo }}
            </li>
          </ul>
        </div>

        <RouterLink
          to="/dashboard"
          class="mt-6 inline-flex rounded-lg bg-white px-4 py-2.5 text-sm font-semibold text-ink-900 transition hover:bg-mist-100"
        >
          Ver dashboard completo
        </RouterLink>
      </section>
    </main>
  </div>
</template>
