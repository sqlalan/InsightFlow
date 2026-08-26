# Automação Industrial — o que se aplica e o que não se aplica

> Entregável da UC de Automação Industrial (item 1.3.4 do plano de ensino).

A ementa desta UC foi escrita para chão de fábrica: sensores, atuadores, robôs e
CLPs. O Sistema CTI Insights é um projeto de **dados comerciais**, não de planta
industrial. Este documento registra, com transparência, quais tópicos da ementa o
projeto realmente exercita e quais ficam apenas no plano teórico — em vez de
forçar uma implementação que não existe.

---

## 1. Bloco aplicável: Integração com a Tecnologia da Informação

### 1.1 Comunicação entre sistemas (equivalente ao 3.1 da ementa)

O projeto não tem CLP, mas trabalha o mesmo princípio estudado na UC: sistemas
independentes que trocam informação por um protocolo combinado.

| Camada | Papel no projeto | Equivalente em planta industrial |
| ------ | ---------------- | -------------------------------- |
| Front-end Vue (Vercel) | interface do operador | IHM / supervisório |
| API Spring Boot (Render) | recebe, valida e distribui os dados | CLP / gateway de campo |
| Módulo Python | processa e transforma o dado bruto | rotina de tratamento no supervisório |
| PostgreSQL (Neon) | histórico persistido | historiador de processo |

A comunicação real implementada é **HTTP/REST sobre HTTPS**, com JSON no corpo
das mensagens.

### 1.2 Comparação com um protocolo industrial (item 3.1.1)

Comparação exigida na ementa, entre o protocolo do projeto e o Modbus TCP:

| Aspecto | HTTP/REST (usado no projeto) | Modbus TCP |
| ------- | ---------------------------- | ---------- |
| Modelo | cliente/servidor sem estado | mestre/escravo |
| Transporte | TCP (porta 443, com TLS) | TCP (porta 502, sem criptografia nativa) |
| Unidade de dado | recurso JSON (`/api/clientes`) | registrador de 16 bits, endereçado por número |
| Descoberta | rota nomeada e autodescritiva | mapa de registradores documentado à parte |
| Payload típico | kilobytes de texto | dezenas de bytes binários |
| Latência esperada | dezenas a centenas de ms | milissegundos |
| Segurança | TLS, CORS por origem, credenciais em variável de ambiente | delegada à rede (VLAN, firewall) |
| Uso adequado | integração entre sistemas de TI | leitura/escrita determinística em campo |

**O que os dois têm em comum:** ambos definem um contrato fixo entre duas partes
que não se conhecem internamente. Quem chama `GET /api/clientes` não sabe se por
trás existe PostgreSQL ou arquivo; quem lê o registrador 40001 não sabe qual
sensor alimenta aquele endereço. É o mesmo desacoplamento.

**A diferença que decide a escolha:** Modbus é determinístico e leve porque a
malha de controle não pode esperar; REST é verboso e tolerante à latência porque
o consultor comercial pode esperar dois segundos por um dashboard. O projeto
escolheu REST porque o requisito é integração de sistemas de informação, não
controle em tempo real.

### 1.3 Implementação da comunicação (item 3.1.2)

O caminho completo de uma requisição está em
[`AnaliseService.java`](../backend-java/src/main/java/br/senai/ctiinsights/service/AnaliseService.java)
e [`api.js`](../frontend-vue/src/services/api.js):

```
Navegador                API Spring Boot            Módulo Python
   |  POST /api/planilhas      |                          |
   | ------------------------> |                          |
   |   (multipart/form-data)   |  ProcessBuilder          |
   |                           | -----------------------> |
   |                           |                          | trata a planilha
   |                           | <----------------------- |
   |                           |   JSON em output/        |
   |                           |                          |
   |                           |--> PostgreSQL (JDBC)     |
   | <------------------------ |                          |
   |   200 + UploadResponse    |                          |
```

O `ProcessBuilder` é a fronteira entre dois runtimes diferentes na mesma máquina
— o mesmo papel que um gateway cumpre entre dois protocolos incompatíveis.

### 1.4 Banco de dados (item 3.2)

PostgreSQL gerenciado no Neon. Modelo em
[`database/schema.sql`](../database/schema.sql). A tabela `telemetria` guarda o
histórico de cada processamento (evento, status, duração), cumprindo o papel de
historiador: é ela que responde "o upload de ontem falhou por quê".

### 1.5 Coleta de dados (itens 3.3 e 3.3.1)

A planilha Excel enviada pela CTI é a **fonte de coleta** do projeto. Em uma
planta industrial esse papel caberia a sensores enviando leituras ao CLP; aqui o
dado nasce do preenchimento manual dos consultores. A diferença é a natureza da
fonte, não o fluxo: em ambos os casos um dado bruto e ruidoso precisa ser
validado e tratado antes de virar informação.

O tratamento correspondente ao "condicionamento de sinal" está em
[`limpeza.py`](../analytics-python/limpeza.py): assim como um sinal analógico
passa por filtro antes de virar valor de processo, `"IND."`, `"Industria"` e
`"INDUSTRIA"` passam pela normalização antes de virar o segmento `Industria`.

### 1.6 Coleta para planejamento (item 3.3.2)

Os insights gerados (por segmento, nível e faturamento) alimentam o planejamento
comercial da CTI, assim como dados de chão de fábrica alimentam o planejamento de
produção. A saída em `output/indicadores.json` é o insumo desse planejamento.

### 1.7 Dashboards e monitoramento (itens 3.4, 3.4.1 e 3.4.2)

O dashboard em Vue + Chart.js
([`DashboardView.vue`](../frontend-vue/src/views/DashboardView.vue)) é o painel de
monitoramento do projeto: assim que a planilha é processada, os gráficos refletem
o novo estado da carteira. No lugar de temperatura e pressão, as variáveis
monitoradas são segmento, nível e faturamento.

O endpoint `GET /api/telemetria` expõe os últimos eventos de processamento — o
equivalente ao histórico de alarmes de um supervisório.

---

## 2. O que NÃO se aplica a este projeto

Registro explícito, exigido pelo plano de ensino:

| Tópico da ementa | Situação |
| ---------------- | -------- |
| 1. Dispositivos industriais — sensores eletromecânico, indutivo, capacitivo, magnético, ultrassônico, fotoelétrico | **Não implementado.** O projeto não lê grandezas físicas. |
| 1. Atuadores — pneumáticos, hidráulicos, motores, servomotores | **Não implementado.** Não há acionamento de nada. |
| 1. Robôs industriais | **Não implementado.** |
| 2. Controladores industriais — CLP, PC industrial | **Não implementado.** Não existe CLP no sistema. |
| 3.4.3 Enviar e receber sinais para o CLP | **Não implementado**, pela mesma razão: não há CLP para receber sinal. |

Esses tópicos foram estudados na disciplina e são compreendidos pelo grupo, mas
implementá-los aqui exigiria inventar um hardware que o problema da CTI não tem.
A demanda da empresa é tratar planilha comercial — forçar um sensor no meio disso
seria demonstração artificial, não solução.

---

## 3. Evidências para a apresentação

1. Print do dashboard atualizando após o upload (visualização em tempo real).
2. Print de `GET /api/telemetria` mostrando a trilha de execuções (monitoramento).
3. Esta tabela comparativa HTTP/REST × Modbus TCP.
4. Trecho de `AnaliseService.java` mostrando a integração entre os dois runtimes.
