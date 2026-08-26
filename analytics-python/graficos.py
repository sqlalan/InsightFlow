"""
Geracao dos graficos do relatorio de Ciencia de Dados.

Cobre os quatro tipos exigidos na ementa (linha, dispersao, histograma e
boxplot) mais os dois graficos de dados categoricos (barras e pizza). Os PNG
ficam em output/graficos e entram no relatorio; no dashboard web os mesmos
recortes sao redesenhados pelo Chart.js a partir de /api/indicadores.
"""

from __future__ import annotations

from pathlib import Path

import matplotlib

matplotlib.use("Agg")  # sem interface grafica: roda igual no Render e na maquina do grupo

import matplotlib.pyplot as plt
import pandas as pd

PALETA = ["#12a8d4", "#2f6bf0", "#34d399", "#f59e0b", "#a855f7", "#ef4444", "#64748b"]


def _finalizar(figura, destino: Path, nome: str) -> str:
    destino.mkdir(parents=True, exist_ok=True)
    caminho = destino / nome
    figura.tight_layout()
    figura.savefig(caminho, dpi=140)
    plt.close(figura)
    return caminho.name


def barras_por_segmento(df: pd.DataFrame, destino: Path) -> str | None:
    """Dados categoricos: quantos clientes por segmento de atuacao."""
    contagem = df["segmento"].value_counts()
    if contagem.empty:
        return None
    figura, eixo = plt.subplots(figsize=(8, 4.5))
    eixo.bar(contagem.index, contagem.values, color=PALETA[0])
    eixo.set_title("Clientes por segmento")
    eixo.set_xlabel("Segmento")
    eixo.set_ylabel("Clientes")
    eixo.tick_params(axis="x", rotation=30)
    return _finalizar(figura, destino, "grafico_01_barras_segmento.png")


def pizza_por_nivel(df: pd.DataFrame, destino: Path) -> str | None:
    """Proporcao da carteira em cada nivel A/B/C."""
    contagem = df["nivel"].value_counts().sort_index()
    if contagem.empty:
        return None
    figura, eixo = plt.subplots(figsize=(5.5, 5.5))
    eixo.pie(contagem.values, labels=contagem.index, autopct="%1.1f%%",
             colors=PALETA[: len(contagem)], startangle=90)
    eixo.set_title("Distribuicao por nivel de cliente")
    eixo.axis("equal")
    return _finalizar(figura, destino, "grafico_02_pizza_nivel.png")


def linha_evolucao(df: pd.DataFrame, destino: Path) -> str | None:
    """Serie temporal: contratacoes por mes."""
    datas = df["data_inicio"].dropna()
    if datas.empty:
        return None
    serie = datas.dt.to_period("M").value_counts().sort_index()
    figura, eixo = plt.subplots(figsize=(8, 4.5))
    eixo.plot([str(p) for p in serie.index], serie.values, marker="o", color=PALETA[1])
    eixo.set_title("Evolucao das contratacoes por mes")
    eixo.set_xlabel("Mes")
    eixo.set_ylabel("Contratacoes")
    eixo.tick_params(axis="x", rotation=45)
    eixo.grid(alpha=0.2)
    return _finalizar(figura, destino, "grafico_03_linha_evolucao.png")


def dispersao_faturamento_servicos(df: pd.DataFrame, destino: Path) -> str | None:
    """Dispersao: faturamento anual x quantidade de servicos contratados."""
    dados = df.dropna(subset=["faturamento_anual"])
    if dados.empty:
        return None
    figura, eixo = plt.subplots(figsize=(7, 4.5))
    eixo.scatter(dados["faturamento_anual"], dados["qtd_servicos"], alpha=0.7, color=PALETA[2])
    eixo.set_title("Faturamento anual x servicos contratados")
    eixo.set_xlabel("Faturamento anual (R$)")
    eixo.set_ylabel("Servicos contratados")
    eixo.grid(alpha=0.2)
    return _finalizar(figura, destino, "grafico_04_dispersao.png")


def histograma_faturamento(df: pd.DataFrame, destino: Path) -> str | None:
    """Histograma: como o faturamento se distribui na carteira."""
    serie = df["faturamento_anual"].dropna()
    if serie.empty:
        return None
    figura, eixo = plt.subplots(figsize=(7, 4.5))
    eixo.hist(serie, bins=min(15, max(5, len(serie) // 4)), color=PALETA[3], edgecolor="white")
    eixo.set_title("Distribuicao do faturamento anual")
    eixo.set_xlabel("Faturamento anual (R$)")
    eixo.set_ylabel("Clientes")
    eixo.grid(alpha=0.2)
    return _finalizar(figura, destino, "grafico_05_histograma.png")


def boxplot_por_segmento(df: pd.DataFrame, destino: Path) -> str | None:
    """Boxplot: dispersao do faturamento dentro de cada segmento."""
    dados = df.dropna(subset=["faturamento_anual"])
    grupos = [g["faturamento_anual"].values for _, g in dados.groupby("segmento") if len(g) >= 2]
    rotulos = [nome for nome, g in dados.groupby("segmento") if len(g) >= 2]
    if not grupos:
        return None
    figura, eixo = plt.subplots(figsize=(8, 4.5))
    eixo.boxplot(grupos, tick_labels=rotulos, patch_artist=True,
                 boxprops={"facecolor": PALETA[4], "alpha": 0.6})
    eixo.set_title("Faturamento por segmento")
    eixo.set_ylabel("Faturamento anual (R$)")
    eixo.tick_params(axis="x", rotation=30)
    eixo.grid(alpha=0.2, axis="y")
    return _finalizar(figura, destino, "grafico_06_boxplot.png")


def gerar_todos(df: pd.DataFrame, destino: Path) -> list[str]:
    """Roda todos os graficos e devolve os nomes dos arquivos efetivamente criados."""
    producoes = [
        barras_por_segmento,
        pizza_por_nivel,
        linha_evolucao,
        dispersao_faturamento_servicos,
        histograma_faturamento,
        boxplot_por_segmento,
    ]
    gerados = []
    for producao in producoes:
        nome = producao(df, destino)
        if nome:
            gerados.append(nome)
    return gerados
