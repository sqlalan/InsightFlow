<script setup>
import { computed, reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import BrandLogo from '../components/BrandLogo.vue'
import { useAuthStore } from '../stores/authStore'

const form = reactive({
  email: '',
  password: '',
  remember: true,
})

const router = useRouter()
const auth = useAuthStore()

const touched = reactive({ email: false, password: false })
const showPassword = ref(false)
const loading = ref(false)
const notice = ref(null) // { type: 'info' | 'error', text: string }

const emailError = computed(() => {
  if (!form.email) return 'Informe seu e-mail corporativo.'
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(form.email)) return 'E-mail em formato inválido.'
  return ''
})

const passwordError = computed(() => {
  if (!form.password) return 'Informe sua senha.'
  if (form.password.length < 8) return 'A senha deve ter ao menos 8 caracteres.'
  return ''
})

const isValid = computed(() => !emailError.value && !passwordError.value)

async function handleSubmit() {
  touched.email = true
  touched.password = true
  notice.value = null

  if (!isValid.value) return

  loading.value = true
  try {
    await auth.login(form.email, form.password, form.remember)
    await router.push('/upload')
  } catch (erro) {
    notice.value = { type: 'error', text: erro.message }
  } finally {
    loading.value = false
  }
}

const highlights = [
  'Acesso com as credenciais fornecidas pelo administrador',
  'Sessões com prazo de validade e opção de encerrar o acesso',
  'Histórico de processamento das planilhas no painel de relatórios',
]
</script>

<template>
  <div class="grid min-h-screen lg:grid-cols-2">
    <!-- painel institucional -->
    <aside class="relative hidden overflow-hidden bg-ink-900 p-12 lg:flex lg:flex-col lg:justify-between">
      <div
        class="pointer-events-none absolute inset-0 opacity-50 bg-[linear-gradient(to_right,rgba(148,176,214,0.07)_1px,transparent_1px),linear-gradient(to_bottom,rgba(148,176,214,0.07)_1px,transparent_1px)] bg-size-[56px_56px]"
        aria-hidden="true"
      />
      <div
        class="pointer-events-none absolute inset-0 bg-[radial-gradient(90%_70%_at_20%_10%,rgba(18,168,212,0.22),transparent_60%)]"
        aria-hidden="true"
      />
      <div
        class="pointer-events-none absolute -bottom-32 -left-20 h-96 w-96 rounded-full bg-signal-500/15 blur-[130px]"
        aria-hidden="true"
      />

      <RouterLink to="/" class="relative w-fit">
        <BrandLogo size="lg" />
      </RouterLink>

      <div class="relative max-w-md">
        <h2 class="text-3xl font-semibold leading-tight tracking-tight text-white">
          Acesse seus dados de operação com
          <span
            class="bg-linear-to-r from-flow-300 via-flow-400 to-signal-400 bg-clip-text text-transparent"
            >indicadores, gráficos e relatórios</span
          >.
        </h2>
        <p class="mt-6 text-sm leading-relaxed text-mist-400">
          Entre para acompanhar indicadores, revisar planilhas enviadas e compartilhar
          relatórios com o seu time.
        </p>

        <ul class="mt-10 space-y-4">
          <li v-for="item in highlights" :key="item" class="flex items-start gap-3">
            <span class="mt-0.5 grid h-5 w-5 shrink-0 place-items-center rounded-full bg-trust-500/15 text-trust-400">
              <svg viewBox="0 0 20 20" class="h-3 w-3" fill="none" stroke="currentColor" stroke-width="2.6">
                <path d="m5 10.4 3.2 3.1L15 6.6" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
            </span>
            <span class="text-sm leading-relaxed text-mist-300">{{ item }}</span>
          </li>
        </ul>
      </div>

      <div class="relative flex items-center gap-3 text-xs text-mist-500">
        <svg viewBox="0 0 24 24" class="h-4 w-4 text-trust-400" fill="none" stroke="currentColor" stroke-width="1.8">
          <rect x="4.5" y="10" width="15" height="10" rx="2" />
          <path d="M8.5 10V7.5a3.5 3.5 0 0 1 7 0V10" stroke-linecap="round" />
        </svg>
        InsightFlow · análise da carteira de clientes
      </div>
    </aside>

    <!-- formulário -->
    <main class="relative flex items-center justify-center bg-ink-950 px-5 py-12 sm:px-10">
      <div
        class="pointer-events-none absolute inset-0 bg-[radial-gradient(70%_50%_at_50%_0%,rgba(47,107,240,0.10),transparent_60%)] lg:hidden"
        aria-hidden="true"
      />

      <div class="relative w-full max-w-md">
        <RouterLink to="/" class="mb-10 inline-flex lg:hidden">
          <BrandLogo />
        </RouterLink>

        <RouterLink
          to="/"
          class="mb-8 hidden items-center gap-1.5 text-xs font-medium text-mist-400 transition hover:text-mist-100 lg:inline-flex"
        >
          <svg viewBox="0 0 20 20" class="h-3.5 w-3.5" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M11.5 5 6.5 10l5 5" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
          Voltar ao site
        </RouterLink>

        <h1 class="text-2xl font-semibold tracking-tight text-white sm:text-3xl">
          Acessar a plataforma
        </h1>
        <p class="mt-2.5 text-sm text-mist-400">
          Use as credenciais fornecidas pelo administrador da sua organização.
        </p>

        <Transition
          enter-active-class="transition duration-200"
          enter-from-class="-translate-y-1 opacity-0"
        >
          <div
            v-if="notice"
            class="mt-6 flex gap-3 rounded-xl border p-3.5"
            :class="
              notice.type === 'error'
                ? 'border-red-400/25 bg-red-500/10'
                : 'border-flow-400/25 bg-flow-500/10'
            "
            role="status"
          >
            <svg
              viewBox="0 0 20 20"
              class="mt-0.5 h-4 w-4 shrink-0"
              :class="notice.type === 'error' ? 'text-red-400' : 'text-flow-300'"
              fill="none"
              stroke="currentColor"
              stroke-width="1.8"
            >
              <circle cx="10" cy="10" r="7.5" />
              <path d="M10 6.5v4.2M10 13.4v.2" stroke-linecap="round" />
            </svg>
            <p class="text-xs leading-relaxed text-mist-200">{{ notice.text }}</p>
          </div>
        </Transition>

        <form class="mt-8 space-y-5" novalidate @submit.prevent="handleSubmit">
          <div>
            <label for="email" class="mb-2 block text-sm font-medium text-mist-200">
              E-mail corporativo
            </label>
            <input
              id="email"
              v-model.trim="form.email"
              type="email"
              autocomplete="email"
              placeholder="nome@suaempresa.com.br"
              class="w-full rounded-xl border bg-white/4 px-4 py-3 text-sm text-white placeholder:text-mist-500 transition focus:bg-white/6 focus:outline-none"
              :class="
                touched.email && emailError
                  ? 'border-red-400/50'
                  : 'border-white/10 focus:border-flow-400/60'
              "
              :aria-invalid="Boolean(touched.email && emailError)"
              :aria-describedby="touched.email && emailError ? 'email-erro' : undefined"
              @blur="touched.email = true"
            />
            <p v-if="touched.email && emailError" id="email-erro" class="mt-1.5 text-xs text-red-400">
              {{ emailError }}
            </p>
          </div>

          <div>
            <div class="mb-2 flex items-center justify-between">
              <label for="senha" class="block text-sm font-medium text-mist-200">Senha</label>
              <a href="#" class="text-xs font-medium text-flow-300 transition hover:text-flow-200">
                Esqueci minha senha
              </a>
            </div>
            <div class="relative">
              <input
                id="senha"
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                autocomplete="current-password"
                placeholder="Mínimo de 8 caracteres"
                class="w-full rounded-xl border bg-white/4 px-4 py-3 pr-12 text-sm text-white placeholder:text-mist-500 transition focus:bg-white/6 focus:outline-none"
                :class="
                  touched.password && passwordError
                    ? 'border-red-400/50'
                    : 'border-white/10 focus:border-flow-400/60'
                "
                :aria-invalid="Boolean(touched.password && passwordError)"
                :aria-describedby="touched.password && passwordError ? 'senha-erro' : undefined"
                @blur="touched.password = true"
              />
              <button
                type="button"
                class="absolute right-2 top-1/2 grid h-8 w-8 -translate-y-1/2 place-items-center rounded-lg text-mist-400 transition hover:bg-white/8 hover:text-mist-100"
                :aria-label="showPassword ? 'Ocultar senha' : 'Mostrar senha'"
                @click="showPassword = !showPassword"
              >
                <svg viewBox="0 0 24 24" class="h-4.5 w-4.5" fill="none" stroke="currentColor" stroke-width="1.7">
                  <path
                    d="M2.6 12S6 5.8 12 5.8 21.4 12 21.4 12 18 18.2 12 18.2 2.6 12 2.6 12Z"
                    stroke-linejoin="round"
                  />
                  <circle cx="12" cy="12" r="2.8" />
                  <path v-if="showPassword" d="m4 20 16-16" stroke-linecap="round" />
                </svg>
              </button>
            </div>
            <p v-if="touched.password && passwordError" id="senha-erro" class="mt-1.5 text-xs text-red-400">
              {{ passwordError }}
            </p>
          </div>

          <label class="flex cursor-pointer items-center gap-2.5 text-sm text-mist-300">
            <input
              v-model="form.remember"
              type="checkbox"
              class="h-4 w-4 rounded border-white/20 bg-white/5 accent-flow-500"
            />
            Manter conectado neste dispositivo
          </label>

          <button
            type="submit"
            :disabled="loading"
            class="flex w-full items-center justify-center gap-2 rounded-xl bg-linear-to-r from-flow-400 to-signal-500 px-5 py-3.5 text-sm font-semibold text-white shadow-lg shadow-flow-500/25 transition hover:shadow-flow-500/40 disabled:cursor-not-allowed disabled:opacity-70"
          >
            <svg
              v-if="loading"
              class="h-4 w-4 animate-spin"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2.5"
            >
              <circle cx="12" cy="12" r="9" stroke-opacity=".25" />
              <path d="M21 12a9 9 0 0 0-9-9" stroke-linecap="round" />
            </svg>
            {{ loading ? 'Verificando...' : 'Entrar' }}
          </button>
        </form>

        <div class="my-7 flex items-center gap-4">
          <span class="h-px flex-1 bg-white/10" />
          <span class="text-xs text-mist-500">ou</span>
          <span class="h-px flex-1 bg-white/10" />
        </div>

        <button
          type="button"
          class="flex w-full items-center justify-center gap-2.5 rounded-xl border border-white/12 bg-white/4 px-5 py-3.5 text-sm font-medium text-mist-100 transition hover:bg-white/8"
        >
          <svg viewBox="0 0 24 24" class="h-4 w-4 text-mist-300" fill="none" stroke="currentColor" stroke-width="1.7">
            <rect x="4.5" y="10" width="15" height="10" rx="2" />
            <path d="M8.5 10V7.5a3.5 3.5 0 0 1 7 0V10" stroke-linecap="round" />
          </svg>
          Entrar com SSO corporativo
        </button>

        <p class="mt-8 text-center text-sm text-mist-400">
          Ainda não tem acesso?
          <a href="#" class="font-medium text-flow-300 transition hover:text-flow-200">
            Solicite uma conta
          </a>
        </p>

        <p class="mt-10 text-center text-xs leading-relaxed text-mist-500">
          Ao continuar, você concorda com os
          <a href="#" class="underline underline-offset-2 hover:text-mist-300">Termos de uso</a> e a
          <a href="#" class="underline underline-offset-2 hover:text-mist-300">Política de privacidade</a>
          do InsightFlow.
        </p>
      </div>
    </main>
  </div>
</template>
