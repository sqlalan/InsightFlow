# docs — Diagrama e protótipos

Documentos visuais do Sistema CTI Insights. O código e a documentação técnica
ficam nas pastas de cada módulo; aqui ficam apenas as imagens citadas nas
entregas das sprints.

## Diagrama

| Arquivo | O que mostra |
| ------- | ------------ |
| [`diagrama/diagrama-classes.png`](diagrama/diagrama-classes.png) | Diagrama de classes: Consultor, Cliente, Contrato, Servico, Insight (com os três subtipos) e Telemetria, com atributos, multiplicidades e a herança por `SINGLE_TABLE` |

O mesmo modelo está no código, em
[`backend-java/src/main/java/br/senai/ctiinsights/domain/`](../backend-java/src/main/java/br/senai/ctiinsights/domain/),
e no script do banco, em [`database/schema.sql`](../database/schema.sql).

## Protótipos das telas

| Arquivo | Tela |
| ------- | ---- |
| [`prototipos/upload.png`](prototipos/upload.png) | Envio da planilha |
| [`prototipos/upload-erro.png`](prototipos/upload-erro.png) | Envio da planilha com avisos de validação |
| [`prototipos/dashboard.png`](prototipos/dashboard.png) | Dashboard da carteira: indicadores, gráficos, carteira e insights |
| [`prototipos/dashboard-celular.png`](prototipos/dashboard-celular.png) | Dashboard em tela de celular |
| [`prototipos/menu-celular.png`](prototipos/menu-celular.png) | Menu lateral do painel em tela de celular |

As telas implementadas estão em
[`frontend-vue/src/views/`](../frontend-vue/src/views/) e
[`frontend-vue/src/components/`](../frontend-vue/src/components/).
