# frontend-vue — Interface

Front-end do Sistema CTI Insights.
**Vue 3** (`<script setup>`) + **Vite** + **Tailwind CSS 4** + **Vue Router 4** +
**Pinia** + **Chart.js** (via `vue-chartjs`) + **axios** + **xlsx** (SheetJS).

O `xlsx` é instalado do endereço oficial da SheetJS (`cdn.sheetjs.com`), não do
registro do npm: lá a última versão é a 0.18.5, com duas falhas de segurança
conhecidas, e as correções só são publicadas pelo próprio fabricante.

## Rodando

```bash
npm install
cp .env.example .env.development   # aponta VITE_API_URL para a API local
npm run dev      # desenvolvimento
npm run build    # build de produção em dist/
npm run preview  # serve o build
```

`VITE_API_URL` define o endereço da API. Sem ele, o padrão é
`http://localhost:8080`. Em produção, aponta para o serviço do Render.

## Rotas

| Rota         | Tela                              | Status |
| ------------ | --------------------------------- | ------ |
| `/`          | Landing page                      | concluída |
| `/login`     | Autenticação                      | UI concluída, API pendente |
| `/upload`    | Envio da planilha Excel           | concluída |
| `/dashboard` | Indicadores, gráficos e carteira  | concluída |
| `/relatorios`| Histórico, insights e exportação CSV | concluída |
| `*`          | Redireciona para `/`              | — |

As telas do painel são carregadas sob demanda: a landing não paga o custo
do Chart.js no bundle inicial.

## Componentes

```
src/
├── services/api.js            axios + tradução dos erros da API
├── stores/uploadStore.js      Pinia: leitura da planilha, prévia e envio
├── views/
│   ├── HomeView.vue           landing
│   ├── LoginView.vue          login (lazy)
│   ├── UploadView.vue         envio + prévia dos dados (lazy)
│   ├── DashboardView.vue      cartões, gráficos, tabela e insights (lazy)
│   └── RelatoriosView.vue     histórico de processamento, insights e CSV (lazy)
└── components/
    ├── BrandLogo.vue
    ├── layout/                AppHeader, AppFooter, PainelNav
    ├── home/                  seções da landing
    ├── upload/
    │   ├── UploadPlanilha.vue      drag-and-drop e seleção do arquivo
    │   └── ModalEnvio.vue          confirmação, progresso e resumo do envio
    └── dashboard/
        ├── grafico.js         registro do Chart.js + paleta e escalas do tema
        ├── PainelGrafico.vue  moldura comum (título, estado vazio, altura)
        ├── CardIndicador.vue  cartão reaproveitado na linha de indicadores
        ├── GraficoSegmento.vue     barras — clientes por segmento
        ├── GraficoNivel.vue        rosca — distribuição A/B/C
        ├── GraficoFaturamento.vue  barras horizontais — faixa de faturamento
        ├── GraficoEvolucao.vue     linha — contratações por mês
        ├── TabelaClientes.vue      busca, filtro, ordenação, PATCH e DELETE
        └── ListaInsights.vue       insights em texto
```

## Estado e props

A tela de upload guarda o estado no **Pinia** (`stores/uploadStore.js`): arquivo,
prévia, avisos e resultado do envio ficam na store e são lidos pela view, pelo
componente de envio e pelo diálogo. Nas demais telas, os componentes recebem
dados por **props** e devolvem eventos por **emit** — quem busca da API é a view. `DashboardView` faz as três chamadas em paralelo
(`Promise.all`) e passa os recortes para cada gráfico; `TabelaClientes` guarda
apenas o estado da própria tela (busca, filtro, ordenação) com `ref`/`computed`,
e avisa o pai com `@alterado` depois de alterar um cliente.

## Tratamento de erro

`services/api.js` converte qualquer falha em `ErroDaApi`, com `mensagem` pronta
para a tela e `detalhes` quando a API os envia (ex.: a lista de colunas que
faltaram na planilha). Nenhum componente lida com `axios` diretamente.

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
`border-trust-400/25`, `animate-rise`. Os `@keyframes` ficam dentro do próprio
`@theme`, junto dos tokens `--animate-*`. A paleta dos gráficos em
`components/dashboard/grafico.js` repete esses mesmos tons para que gráfico e
interface combinem.

Se algum padrão de classes começar a se repetir demais, o caminho oficial do
Tailwind 4 para extraí-lo é a diretiva `@utility` — não voltar a escrever CSS em
`@layer`.

## Responsividade

Testar em pelo menos duas larguras (exigência do plano de ensino). Os
breakpoints usados são `sm`, `lg` e `xl`: a linha de indicadores vai de 1 para 2 e
depois 4 colunas, os gráficos de 1 para 2 colunas, e a tabela rola
horizontalmente dentro do próprio card em telas estreitas.

## Pendências

- `LoginView.vue`: `handleSubmit` valida no cliente e abre o painel direto. O
  ponto de integração está marcado com `TODO` (`POST /api/auth/login`).
- Links de "Esqueci minha senha", "Solicite uma conta", SSO, termos e
  privacidade apontam para `#`.
