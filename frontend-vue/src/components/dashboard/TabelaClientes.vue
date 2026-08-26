<script setup>
import { computed, ref } from 'vue'
import { reclassificarCliente, removerCliente } from '../../services/api'

/**
 * Tabela da carteira. Recebe a lista por prop e mantém localmente apenas o
 * estado da tela (busca, filtro, ordenação, linha em edição). As alterações vão
 * para a API via PATCH/DELETE e o pai é avisado para recarregar os indicadores.
 */
const props = defineProps({
  clientes: { type: Array, default: () => [] },
  carregando: { type: Boolean, default: false },
})
const emit = defineEmits(['alterado'])

const busca = ref('')
const segmento = ref('')
const ordenacao = ref({ campo: 'codigoCti', crescente: true })
const ocupado = ref(null)
const erro = ref('')

const moeda = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
  maximumFractionDigits: 0,
})

const CORES_NIVEL = {
  A: 'bg-trust-500/15 text-trust-400 border-trust-400/25',
  B: 'bg-flow-500/15 text-flow-300 border-flow-400/25',
  C: 'bg-white/5 text-mist-300 border-white/10',
}

const segmentos = computed(() =>
  [...new Set(props.clientes.map((cliente) => cliente.segmento))].sort(),
)

const visiveis = computed(() => {
  const termo = busca.value.trim().toLowerCase()
  const lista = props.clientes.filter((cliente) => {
    const casaBusca =
      !termo ||
      cliente.codigoCti.toLowerCase().includes(termo) ||
      (cliente.consultor ?? '').toLowerCase().includes(termo)
    const casaSegmento = !segmento.value || cliente.segmento === segmento.value
    return casaBusca && casaSegmento
  })

  const { campo, crescente } = ordenacao.value
  return [...lista].sort((a, b) => {
    const valorA = a[campo] ?? ''
    const valorB = b[campo] ?? ''
    const comparacao =
      typeof valorA === 'number' && typeof valorB === 'number'
        ? valorA - valorB
        : String(valorA).localeCompare(String(valorB), 'pt-BR')
    return crescente ? comparacao : -comparacao
  })
})

function ordenarPor(campo) {
  ordenacao.value =
    ordenacao.value.campo === campo
      ? { campo, crescente: !ordenacao.value.crescente }
      : { campo, crescente: true }
}

function formatarFaturamento(valor) {
  return valor == null ? '—' : moeda.format(valor)
}

async function reclassificar(cliente, nivel) {
  if (nivel === cliente.nivel) return
  ocupado.value = cliente.id
  erro.value = ''
  try {
    await reclassificarCliente(cliente.id, nivel)
    emit('alterado')
  } catch (falha) {
    erro.value = falha.message
  } finally {
    ocupado.value = null
  }
}

async function remover(cliente) {
  if (!window.confirm(`Remover o cliente ${cliente.codigoCti} da base?`)) return
  ocupado.value = cliente.id
  erro.value = ''
  try {
    await removerCliente(cliente.id)
    emit('alterado')
  } catch (falha) {
    erro.value = falha.message
  } finally {
    ocupado.value = null
  }
}
</script>

<template>
  <section class="rounded-2xl border border-white/10 bg-ink-900/60">
    <header class="flex flex-col gap-3 border-b border-white/10 p-5 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h3 class="text-sm font-semibold text-white">Carteira de clientes</h3>
        <p class="mt-0.5 text-xs text-mist-500">
          {{ visiveis.length }} de {{ clientes.length }} clientes
        </p>
      </div>

      <div class="flex flex-col gap-2 sm:flex-row">
        <input
          v-model="busca"
          type="search"
          placeholder="Buscar por código ou consultor"
          class="rounded-lg border border-white/10 bg-ink-950/60 px-3 py-2 text-sm text-mist-100 placeholder:text-mist-500 focus:border-flow-400/60"
        />
        <select
          v-model="segmento"
          class="rounded-lg border border-white/10 bg-ink-950/60 px-3 py-2 text-sm text-mist-100 focus:border-flow-400/60"
        >
          <option value="">Todos os segmentos</option>
          <option v-for="item in segmentos" :key="item" :value="item">{{ item }}</option>
        </select>
      </div>
    </header>

    <p v-if="erro" role="alert" class="border-b border-red-500/20 bg-red-500/10 px-5 py-3 text-sm text-red-200">
      {{ erro }}
    </p>

    <div class="overflow-x-auto">
      <table class="w-full min-w-[46rem] text-left text-sm">
        <thead class="text-xs uppercase tracking-wide text-mist-500">
          <tr class="border-b border-white/10">
            <th class="px-5 py-3">
              <button type="button" class="transition hover:text-mist-200" @click="ordenarPor('codigoCti')">
                Código CTI
              </button>
            </th>
            <th class="px-5 py-3">
              <button type="button" class="transition hover:text-mist-200" @click="ordenarPor('segmento')">
                Segmento
              </button>
            </th>
            <th class="px-5 py-3">Nível</th>
            <th class="px-5 py-3 text-right">
              <button type="button" class="transition hover:text-mist-200" @click="ordenarPor('faturamentoAnual')">
                Faturamento anual
              </button>
            </th>
            <th class="px-5 py-3">Consultor</th>
            <th class="px-5 py-3 text-right">Contratos</th>
            <th class="px-5 py-3"><span class="sr-only">Ações</span></th>
          </tr>
        </thead>

        <tbody class="divide-y divide-white/5">
          <tr v-if="carregando">
            <td colspan="7" class="px-5 py-10 text-center text-mist-500">Carregando carteira…</td>
          </tr>
          <tr v-else-if="!visiveis.length">
            <td colspan="7" class="px-5 py-10 text-center text-mist-500">
              Nenhum cliente encontrado para este filtro.
            </td>
          </tr>
          <template v-else>
            <tr
              v-for="cliente in visiveis"
              :key="cliente.id"
              class="transition hover:bg-white/[0.03]"
              :class="ocupado === cliente.id && 'opacity-50'"
            >
              <td class="px-5 py-3 font-medium text-mist-100">{{ cliente.codigoCti }}</td>
              <td class="px-5 py-3 text-mist-300">{{ cliente.segmento }}</td>
              <td class="px-5 py-3">
                <select
                  :value="cliente.nivel"
                  :disabled="ocupado === cliente.id"
                  class="rounded-md border px-2 py-1 text-xs font-semibold"
                  :class="CORES_NIVEL[cliente.nivel]"
                  @change="reclassificar(cliente, $event.target.value)"
                >
                  <option value="A">A</option>
                  <option value="B">B</option>
                  <option value="C">C</option>
                </select>
              </td>
              <td class="px-5 py-3 text-right tabular-nums text-mist-200">
                {{ formatarFaturamento(cliente.faturamentoAnual) }}
              </td>
              <td class="px-5 py-3 text-mist-300">{{ cliente.consultor ?? '—' }}</td>
              <td class="px-5 py-3 text-right tabular-nums text-mist-300">{{ cliente.contratosAtivos }}</td>
              <td class="px-5 py-3 text-right">
                <button
                  type="button"
                  class="rounded-md px-2 py-1 text-xs text-mist-400 transition hover:bg-red-500/10 hover:text-red-300"
                  :disabled="ocupado === cliente.id"
                  @click="remover(cliente)"
                >
                  Remover
                </button>
              </td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>
  </section>
</template>
