<script setup>
import { computed, reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import BrandLogo from '../components/BrandLogo.vue'
import { cadastrar } from '../services/api'

/**
 * Pedido de conta do consultor. Enviar o formulário não faz login: a conta
 * nasce pendente no Spring Boot e só entra depois que o administrador aprovar.
 */
const form = reactive({ nome: '', email: '', senha: '', confirmacao: '' })
const touched = reactive({ nome: false, email: false, senha: false, confirmacao: false })
const loading = ref(false)
const erro = ref('')
const enviado = ref(false)

// Nome e e-mail sem espaços nas pontas; a senha vai exatamente como foi digitada,
// senão o login (que não corta espaços) não reconheceria a mesma senha.
const nome = computed(() => form.nome.trim())
const email = computed(() => form.email.trim())

// Mesmas regras do CadastroRequest do Java; aqui só adiantam o aviso na tela.
const erros = computed(() => ({
  nome: nome.value ? '' : 'Informe seu nome.',
  email: !email.value
    ? 'Informe seu e-mail corporativo.'
    : /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(email.value)
      ? ''
      : 'E-mail em formato inválido.',
  senha: form.senha.length >= 8 ? '' : 'A senha deve ter ao menos 8 caracteres.',
  confirmacao: form.confirmacao === form.senha ? '' : 'As senhas não conferem.',
}))

const isValid = computed(() => Object.values(erros.value).every((mensagem) => !mensagem))

async function handleSubmit() {
  Object.keys(touched).forEach((campo) => (touched[campo] = true))
  erro.value = ''
  if (!isValid.value) return

  loading.value = true
  try {
    await cadastrar(nome.value, email.value, form.senha)
    enviado.value = true
  } catch (falha) {
    erro.value = falha.message
  } finally {
    loading.value = false
  }
}

const campos = [
  { id: 'nome', label: 'Nome completo', type: 'text', autocomplete: 'name', placeholder: 'Ana Souza' },
  { id: 'email', label: 'E-mail corporativo', type: 'email', autocomplete: 'email', placeholder: 'nome@suaempresa.com.br' },
  { id: 'senha', label: 'Senha', type: 'password', autocomplete: 'new-password', placeholder: 'Mínimo de 8 caracteres' },
  { id: 'confirmacao', label: 'Confirme a senha', type: 'password', autocomplete: 'new-password', placeholder: 'Repita a senha' },
]
</script>

<template>
  <main class="relative flex min-h-screen items-center justify-center bg-ink-950 px-5 py-12 sm:px-10">
    <div
      class="pointer-events-none absolute inset-0 bg-[radial-gradient(70%_50%_at_50%_0%,rgba(47,107,240,0.10),transparent_60%)]"
      aria-hidden="true"
    />

    <div class="relative w-full max-w-md">
      <RouterLink to="/" class="mb-10 inline-flex">
        <BrandLogo />
      </RouterLink>

      <!-- depois do envio: o formulário some e fica só o aviso -->
      <div v-if="enviado" role="status">
        <span class="grid h-12 w-12 place-items-center rounded-full bg-trust-500/15 text-trust-400">
          <svg viewBox="0 0 20 20" class="h-6 w-6" fill="none" stroke="currentColor" stroke-width="2.2">
            <path d="m5 10.4 3.2 3.1L15 6.6" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </span>
        <h1 class="mt-6 text-2xl font-semibold tracking-tight text-white sm:text-3xl">Pedido enviado</h1>
        <p class="mt-3 text-sm leading-relaxed text-mist-400">
          Sua conta foi criada e está aguardando a aprovação do administrador. Assim que ela for
          liberada, você entra com o e-mail <span class="text-mist-200">{{ email }}</span> e a
          senha que acabou de cadastrar.
        </p>
        <RouterLink
          to="/login"
          class="mt-8 flex w-full items-center justify-center rounded-xl bg-linear-to-r from-flow-400 to-signal-500 px-5 py-3.5 text-sm font-semibold text-white shadow-lg shadow-flow-500/25 transition hover:shadow-flow-500/40"
        >
          Ir para o login
        </RouterLink>
      </div>

      <template v-else>
        <h1 class="text-2xl font-semibold tracking-tight text-white sm:text-3xl">Solicitar conta</h1>
        <p class="mt-2.5 text-sm text-mist-400">
          Preencha seus dados. O administrador precisa aprovar a conta antes do primeiro acesso.
        </p>

        <div
          v-if="erro"
          role="alert"
          class="mt-6 rounded-xl border border-red-400/25 bg-red-500/10 p-3.5 text-xs leading-relaxed text-mist-200"
        >
          {{ erro }}
        </div>

        <form class="mt-8 space-y-5" novalidate @submit.prevent="handleSubmit">
          <div v-for="campo in campos" :key="campo.id">
            <label :for="campo.id" class="mb-2 block text-sm font-medium text-mist-200">
              {{ campo.label }}
            </label>
            <input
              :id="campo.id"
              v-model="form[campo.id]"
              :type="campo.type"
              :autocomplete="campo.autocomplete"
              :placeholder="campo.placeholder"
              class="w-full rounded-xl border bg-white/4 px-4 py-3 text-sm text-white placeholder:text-mist-500 transition focus:bg-white/6 focus:outline-none"
              :class="
                touched[campo.id] && erros[campo.id]
                  ? 'border-red-400/50'
                  : 'border-white/10 focus:border-flow-400/60'
              "
              :aria-invalid="Boolean(touched[campo.id] && erros[campo.id])"
              :aria-describedby="touched[campo.id] && erros[campo.id] ? `${campo.id}-erro` : undefined"
              @blur="touched[campo.id] = true"
            />
            <p
              v-if="touched[campo.id] && erros[campo.id]"
              :id="`${campo.id}-erro`"
              class="mt-1.5 text-xs text-red-400"
            >
              {{ erros[campo.id] }}
            </p>
          </div>

          <button
            type="submit"
            :disabled="loading"
            class="flex w-full items-center justify-center gap-2 rounded-xl bg-linear-to-r from-flow-400 to-signal-500 px-5 py-3.5 text-sm font-semibold text-white shadow-lg shadow-flow-500/25 transition hover:shadow-flow-500/40 disabled:cursor-not-allowed disabled:opacity-70"
          >
            {{ loading ? 'Enviando...' : 'Solicitar conta' }}
          </button>
        </form>

        <p class="mt-8 text-center text-sm text-mist-400">
          Já tem uma conta?
          <RouterLink to="/login" class="font-medium text-flow-300 transition hover:text-flow-200">
            Entrar
          </RouterLink>
        </p>
      </template>
    </div>
  </main>
</template>
