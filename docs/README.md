# docs — Telas e diagrama

Imagens usadas nas entregas das sprints. O código fica nas pastas de cada
módulo; aqui ficam apenas as evidências visuais.

## Diagrama (`diagrama/`)

| Arquivo | O que mostra |
| ------- | ------------ |
| `diagrama-classes.png` | Diagrama de classes (versão 1.2): Consultor, Cliente, Contrato, Servico, Insight com os três subtipos, e Telemetria, com atributos, multiplicidades e a herança por `SINGLE_TABLE` |

O mesmo modelo está no código, em
[`backend-java/src/main/java/br/senai/ctiinsights/domain/`](../backend-java/src/main/java/br/senai/ctiinsights/domain/),
e no script do banco, em [`database/schema.sql`](../database/schema.sql).

## Telas do sistema (`prototipos/`)

Capturas do sistema em funcionamento, na ordem do fluxo de uso.

| Arquivo | Tela |
| ------- | ---- |
| `upload-selecao-arquivo.png` | Escolha da planilha na tela de envio |
| `upload-erro.png` | Arquivo em formato não suportado: o envio fica bloqueado |
| `upload-avisos.png` | Avisos de validação encontrados na planilha |
| `upload-previa.png` | Ajustes automáticos e prévia dos dados tratados |
| `envio-confirmacao.png` | Confirmação do envio, com o resumo da planilha |
| `envio-processando.png` | Indicador de processamento durante a análise |
| `envio-concluido.png` | Resumo do processamento e acesso ao dashboard |
| `dashboard.png` | Dashboard da carteira: indicadores, gráficos, carteira e insights |
| `relatorios.png` | Relatórios: histórico de processamento, insights e exportação em CSV |

As telas implementadas estão em
[`frontend-vue/src/views/`](../frontend-vue/src/views/) e
[`frontend-vue/src/components/`](../frontend-vue/src/components/).
