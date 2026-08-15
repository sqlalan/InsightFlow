# InsightFlow

Plataforma de análise de planilhas Excel (.xlsx / .xls) para times de operações.

Stack: **Vue 3** (`<script setup>`) + **Vite 8** + **Tailwind CSS 4** + **Vue Router 4**.

## Rodando o projeto

```bash
npm install
npm run dev      # ambiente de desenvolvimento
npm run build    # build de produção em dist/
npm run preview  # serve o build
```

## Rotas

| Rota     | Tela                      | Status                       |
| -------- | ------------------------- | ---------------------------- |
| `/`      | Landing page              | Concluída                    |
| `/login` | Autenticação              | UI concluída, API pendente   |
| `*`      | Redireciona para `/`      | —                            |

## Estrutura

```
src/
├── router/index.js          rotas + título da página + scroll behavior
├── style.css                design tokens (@theme), utilitários e keyframes
├── views/
│   ├── HomeView.vue         monta as seções da landing
│   └── LoginView.vue        tela de login (lazy-loaded)
└── components/
    ├── BrandLogo.vue        marca (símbolo + wordmark)
    ├── layout/              AppHeader, AppFooter
    └── home/                HeroSection, HowItWorks, FeaturesSection,
                             SecuritySection, PricingSection, CtaSection
```

## Design system

**Não há CSS escrito à mão no projeto.** O `src/style.css` contém apenas o
`@import "tailwindcss"` e um bloco `@theme` com os tokens; toda a estilização
acontece em classes utilitárias nos templates.

| Família    | Uso                                     |
| ---------- | --------------------------------------- |
| `ink-*`    | fundos e superfícies (base escura)      |
| `mist-*`   | textos e bordas claras                  |
| `flow-*`   | acento primário (ciano) — dados e ações |
| `signal-*` | acento secundário (azul) — gradientes   |
| `trust-*`  | verde — segurança, validação e status   |

Cada token vira utilitário automaticamente: `bg-ink-950`, `text-flow-300`,
`border-trust-400/25`, `animate-rise`, etc. Os `@keyframes` das animações ficam
dentro do próprio `@theme`, junto dos tokens `--animate-*`.

Estilos globais (fundo, cor de texto, `::selection`, foco visível e
`scroll-smooth`) são aplicados por classes no `<html>` e no `<body>` em
`index.html`. Acessibilidade de movimento usa a variante `motion-reduce:`
nos elementos animados.

Se algum padrão de classes começar a se repetir demais, o caminho oficial do
Tailwind 4 para extraí-lo é a diretiva `@utility` — não voltar a escrever CSS
em `@layer`.

## Pendências conhecidas

- `LoginView.vue`: o `handleSubmit` valida no cliente e simula a chamada. O ponto de
  integração está marcado com `TODO` (`POST /api/auth/login`).
- Links de "Esqueci minha senha", "Solicite uma conta", SSO, termos e privacidade
  apontam para `#`.
- Valores da seção de planos são de referência e precisam ser confirmados.
- Rotas do painel (pós-login) ainda não existem.
