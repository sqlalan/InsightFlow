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
Planilha  →  Vue 3       →  Spring Boot  →  Python        →  Azure       →  Dashboards
Excel        (Front-end)    (API Java)      (Ciência de      Database for   (Chart.js)
                                             Dados)          PostgreSQL
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
│   │   ├── views/        HomeView, LoginView, UploadView, DashboardView, RelatoriosView
│   │   ├── stores/       uploadStore.js (Pinia: leitura e prévia da planilha)
│   │   ├── router/       rotas + título da página
│   │   └── services/     api.js (axios + tratamento de erro)
│   ├── package.json
│   └── vite.config.js
├── backend-java/       [Spring Boot 3 + JPA]
│   ├── src/main/java/br/senai/ctiinsights/
│   │   ├── controller/  services/  repository/  domain/  dto/  exception/  config/
│   ├── src/test/java/   testes da validação da planilha e das faixas
│   ├── Dockerfile
│   └── pom.xml
├── analytics-python/   [Python + Pandas + SciPy + Matplotlib]
│   ├── limpeza.py  analise.py  insights.py  graficos.py
│   ├── tests/          testes do tratamento (unittest)
│   ├── exemplos/       modelo da aula para testar o upload (dados fictícios)
│   ├── requirements.txt
│   └── output/         clientes.json, indicadores.json, insights.json, graficos/
├── database/           [PostgreSQL]
│   └── schema.sql
├── docs/
│   ├── diagrama/       diagrama-classes.png
│   └── prototipos/     telas do sistema em .png
└── render.yaml
```

---

## Como rodar

### 1. Módulo de Ciência de Dados

```bash
cd analytics-python
pip install -r requirements.txt
python analise.py --entrada exemplos/CTI_Insights_modelo_upload_aula.xlsx --saida output
```

### 2. API

```bash
cd backend-java
export AUTH_ADMIN_EMAIL="admin@exemplo.com"
export AUTH_ADMIN_PASSWORD="<sua senha com pelo menos 8 caracteres>"
./mvnw spring-boot:run -Dspring-boot.run.profiles=local   # H2 em memória, sem precisar do Azure
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

Telas: `/` landing · `/login` · `/upload` envio da planilha · `/dashboard` indicadores ·
`/relatorios` histórico, insights e exportação.

### 4. Testes

```bash
cd analytics-python && python -m unittest discover tests   # tratamento da planilha
cd backend-java && ./mvnw test                               # validação e faixas de faturamento
```

### Modelo da planilha

O cabeçalho segue o modelo da aula: `codigo_cliente`, `nome_cliente`, `consultor`,
`segmento`, `nivel_cliente`, `faturamento_anual`, `servicos_contratados`,
`data_contratacao`, `cidade`, `uf`. Para testar, envie
`analytics-python/exemplos/CTI_Insights_modelo_upload_aula.xlsx`. Os sinônimos aceitos ficam em dois lugares que
precisam andar juntos: `EQUIVALENTES` em `PlanilhaService.java` e `equivalentes` em
`limpeza.py`.

---

## Responsabilidades da equipe

| Integrante | Responsabilidade | Evidência no repositório |
| ---------- | ---------------- | ------------------------ |
| **Alan Muterle da Silva** | Consolidação da entrega, repositório e cadastro na SAGA. Publicação nos serviços gratuitos | histórico do Git, `README.md`, `render.yaml`, `frontend-vue/vercel.json` |
| **Raphael Reche** | Levantamento da problemática junto à CTI: uso atual da planilha, evidências de inconsistência e consequências. Confidencialidade dos dados e LGPD | seção [O problema](#o-problema), seção [Confidencialidade](#confidencialidade), `analytics-python/exemplos/CTI_Insights_modelo_upload_aula.xlsx` |
| **Rogerio Bertolino** | Formulação e teste das hipóteses. Rotinas de tratamento e análise estatística dos dados | `analytics-python/limpeza.py`, `analytics-python/analise.py`, `analytics-python/insights.py`, `analytics-python/graficos.py`, `analytics-python/tests/test_limpeza.py` |
| **Maycol Ticona** | Modelagem das classes do domínio e diagrama. Implementação da API e do banco | `docs/diagrama/diagrama-classes.png`, `backend-java/src/main/java/br/senai/ctiinsights/domain/`, `database/schema.sql`, `backend-java/src/test/java/` |
| **Yasmin Bertolino** | Fluxo do usuário e protótipos das telas. Interface em Vue e gráficos do dashboard | `docs/prototipos/`, `frontend-vue/src/views/`, `frontend-vue/src/components/` |

| Item | Onde está |
| ---- | --------- |
| Repositório | https://github.com/sqlalan/InsightFlow |
| Branch de desenvolvimento | `projeto-integrador` (produção: `main`) |
| Pasta do diagrama | `docs/diagrama/` — arquivo `diagrama-classes.png` |
| Pasta dos protótipos | `docs/prototipos/` — 9 telas em `.png` (ver [`docs/README.md`](docs/README.md)) |

---

## O papel de cada disciplina

| UC | O que entrega | Onde está |
| -- | ------------- | --------- |
| **Frameworks Front-end** | telas de upload, dashboard e relatórios, 4 gráficos, Pinia, layout responsivo, consumo da API com tratamento de erro | `frontend-vue/` |
| **Desenvolvimento Back-end** | modelo de classes, CRUD REST completo, 3 exceções customizadas, chamada do Python via `ProcessBuilder`, CORS | `backend-java/` |
| **Ciência de Dados** | padronização da planilha, estatística descritiva, testes de hipótese, correlação, 6 gráficos, lista de insights | `analytics-python/` |
| **Automação Industrial** | integração entre sistemas, dashboards como painel de monitoramento, histórico de processamento | `backend-java/` (`TelemetriaService`), `frontend-vue/` (Relatórios) |
| **Computação em Nuvem** | publicação em Vercel + Render + Azure Database for PostgreSQL, variáveis de ambiente, HTTPS, monitoramento | `render.yaml`, `frontend-vue/vercel.json`, `backend-java/Dockerfile` |
| **Relações Humanas e Cidadania** | divisão de tarefas, confidencialidade dos dados da CTI, reflexão sobre LGPD | seção [Confidencialidade](#confidencialidade) |

---

## O que o tratamento resolve

| Na planilha da CTI | Depois do tratamento |
| ------------------ | -------------------- |
| `"IND."`, `"Industria"`, `"INDUSTRIA"`, `"industria "` | `Indústria` |
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
| Banco | Azure Database for PostgreSQL (Flexible Server) | conta Azure for Students / gratuita |

Java e Python ficam no **mesmo container** porque o `ProcessBuilder` só enxerga
um processo local. Nenhuma credencial está no repositório: tudo vem de variável
de ambiente (`render.yaml` e `.env.example`).

## Confidencialidade

A planilha real da CTI **não é versionada**. O `.gitignore` bloqueia `*.xlsx`,
`*.xls` e `*.csv`, e todo o desenvolvimento usa a planilha modelo da aula
(`analytics-python/exemplos/CTI_Insights_modelo_upload_aula.xlsx`), com dados
fictícios — é o único `.xlsx` liberado no `.gitignore`.

## Pendências conhecidas

- A autenticação usa uma conta administrativa definida por ambiente. As sessões
  ficam em memória e são invalidadas ao reiniciar a API. Cadastro de múltiplos
  usuários, recuperação de senha, SSO e autenticação em dois fatores ainda não
  estão implementados.
- Links de "Esqueci minha senha", SSO, termos e privacidade apontam para `#`.

O roteiro completo para Windows, com exemplos de chamadas e explicação das
camadas, está em [backend-java/EXECUCAO_E_ARQUITETURA.md](backend-java/EXECUCAO_E_ARQUITETURA.md).
