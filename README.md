# InsightFlow — Sistema CTI Insights

**Projeto Integrador II — SENAI "Roberto Mange"**
Tecnologia em Análise e Desenvolvimento de Sistemas · Módulo F1 · 2º semestre
Demanda 2026/01 1.32 02 — *Tratamento e Análise de Dados Comerciais para Geração
de Insights Estratégicos*
Empresa parceira: **Provedor CTI — Cti Comunicação de Dados e Tecnologia LTDA**

---

## O problema

A CTI mantém a carteira de clientes em planilhas Excel preenchidas manualmente
por consultores diferentes. O mesmo segmento aparece como `"IND."`, `"Industria"`
e `"INDUSTRIA"`; há campos em branco, códigos repetidos e capitalização
misturada. O dado existe, mas é operacional: ninguém consegue responder quais
segmentos concentram mais clientes, quais serviços são mais contratados ou como
a carteira se distribui entre os níveis A, B e C.

**Pergunta-guia:** como transformar essa planilha em um sistema web que trata os
dados automaticamente e mostra insights estratégicos para a empresa?

## A solução

Um sistema web que recebe a planilha como ela é, padroniza os dados com rotinas
em Python, guarda tudo em banco na nuvem, calcula estatísticas e apresenta os
insights em um dashboard.

```
Planilha  →  Vue 3       →  Spring Boot  →  Python        →  Neon        →  Dashboards
Excel        (Front-end)    (API Java)      (Ciência de      (PostgreSQL)   (Chart.js)
                                             Dados)
```

## Hipóteses do projeto

1. É possível padronizar automaticamente planilhas preenchidas de forma
   inconsistente com rotinas de tratamento em Python.
2. Um sistema web com upload e geração automática de gráficos substitui a
   análise manual feita hoje.
3. Cruzando segmento, nível A/B/C e faixa de faturamento é possível identificar
   padrões e oportunidades comerciais hoje invisíveis.
4. A solução pode ser construída e publicada usando apenas ferramentas gratuitas.

**Limite assumido:** a qualidade dos insights depende da qualidade dos dados
recebidos. Quando há campos faltando, o próprio sistema gera um insight de
qualidade de dados apontando o que ficou de fora — em vez de esconder a
limitação.

---

## Estrutura do repositório

```
InsightFlow/
├── frontend-vue/       [Vue 3 + Vite + Tailwind + Chart.js]
│   ├── src/
│   │   ├── components/   home/, layout/, upload/, dashboard/
│   │   ├── views/        HomeView, LoginView, UploadView, DashboardView
│   │   ├── router/       rotas + título da página
│   │   └── services/     api.js (axios + tratamento de erro)
│   ├── package.json
│   └── vite.config.js
├── backend-java/       [Spring Boot 3 + JPA]
│   ├── src/main/java/br/senai/ctiinsights/
│   │   ├── controller/  services/  repository/  domain/  dto/  exception/  config/
│   ├── Dockerfile
│   └── pom.xml
├── analytics-python/   [Python + Pandas + SciPy + Matplotlib]
│   ├── limpeza.py  analise.py  insights.py  graficos.py
│   ├── gerar_planilha_exemplo.py
│   ├── requirements.txt
│   └── output/         clientes.json, indicadores.json, insights.json, graficos/
├── database/           [PostgreSQL]
│   └── schema.sql
├── docs/
│   ├── automacao-industrial.md
│   └── relacoes-humanas.md
└── render.yaml
```

---

## Como rodar

### 1. Módulo de Ciência de Dados

```bash
cd analytics-python
pip install -r requirements.txt
python gerar_planilha_exemplo.py                       # planilha de teste despadronizada
python analise.py --entrada planilha_exemplo.xlsx --saida output
```

### 2. API

```bash
cd backend-java
./mvnw spring-boot:run -Dspring-boot.run.profiles=local   # H2 em memória, sem precisar do Neon
```

No Windows (PowerShell/cmd), use `mvnw.cmd` no lugar de `./mvnw`. Não é preciso
instalar Maven: o wrapper baixa a versão certa na primeira execução — só o
**JDK 21+** precisa estar instalado.

A API descobre sozinha o interpretador Python, testando `python3`, `python` e
`py` nessa ordem. Se o seu estiver em outro lugar, defina `PYTHON_BIN` com o
caminho.

### 3. Front-end

```bash
cd frontend-vue
npm install
cp .env.example .env.development
npm run dev
```

Telas: `/` landing · `/login` · `/upload` envio da planilha · `/dashboard` insights.

---

## O papel de cada disciplina

| UC | O que entrega | Onde está |
| -- | ------------- | --------- |
| **Frameworks Front-end** | telas de upload e dashboard, 4 gráficos, layout responsivo, consumo da API com tratamento de erro | `frontend-vue/` |
| **Desenvolvimento Back-end** | modelo de classes, CRUD REST completo, 3 exceções customizadas, chamada do Python via `ProcessBuilder`, CORS | `backend-java/` |
| **Ciência de Dados** | padronização da planilha, estatística descritiva, testes de hipótese, correlação, 6 gráficos, lista de insights | `analytics-python/` |
| **Automação Industrial** | integração entre sistemas, comparação HTTP/REST × Modbus, dashboards como painel de monitoramento, registro do que não se aplica | [`docs/automacao-industrial.md`](docs/automacao-industrial.md) |
| **Computação em Nuvem** | publicação em Vercel + Render + Neon, variáveis de ambiente, HTTPS, monitoramento | `render.yaml`, `frontend-vue/vercel.json`, `backend-java/Dockerfile` |
| **Relações Humanas e Cidadania** | divisão de tarefas, confidencialidade dos dados da CTI, reflexão sobre LGPD | [`docs/relacoes-humanas.md`](docs/relacoes-humanas.md) |

---

## O que o tratamento resolve

| Na planilha da CTI | Depois do tratamento |
| ------------------ | -------------------- |
| `"IND."`, `"Industria"`, `"INDUSTRIA"`, `"industria "` | `Industria` |
| `"a"`, `" B"`, `"Nivel A"`, `"classe c"` | `A`, `B`, `C` |
| `"R$ 1.250.000,00"`, `"1,2 mi"`, `"90 mil"`, `"Ate 360 mil"` | valor numérico em reais |
| `"Link Dedicado; cloud / TELEFONIA"` | lista de três serviços padronizados |
| código CTI repetido | mantém o registro mais recente e reporta a duplicidade |
| linha sem código | descartada, com aviso na tela de upload |

## Análises geradas

- Tendência central e dispersão do faturamento (média, mediana, moda, desvio
  padrão, quartis), no total e por segmento e nível.
- **Shapiro-Wilk** — testa se o faturamento segue distribuição normal, o que
  decide se média ou mediana descreve melhor a carteira.
- **Qui-quadrado** — testa associação entre segmento de atuação e nível do
  cliente.
- **Correlação de Pearson e Spearman** entre faturamento e número de serviços
  contratados.
- Seis gráficos: barras, pizza, linha, dispersão, histograma e boxplot.

---

## Publicação

| Camada | Serviço | Plano |
| ------ | ------- | ----- |
| Front-end | Vercel | gratuito |
| API + Python | Render (Docker) | gratuito |
| Banco | Neon PostgreSQL | gratuito |

Java e Python ficam no **mesmo container** porque o `ProcessBuilder` só enxerga
um processo local. Nenhuma credencial está no repositório: tudo vem de variável
de ambiente (`render.yaml` e `.env.example`).

## Confidencialidade

A planilha real da CTI **não é versionada**. O `.gitignore` bloqueia `*.xlsx`,
`*.xls` e `*.csv`, e todo o desenvolvimento usa a base sintética gerada por
`gerar_planilha_exemplo.py`. Detalhes em
[`docs/relacoes-humanas.md`](docs/relacoes-humanas.md).

## Pendências conhecidas

- Autenticação: a tela de login valida no cliente e abre o painel; o endpoint
  `POST /api/auth/login` ainda não existe. Sem isso, não deve receber dados
  reais da empresa.
- Preencher os nomes do grupo na tabela de divisão de tarefas.
- Links de "Esqueci minha senha", SSO, termos e privacidade apontam para `#`.
- Valores da seção de planos da landing são de referência.
