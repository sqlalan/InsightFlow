<script setup>
import PainelNav from '../components/layout/PainelNav.vue'
import UploadPlanilha from '../components/upload/UploadPlanilha.vue'
import { useUploadStore } from '../stores/uploadStore'

const upload = useUploadStore()

// Cabeçalhos do modelo oficial da aula, na ordem da planilha.
// `rotulo` é o que aparece na tela; `nome` é o cabeçalho real, mostrado ao passar o mouse.
// Obrigatórias = as mesmas do ClienteDTO no Java.
const COLUNAS_ESPERADAS = [
  { nome: 'codigo_cliente', rotulo: 'Código do cliente', obrigatoria: true },
  { nome: 'nome_cliente', rotulo: 'Nome do cliente', obrigatoria: true },
  { nome: 'consultor', rotulo: 'Consultor', obrigatoria: true },
  { nome: 'segmento', rotulo: 'Segmento', obrigatoria: true },
  { nome: 'nivel_cliente', rotulo: 'Nível do cliente', obrigatoria: true },
  { nome: 'faturamento_anual', rotulo: 'Faturamento anual', obrigatoria: true },
  { nome: 'servicos_contratados', rotulo: 'Serviços contratados', obrigatoria: true },
  { nome: 'data_contratacao', rotulo: 'Data de contratação', obrigatoria: true },
  { nome: 'cidade', rotulo: 'Cidade', obrigatoria: false },
  { nome: 'uf', rotulo: 'UF', obrigatoria: false },
]
</script>

<template>
  <div class="min-h-dvh bg-ink-950 lg:pl-64">
    <PainelNav />

    <main class="mx-auto max-w-5xl px-5 py-10 lg:px-8">
      <h1 class="text-3xl font-semibold text-white sm:text-4xl">Enviar planilha</h1>
      <p class="mt-2 max-w-2xl text-sm text-mist-400">
        Envie a planilha de clientes para atualizar os indicadores e as análises da carteira.
      </p>

      <div class="mt-8 grid gap-6 lg:grid-cols-5">
        <div class="lg:col-span-3">
          <UploadPlanilha />
        </div>

        <aside class="rounded-2xl border border-white/10 bg-ink-900/40 p-6 lg:col-span-2">
          <h2 class="text-sm font-semibold text-white">Colunas esperadas</h2>
          <ul class="mt-3 space-y-2 text-sm">
            <li
              v-for="coluna in COLUNAS_ESPERADAS"
              :key="coluna.nome"
              class="flex items-center justify-between gap-3"
            >
              <span
                :title="coluna.nome"
                :class="coluna.obrigatoria ? 'text-mist-200' : 'text-mist-500'"
              >
                {{ coluna.rotulo }}
              </span>
              <span v-if="!coluna.obrigatoria" class="text-[0.7rem] text-mist-500">opcional</span>
            </li>
          </ul>
        </aside>
      </div>

      <!-- Prévia lida no navegador: confere o arquivo antes de mandar ao servidor. -->
      <section
        v-if="upload.temDados"
        class="mt-8 rounded-2xl border border-white/10 bg-ink-900/40 p-6"
      >
        <div class="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <h2 class="text-sm font-semibold text-white">Prévia dos dados</h2>
            <p class="mt-1 text-xs text-mist-500">
              Confira se os dados estão corretos antes de enviar.
            </p>
          </div>
          <dl class="flex gap-6">
            <div>
              <dt class="text-xs text-mist-500">Linhas</dt>
              <dd class="text-lg font-semibold tabular-nums text-white">
                {{ upload.quantidadeLinhas }}
              </dd>
            </div>
            <div>
              <dt class="text-xs text-mist-500">Válidas</dt>
              <dd class="text-lg font-semibold tabular-nums text-white">
                {{ upload.quantidadeValidas }}
                <span class="text-xs font-normal text-mist-500">({{ upload.percentualValidos }}%)</span>
              </dd>
            </div>
            <div>
              <dt class="text-xs text-mist-500">Com problema</dt>
              <dd
                class="text-lg font-semibold tabular-nums"
                :class="upload.quantidadeInvalidas ? 'text-amber-300' : 'text-white'"
              >
                {{ upload.quantidadeInvalidas }}
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

        <!-- O que o tratamento corrige sozinho: o usuário não precisa mexer na planilha. -->
        <div
          v-if="upload.ajustes.length"
          class="mt-4 rounded-lg border border-trust-400/20 bg-trust-500/5 px-4 py-3"
        >
          <p class="text-sm font-medium text-trust-300">Ajustes automáticos</p>
          <p class="mt-0.5 text-xs text-mist-500">
            Escritas diferentes do mesmo valor são unificadas antes da análise.
          </p>
          <ul class="mt-2 space-y-1.5 text-xs text-mist-300">
            <li v-for="ajuste in upload.ajustes" :key="ajuste.rotulo">
              <span class="font-medium text-mist-100">{{ ajuste.rotulo }}:</span>
              {{ ajuste.linhas }} {{ ajuste.linhas > 1 ? 'linhas padronizadas' : 'linha padronizada' }}
              <span class="text-mist-500">— ex.: {{ ajuste.exemplos.join(' · ') }}</span>
            </li>
          </ul>
        </div>

        <div class="mt-4 overflow-x-auto">
          <table class="w-full min-w-xl text-left text-sm">
            <thead>
              <tr class="border-b border-white/10 text-xs uppercase tracking-wide text-mist-500">
                <th class="py-2 pr-4 font-medium">Código</th>
                <th class="py-2 pr-4 font-medium">Consultor</th>
                <th class="py-2 pr-4 font-medium">Segmento</th>
                <th class="py-2 pr-4 font-medium">Nível</th>
                <th class="py-2 font-medium">Situação</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="cliente in upload.previa"
                :key="cliente.linha"
                class="border-b border-white/5 text-mist-300"
              >
                <td class="py-2 pr-4 font-medium text-mist-100">{{ cliente.codigo || '—' }}</td>
                <td class="py-2 pr-4">{{ cliente.consultor || '—' }}</td>
                <td class="py-2 pr-4">{{ cliente.segmento || '—' }}</td>
                <td class="py-2 pr-4">{{ cliente.nivel || '—' }}</td>
                <td class="py-2">
                  <span
                    v-if="cliente.problemas.length"
                    class="text-amber-300"
                    :title="cliente.problemas.join('; ')"
                  >
                    Fora do envio
                  </span>
                  <span v-else class="text-trust-300">Válida</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <p v-if="upload.quantidadeLinhas > upload.previa.length" class="mt-3 text-xs text-mist-500">
          Mostrando as {{ upload.previa.length }} primeiras de
          {{ upload.quantidadeLinhas }} linhas.
        </p>
      </section>
    </main>
  </div>
</template>
