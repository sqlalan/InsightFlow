<script setup>
import { computed, ref } from 'vue'
import { enviarPlanilha } from '../../services/api'

const emit = defineEmits(['processada'])

const EXTENSOES = ['.xlsx', '.xls']
const TAMANHO_MAXIMO = 10 * 1024 * 1024

const arquivo = ref(null)
const arrastando = ref(false)
const enviando = ref(false)
const progresso = ref(0)
const erro = ref(null)
const campo = ref(null)

// `<` dentro de interpolação confunde o parser de template: o rótulo vem pronto daqui.
const rotuloProgresso = computed(() =>
  progresso.value >= 100 ? 'Tratando os dados no servidor…' : `Enviando… ${progresso.value}%`,
)

const tamanhoLegivel = computed(() => {
  if (!arquivo.value) return ''
  const mb = arquivo.value.size / 1024 / 1024
  return mb < 1 ? `${Math.round(arquivo.value.size / 1024)} KB` : `${mb.toFixed(1)} MB`
})

/** Validação no navegador: evita subir arquivo que a API já recusaria. */
function validar(candidato) {
  const nome = candidato.name.toLowerCase()
  if (!EXTENSOES.some((extensao) => nome.endsWith(extensao))) {
    return 'Formato não suportado. Envie a planilha em .xlsx ou .xls.'
  }
  if (candidato.size > TAMANHO_MAXIMO) {
    return 'O arquivo passa de 10 MB. Exporte apenas a aba de clientes.'
  }
  if (candidato.size === 0) {
    return 'O arquivo está vazio.'
  }
  return null
}

function selecionar(candidato) {
  if (!candidato) return
  const problema = validar(candidato)
  if (problema) {
    erro.value = { mensagem: problema, detalhes: [] }
    arquivo.value = null
    return
  }
  erro.value = null
  arquivo.value = candidato
}

function aoSoltar(evento) {
  arrastando.value = false
  selecionar(evento.dataTransfer?.files?.[0])
}

async function enviar() {
  if (!arquivo.value || enviando.value) return
  enviando.value = true
  erro.value = null
  progresso.value = 0
  try {
    const resultado = await enviarPlanilha(arquivo.value, (valor) => {
      progresso.value = valor
    })
    emit('processada', resultado)
    arquivo.value = null
    if (campo.value) campo.value.value = ''
  } catch (falha) {
    erro.value = { mensagem: falha.message, detalhes: falha.detalhes ?? [] }
  } finally {
    enviando.value = false
  }
}
</script>

<template>
  <div class="rounded-2xl border border-white/10 bg-ink-900/60 p-6 sm:p-8">
    <h2 class="text-lg font-semibold text-white">Envio da planilha</h2>
    <p class="mt-1 text-sm text-mist-400">
      Aceita a planilha da CTI mesmo despadronizada — o tratamento é feito no servidor.
    </p>

    <label
      class="mt-6 flex cursor-pointer flex-col items-center justify-center gap-3 rounded-xl border-2 border-dashed px-6 py-10 text-center transition"
      :class="
        arrastando
          ? 'border-flow-400 bg-flow-500/10'
          : 'border-white/15 bg-ink-950/40 hover:border-flow-400/60 hover:bg-white/[0.03]'
      "
      @dragover.prevent="arrastando = true"
      @dragleave.prevent="arrastando = false"
      @drop.prevent="aoSoltar"
    >
      <svg class="h-9 w-9 text-flow-300" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" aria-hidden="true">
        <path d="M12 16V4m0 0L8 8m4-4 4 4" stroke-linecap="round" stroke-linejoin="round" />
        <path d="M4 16v2a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-2" stroke-linecap="round" />
      </svg>
      <span class="text-sm font-medium text-mist-200">
        Arraste a planilha aqui ou <span class="text-flow-300 underline">escolha um arquivo</span>
      </span>
      <span class="text-xs text-mist-500">.xlsx ou .xls — até 10 MB</span>
      <input
        ref="campo"
        type="file"
        accept=".xlsx,.xls"
        class="sr-only"
        @change="selecionar($event.target.files?.[0])"
      />
    </label>

    <div
      v-if="arquivo"
      class="mt-4 flex items-center justify-between gap-4 rounded-lg border border-white/10 bg-ink-950/60 px-4 py-3"
    >
      <div class="min-w-0">
        <p class="truncate text-sm font-medium text-mist-100">{{ arquivo.name }}</p>
        <p class="text-xs text-mist-500">{{ tamanhoLegivel }}</p>
      </div>
      <button
        type="button"
        class="shrink-0 rounded-md px-2 py-1 text-xs text-mist-400 transition hover:text-white"
        @click="arquivo = null"
      >
        Remover
      </button>
    </div>

    <div v-if="erro" role="alert" class="mt-4 rounded-lg border border-red-500/30 bg-red-500/10 px-4 py-3">
      <p class="text-sm font-medium text-red-200">{{ erro.mensagem }}</p>
      <ul v-if="erro.detalhes.length" class="mt-2 list-disc pl-5 text-xs text-red-200/80">
        <li v-for="detalhe in erro.detalhes" :key="detalhe">{{ detalhe }}</li>
      </ul>
    </div>

    <div v-if="enviando" class="mt-4">
      <div class="h-1.5 overflow-hidden rounded-full bg-white/10">
        <div
          class="h-full rounded-full bg-flow-400 transition-all duration-200"
          :style="{ width: `${progresso}%` }"
        />
      </div>
      <p class="mt-2 text-xs text-mist-400">
        {{ rotuloProgresso }}
      </p>
    </div>

    <button
      type="button"
      class="mt-6 w-full rounded-lg bg-flow-500 px-4 py-3 text-sm font-semibold text-ink-950 transition hover:bg-flow-400 disabled:cursor-not-allowed disabled:opacity-40"
      :disabled="!arquivo || enviando"
      @click="enviar"
    >
      {{ enviando ? 'Processando…' : 'Enviar e analisar' }}
    </button>
  </div>
</template>
