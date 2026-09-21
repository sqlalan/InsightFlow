<script setup>
import { computed, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUploadStore } from '../../stores/uploadStore'

/**
 * Diálogo do envio da planilha. Passa por quatro etapas, todas tiradas do store:
 * confirmar -> processando -> concluído (ou erro).
 *
 * Usa o <dialog> nativo do HTML: ele já bloqueia o resto da página, prende o
 * foco dentro do diálogo e fecha com Esc -- sem biblioteca de modal.
 */
const aberto = defineModel({ type: Boolean, default: false })

const upload = useUploadStore()
const router = useRouter()
const dialogo = ref(null)

const etapa = computed(() => {
  if (upload.enviando) return 'processando'
  if (upload.erroEnvio) return 'erro'
  if (upload.resultado) return 'concluido'
  return 'confirmar'
})

watch(aberto, (valor) => {
  if (valor) dialogo.value?.showModal()
  else dialogo.value?.close()
})

// Contador de segundos: o Python leva perto de um minuto e, sem ele, a tela
// parada parece travada.
const segundos = ref(0)
let relogio = null

watch(
  () => upload.enviando,
  (enviando) => {
    clearInterval(relogio)
    if (!enviando) return
    segundos.value = 0
    relogio = setInterval(() => (segundos.value += 1), 1000)
  },
)

onUnmounted(() => clearInterval(relogio))

/**
 * Esc não pode fechar durante o processamento: o servidor continuaria sem
 * ninguém olhando o resultado.
 *
 * A escuta é na janela, não no <dialog>: o botão "Confirmar envio" some ao ser
 * clicado, o foco cai para fora do diálogo e ele nunca receberia a tecla. E o
 * bloqueio é no keydown porque o Chrome ignora o preventDefault do `cancel`.
 */
function bloquearEsc(evento) {
  if (evento.key === 'Escape' && upload.enviando) evento.preventDefault()
}

window.addEventListener('keydown', bloquearEsc, true)
onUnmounted(() => window.removeEventListener('keydown', bloquearEsc, true))

function aoCancelar(evento) {
  if (upload.enviando) evento.preventDefault()
}

function fechar() {
  aberto.value = false
}

/**
 * Roda em qualquer forma de fechar (botão, Esc): sem isso, reabrir o diálogo
 * mostraria o resultado ou o erro do envio anterior.
 */
function aoFechar() {
  aberto.value = false
  if (upload.resultado) upload.limpar()
  upload.erroEnvio = null
}

function irParaDashboard() {
  fechar()
  router.push('/dashboard')
}

function formatarDuracao(ms) {
  return ms < 1000 ? `${ms} ms` : `${(ms / 1000).toFixed(1)} s`
}
</script>

<template>
  <dialog
    ref="dialogo"
    class="m-auto w-[calc(100%-2rem)] max-w-lg rounded-2xl border border-white/10 bg-ink-900 p-0 text-mist-200 shadow-2xl shadow-black/50 backdrop:bg-black/70 backdrop:backdrop-blur-sm"
    aria-labelledby="titulo-envio"
    @cancel="aoCancelar"
    @close="aoFechar"
  >
    <!-- 1. Confirmar -->
    <div v-if="etapa === 'confirmar'" class="p-6">
      <h2 id="titulo-envio" class="text-lg font-semibold text-white">Confirmar envio</h2>
      <p class="mt-1 truncate text-sm text-mist-400">{{ upload.arquivo?.name }}</p>

      <dl class="mt-5 grid grid-cols-3 gap-3">
        <div class="rounded-lg border border-white/10 bg-ink-950/60 p-3">
          <dt class="text-xs text-mist-500">Linhas</dt>
          <dd class="mt-1 text-xl font-semibold tabular-nums text-white">
            {{ upload.totalClientes }}
          </dd>
        </div>
        <div class="rounded-lg border border-white/10 bg-ink-950/60 p-3">
          <dt class="text-xs text-mist-500">Nível A</dt>
          <dd class="mt-1 text-xl font-semibold tabular-nums text-white">
            {{ upload.clientesNivelA }}
          </dd>
        </div>
        <div class="rounded-lg border border-white/10 bg-ink-950/60 p-3">
          <dt class="text-xs text-mist-500">Avisos</dt>
          <dd
            class="mt-1 text-xl font-semibold tabular-nums"
            :class="upload.totalAvisos ? 'text-amber-300' : 'text-white'"
          >
            {{ upload.totalAvisos }}
          </dd>
        </div>
      </dl>

      <p v-if="upload.totalAvisos" class="mt-4 text-xs leading-relaxed text-mist-400">
        Os avisos não impedem o envio. Ao final, o resumo mostra o que ficou de fora.
      </p>

      <div class="mt-6 flex justify-end gap-3">
        <button
          type="button"
          class="rounded-lg border border-white/10 px-4 py-2.5 text-sm font-medium text-mist-200 transition hover:bg-white/5"
          @click="fechar"
        >
          Cancelar
        </button>
        <button
          type="button"
          class="rounded-lg bg-flow-500 px-4 py-2.5 text-sm font-semibold text-ink-950 transition hover:bg-flow-400"
          @click="upload.enviarParaBackend()"
        >
          Confirmar envio
        </button>
      </div>
    </div>

    <!-- 2. Processando -->
    <div v-else-if="etapa === 'processando'" class="p-6" aria-live="polite">
      <div class="flex items-center gap-3">
        <span
          class="h-5 w-5 shrink-0 animate-spin rounded-full border-2 border-flow-400/30 border-t-flow-400"
          aria-hidden="true"
        />
        <h2 id="titulo-envio" class="text-lg font-semibold text-white">
          {{ upload.progresso < 100 ? 'Enviando a planilha' : 'Analisando os dados' }}
        </h2>
      </div>

      <div class="mt-5 h-1.5 overflow-hidden rounded-full bg-white/10">
        <div
          class="h-full rounded-full bg-flow-400 transition-all duration-300"
          :class="{ 'animate-pulse': upload.progresso >= 100 }"
          :style="{ width: `${upload.progresso}%` }"
        />
      </div>

      <p class="mt-4 text-sm text-mist-400">
        <template v-if="upload.progresso < 100">
          Enviando o arquivo… {{ upload.progresso }}%
        </template>
        <template v-else>
          Padronizando os dados e calculando os indicadores. Pode levar até um minuto.
        </template>
      </p>
      <p class="mt-2 text-xs tabular-nums text-mist-500">{{ segundos }} s decorridos</p>
    </div>

    <!-- 3. Concluído -->
    <div v-else-if="etapa === 'concluido'" class="p-6" aria-live="polite">
      <div class="flex items-center gap-3">
        <span
          class="grid h-8 w-8 shrink-0 place-items-center rounded-full bg-trust-500/15 text-trust-400"
          aria-hidden="true"
        >
          <svg viewBox="0 0 20 20" class="h-4 w-4" fill="none" stroke="currentColor" stroke-width="2.4">
            <path d="m5 10.4 3.2 3.1L15 6.6" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </span>
        <div>
          <h2 id="titulo-envio" class="text-lg font-semibold text-white">Planilha processada</h2>
          <p class="text-xs text-mist-500">
            em {{ formatarDuracao(upload.resultado.duracaoMs) }} ·
            {{ upload.resultado.insights?.length ?? 0 }} insights gerados
          </p>
        </div>
      </div>

      <dl class="mt-5 grid grid-cols-2 gap-3 sm:grid-cols-4">
        <div class="rounded-lg border border-white/10 bg-ink-950/60 p-3">
          <dt class="text-xs text-mist-500">Linhas lidas</dt>
          <dd class="mt-1 text-lg font-semibold tabular-nums text-white">
            {{ upload.resultado.linhasLidas }}
          </dd>
        </div>
        <div class="rounded-lg border border-white/10 bg-ink-950/60 p-3">
          <dt class="text-xs text-mist-500">Clientes novos</dt>
          <dd class="mt-1 text-lg font-semibold tabular-nums text-white">
            {{ upload.resultado.clientesImportados }}
          </dd>
        </div>
        <div class="rounded-lg border border-white/10 bg-ink-950/60 p-3">
          <dt class="text-xs text-mist-500">Atualizados</dt>
          <dd class="mt-1 text-lg font-semibold tabular-nums text-white">
            {{ upload.resultado.clientesAtualizados }}
          </dd>
        </div>
        <div class="rounded-lg border border-white/10 bg-ink-950/60 p-3">
          <dt class="text-xs text-mist-500">Contratos</dt>
          <dd class="mt-1 text-lg font-semibold tabular-nums text-white">
            {{ upload.resultado.contratosImportados }}
          </dd>
        </div>
      </dl>

      <div v-if="upload.resultado.avisos?.length" class="mt-4">
        <h3 class="text-xs font-semibold uppercase tracking-wide text-mist-400">
          Avisos
        </h3>
        <ul class="mt-2 list-disc space-y-1 pl-5 text-xs text-mist-300">
          <li v-for="(aviso, indice) in upload.resultado.avisos.slice(0, 4)" :key="indice">
            {{ aviso }}
          </li>
        </ul>
      </div>

      <div class="mt-6 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
        <button
          type="button"
          class="rounded-lg border border-white/10 px-4 py-2.5 text-sm font-medium text-mist-200 transition hover:bg-white/5"
          @click="fechar"
        >
          Enviar outra planilha
        </button>
        <button
          type="button"
          class="rounded-lg bg-white px-4 py-2.5 text-sm font-semibold text-ink-900 transition hover:bg-mist-100"
          @click="irParaDashboard"
        >
          Ver dashboard
        </button>
      </div>
    </div>

    <!-- 4. Erro -->
    <div v-else class="p-6" role="alert">
      <h2 id="titulo-envio" class="text-lg font-semibold text-red-200">
        Não foi possível processar
      </h2>
      <p class="mt-2 text-sm text-mist-300">{{ upload.erroEnvio.mensagem }}</p>
      <ul
        v-if="upload.erroEnvio.detalhes.length"
        class="mt-2 list-disc space-y-1 pl-5 text-xs text-mist-400"
      >
        <li v-for="detalhe in upload.erroEnvio.detalhes" :key="detalhe">{{ detalhe }}</li>
      </ul>

      <div class="mt-6 flex justify-end gap-3">
        <button
          type="button"
          class="rounded-lg border border-white/10 px-4 py-2.5 text-sm font-medium text-mist-200 transition hover:bg-white/5"
          @click="fechar"
        >
          Fechar
        </button>
        <button
          type="button"
          class="rounded-lg bg-flow-500 px-4 py-2.5 text-sm font-semibold text-ink-950 transition hover:bg-flow-400"
          @click="upload.enviarParaBackend()"
        >
          Tentar novamente
        </button>
      </div>
    </div>
  </dialog>
</template>
