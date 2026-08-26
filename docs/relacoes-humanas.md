# Relações Humanas e Cidadania

> Entregável da UC de Relações Humanas e Cidadania (item 1.3.6 do plano de ensino).

Esta UC não trata de código: trata de como o grupo se organiza e de como o
projeto se relaciona com as pessoas cujos dados ele processa.

---

## 1. Divisão de tarefas

> **Preencher com os nomes reais do grupo antes da entrega.** A estrutura abaixo
> segue os papéis previstos no item 1.3.7 do plano de ensino.

| Papel | Integrante | Responsabilidade principal |
| ----- | ---------- | -------------------------- |
| Product Owner | *a definir* | representa a voz da CTI; prioriza o que resolve o problema do cliente |
| Scrum Master | *a definir* | conduz a reunião semanal, mantém o Kanban e remove impedimentos |
| Front-end | *a definir* | `frontend-vue/` — telas de upload e dashboard, responsividade |
| Back-end | *a definir* | `backend-java/` — API REST, modelo de classes, exceções |
| Ciência de Dados | *a definir* | `analytics-python/` — tratamento, estatística e gráficos |
| Nuvem e banco | *a definir* | deploy na Vercel/Render/Neon, variáveis de ambiente, `database/` |

Os papéis podem rodar entre as sprints. O que não pode é ficar sem dono
declarado: toda tarefa no Kanban tem um responsável e uma data.

### Cronograma das sprints

| Sprint | Data | Entrega |
| ------ | ---- | ------- |
| 1 | 28/08/2026 | problemática, hipóteses, modelagem de classes e protótipo de telas |
| 2 | 02/10/2026 | CRUD da API funcionando e primeira versão do script Python |
| 3 | 06/11/2026 | Front-end consumindo a API, dashboards e análises estatísticas |
| 4 | 04/12/2026 | sistema publicado, documentação fechada e ensaio da apresentação |

### Cerimônias

- **Reunião semanal (15 min):** o que fiz, o que vou fazer, onde estou travado.
- **Revisão de sprint:** demonstração do que funciona — não de slides.
- **Retrospectiva:** o que manter e o que mudar na organização do grupo.

O quadro Kanban fica no GitHub Projects do próprio repositório, com as colunas
Backlog, A Fazer, Em Andamento e Concluído.

---

## 2. Confidencialidade dos dados da CTI

A base de clientes da Provedor CTI (Cti Comunicação de Dados e Tecnologia LTDA)
é **informação comercial sensível**. Compromissos assumidos pelo grupo:

1. A planilha real da empresa **não é versionada** neste repositório. O
   `.gitignore` bloqueia `*.xlsx` e `*.xls` justamente para impedir um commit
   acidental.
2. Todo desenvolvimento e teste usa a planilha sintética gerada por
   [`gerar_planilha_exemplo.py`](../analytics-python/gerar_planilha_exemplo.py),
   que reproduz os vícios da planilha real sem conter nenhum cliente real.
3. Os dados são usados **exclusivamente para fins acadêmicos** deste Projeto
   Integrador. Não são compartilhados fora do grupo, do docente e da banca.
4. Prints e capturas usados na apresentação saem da base sintética, não da base
   real.
5. Credenciais de banco e URLs de produção ficam em variáveis de ambiente, nunca
   no código — ver `.env.example` e `application.properties`.

---

## 3. Privacidade e proteção de dados (LGPD)

A LGPD (Lei 13.709/2018) protege dados de **pessoas naturais**. A base da CTI é
majoritariamente de pessoas jurídicas — mas dois pontos do projeto tocam dados
pessoais e merecem cuidado:

| Dado tratado | Natureza | Cuidado adotado |
| ------------ | -------- | --------------- |
| Nome do consultor responsável | dado pessoal de funcionário | mantido porque é necessário para a análise por carteira; não é exposto publicamente |
| Código CTI do cliente | identificador comercial | usado no lugar da razão social — a planilha já vem pseudonimizada pela empresa |
| Segmento, nível, faturamento | dado de empresa | não é dado pessoal, mas é sigilo comercial |

**Princípios da LGPD aplicados na prática:**

- **Finalidade e necessidade:** o sistema coleta apenas os seis campos que as
  análises exigem. Não pedimos CNPJ, endereço, telefone ou contato — dados que
  não entram em nenhum gráfico não devem ser coletados.
- **Segurança:** comunicação em HTTPS ponta a ponta, banco acessível apenas pela
  aplicação, credenciais em variável de ambiente.
- **Transparência:** a tabela `telemetria` registra cada processamento, o que
  permite responder quando e como um dado foi tratado.
- **Minimização na demonstração:** a apresentação usa base sintética.

**Limite reconhecido:** o sistema, nesta entrega, não implementa autenticação de
usuários nem controle de acesso por perfil. Em um uso real na CTI isso seria
requisito antes de qualquer dado verdadeiro entrar no sistema — está registrado
como próximo passo, não como algo pronto.

---

## 4. Postura de comunicação

Com a **empresa parceira**: linguagem de negócio, não de código. A CTI não
precisa saber o que é `ProcessBuilder`; precisa saber que a planilha é tratada
automaticamente e o que os números mostram sobre a carteira.

Com a **banca avaliadora**: honestidade sobre o alcance. Onde a análise tem
limitação — poucos dados históricos, campos em branco, faturamento ausente em
parte da base — isso é dito explicitamente, inclusive dentro do próprio produto:
o sistema gera um insight de "qualidade dos dados" apontando o que faltou. Um
resultado apresentado sem suas limitações é um resultado mal apresentado.

Entre o **grupo**: divergência técnica se resolve com argumento e evidência, não
por antiguidade ou volume de voz. Quem discorda propõe alternativa.

---

## 5. Ética e responsabilidade sobre o resultado

O sistema produz insights que podem orientar decisões comerciais reais — onde
concentrar esforço, quais clientes abordar. Isso traz duas responsabilidades:

1. **Não inventar significado.** Um qui-quadrado com p = 0,45 significa que não
   há associação detectável; o texto do insight diz exatamente isso, em vez de
   sugerir um padrão que os dados não sustentam.
2. **Não deixar o número decidir sozinho.** O insight de "segmentos com presença
   marginal" aponta um fato, não uma ordem de abandonar clientes. A decisão é da
   CTI, com contexto que a planilha não contém.
