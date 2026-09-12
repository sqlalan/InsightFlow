<script setup>
import { computed, ref } from 'vue'
import { useUploadStore } from '../../stores/uploadStore'

const emit = defineEmits(['processada'])

const upload = useUploadStore()

// Estado que é só da interface fica no componente; o resto mora no store.
const arrastando = ref(false)
const campo = ref(null)

const tamanhoLegivel = computed(() => {
  if (!upload.arquivo) return ''
  const mb = upload.arquivo.size / 1024 / 1024
  return mb < 1 ? `${Math.round(upload.arquivo.size / 1024)} KB` : `${mb.toFixed(1)} MB`
})

/** Ao escolher o arquivo já montamos a prévia — o envio fica para o botão. */
async function selecionar(candidato) {
  if (!candidato) return
  upload.selecionarArquivo(candidato)
  await upload.processarPlanilha()
}

function aoSoltar(evento) {
  arrastando.value = false
  selecionar(evento.dataTransfer?.files?.[0])
}

function remover() {
  upload.limpar()
  if (campo.value) campo.value.value = ''
}

async function enviar() {
  const resultado = await upload.enviarParaBackend()
  if (!resultado) return
  emit('processada', resultado)
  if (campo.value) campo.value.value = ''
}
</script>

<template>
  <div class="rounded-2xl border border-white/10 bg-ink-900/60 p-6 sm:p-8">
    <h2 class="text-lg font-semibold text-white">Envio da planilha</h2>
    <p class="mt-1 text-sm text-mist-400">
      A planilha é lida aqui no navegador para conferência. O tratamento que vale é feito no
      servidor, depois que você confirmar o envio.
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
      v-if="upload.arquivo"
      class="mt-4 flex items-center justify-between gap-4 rounded-lg border border-white/10 bg-ink-950/60 px-4 py-3"
    >
      <div class="min-w-0">
        <p class="truncate text-sm font-medium text-mist-100">{{ upload.arquivo.name }}</p>
        <p class="text-xs text-mist-500">
          {{ tamanhoLegivel }}
          <span v-if="upload.temDados"> · {{ upload.totalClientes }} linhas lidas</span>
        </p>
      </div>
      <button
        type="button"
        class="shrink-0 rounded-md px-2 py-1 text-xs text-mist-400 transition hover:text-white"
        @click="remover"
      >
        Remover
      </button>
    </div>

    <p v-if="upload.carregando" class="mt-4 text-sm text-mist-400">Lendo a planilha…</p>

    <div
      v-if="upload.totalErros > 0"
      role="alert"
      class="mt-4 rounded-lg border border-red-500/30 bg-red-500/10 px-4 py-3"
    >
      <p class="text-sm font-medium text-red-200">
        {{ upload.totalErros }} aviso(s) na planilha
      </p>
      <ul class="mt-2 list-disc space-y-1 pl-5 text-xs text-red-200/80">
        <li v-for="(aviso, indice) in upload.erros" :key="indice">{{ aviso }}</li>
      </ul>
    </div>

    <div v-if="upload.enviando" class="mt-4">
      <div class="h-1.5 overflow-hidden rounded-full bg-white/10">
        <div
          class="h-full rounded-full bg-flow-400 transition-all duration-200"
          :style="{ width: `${upload.progresso}%` }"
        />
      </div>
      <p class="mt-2 text-xs text-mist-400">{{ upload.rotuloProgresso }}</p>
    </div>

    <button
      type="button"
      class="mt-6 w-full rounded-lg bg-flow-500 px-4 py-3 text-sm font-semibold text-ink-950 transition hover:bg-flow-400 disabled:cursor-not-allowed disabled:opacity-40"
      :disabled="!upload.temDados || upload.enviando"
      @click="enviar"
    >
      {{ upload.enviando ? 'Processando…' : 'Enviar e analisar' }}
    </button>
  </div>
</template>
