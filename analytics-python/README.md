# analytics-python — Ciencia de Dados

Modulo de tratamento e analise da planilha comercial da CTI. E chamado pelo
Back-end Java (`ProcessBuilder`) a cada upload, mas roda sozinho na linha de
comando durante o desenvolvimento.

## Instalacao

```bash
pip install -r requirements.txt
```

## Execucao

```bash
python analise.py --entrada exemplos/CTI_Insights_modelo_upload_aula.xlsx --saida output
python -m unittest discover tests           # testes do tratamento
```

## Arquivos

| Arquivo      | Papel |
| ------------ | ----- |
| `limpeza.py` | padroniza segmento/nivel/faturamento/servicos e remove duplicados |
| `analise.py` | ponto de entrada: estatistica descritiva, testes de hipotese e correlacao |
| `insights.py`| traduz numero em frase de negocio para o dashboard |
| `graficos.py`| linha, dispersao, histograma, boxplot, barras e pizza em PNG |

## Saida (`output/`)

| Arquivo             | Consumidor |
| ------------------- | ---------- |
| `clientes.json`     | Back-end Java grava no PostgreSQL |
| `indicadores.json`  | estatisticas completas, usadas no relatorio |
| `insights.json`     | Back-end Java transforma em entidades `Insight` |
| `base_tratada.csv`  | conferencia manual do tratamento |
| `graficos/*.png`    | relatorio da UC de Ciencia de Dados |
