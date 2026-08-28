"""
Ponto de entrada do modulo de Ciencia de Dados do Sistema CTI Insights.

Fluxo: le a planilha -> trata (limpeza.py) -> calcula estatisticas -> gera
graficos (graficos.py) -> escreve insights (insights.py) -> grava os JSON que o
Back-end Java le depois de chamar este script via ProcessBuilder.

Uso:
    python analise.py --entrada planilha.xlsx --saida output
"""

from __future__ import annotations

import argparse
import json
import math
import sys
from collections import Counter
from pathlib import Path

import pandas as pd
from scipy import stats

import graficos
import insights as modulo_insights
import limpeza


# ----------------------------------------------------------------------
# Estatistica descritiva
# ----------------------------------------------------------------------
def descrever(serie: pd.Series) -> dict:
    """Tendencia central e dispersao de uma variavel quantitativa."""
    limpa = serie.dropna()
    if limpa.empty:
        return {}
    moda = limpa.mode()
    return {
        "n": int(limpa.count()),
        "media": round(float(limpa.mean()), 2),
        "mediana": round(float(limpa.median()), 2),
        "moda": round(float(moda.iloc[0]), 2) if not moda.empty else None,
        "desvio_padrao": round(float(limpa.std(ddof=1)), 2) if limpa.count() > 1 else 0.0,
        "variancia": round(float(limpa.var(ddof=1)), 2) if limpa.count() > 1 else 0.0,
        "amplitude": round(float(limpa.max() - limpa.min()), 2),
        "minimo": round(float(limpa.min()), 2),
        "maximo": round(float(limpa.max()), 2),
        "q1": round(float(limpa.quantile(0.25)), 2),
        "q3": round(float(limpa.quantile(0.75)), 2),
        "p90": round(float(limpa.quantile(0.90)), 2),
    }


def descrever_por_grupo(df: pd.DataFrame, coluna_grupo: str) -> dict:
    """Mesma estatistica, quebrada por segmento ou por nivel."""
    resultado = {}
    for chave, grupo in df.groupby(coluna_grupo):
        resumo = descrever(grupo["faturamento_anual"])
        if resumo:
            resumo["clientes"] = int(len(grupo))
            resultado[str(chave)] = resumo
    return resultado


# ----------------------------------------------------------------------
# Testes de hipotese e associacao
# ----------------------------------------------------------------------
def teste_normalidade(serie: pd.Series) -> dict:
    """
    Shapiro-Wilk sobre o faturamento.

    H0: a amostra vem de uma distribuicao normal.
    p <= 0.05 rejeita H0 e indica que mediana/quartis descrevem melhor a carteira.
    """
    limpa = serie.dropna()
    if len(limpa) < 3:
        return {"aplicado": False, "motivo": "amostra menor que 3 observacoes", "p_valor": None}
    estatistica, p_valor = stats.shapiro(limpa)
    return {
        "aplicado": True,
        "teste": "Shapiro-Wilk",
        "hipotese_nula": "o faturamento segue distribuicao normal",
        "estatistica": round(float(estatistica), 4),
        "p_valor": round(float(p_valor), 6),
        "alfa": 0.05,
        "rejeita_h0": bool(p_valor <= 0.05),
    }


def teste_associacao(df: pd.DataFrame) -> dict:
    """
    Qui-quadrado entre segmento e nivel do cliente.

    H0: as duas variaveis categoricas sao independentes.
    """
    tabela = pd.crosstab(df["segmento"], df["nivel"])
    if tabela.shape[0] < 2 or tabela.shape[1] < 2:
        return {"aplicado": False, "motivo": "menos de duas categorias em uma das variaveis", "p_valor": None}
    qui, p_valor, graus, _ = stats.chi2_contingency(tabela)
    return {
        "aplicado": True,
        "teste": "Qui-quadrado de independencia",
        "hipotese_nula": "segmento e nivel do cliente sao independentes",
        "estatistica": round(float(qui), 4),
        "graus_liberdade": int(graus),
        "p_valor": round(float(p_valor), 6),
        "alfa": 0.05,
        "rejeita_h0": bool(p_valor <= 0.05),
    }


def correlacao(df: pd.DataFrame) -> dict:
    """Correlacao entre faturamento e quantidade de servicos contratados."""
    dados = df.dropna(subset=["faturamento_anual"])
    if len(dados) < 3 or dados["qtd_servicos"].nunique() < 2:
        return {"aplicado": False, "motivo": "dados insuficientes", "coeficiente": None}
    pearson, p_pearson = stats.pearsonr(dados["faturamento_anual"], dados["qtd_servicos"])
    spearman, p_spearman = stats.spearmanr(dados["faturamento_anual"], dados["qtd_servicos"])
    return {
        "aplicado": True,
        "pearson": round(float(pearson), 4),
        "p_valor_pearson": round(float(p_pearson), 6),
        "spearman": round(float(spearman), 4),
        "p_valor_spearman": round(float(p_spearman), 6),
        "leitura": classificar_correlacao(float(pearson)),
    }


def classificar_correlacao(coeficiente: float) -> str:
    forca = abs(coeficiente)
    if forca < 0.2:
        intensidade = "praticamente inexistente"
    elif forca < 0.4:
        intensidade = "fraca"
    elif forca < 0.6:
        intensidade = "moderada"
    elif forca < 0.8:
        intensidade = "forte"
    else:
        intensidade = "muito forte"
    sentido = "positiva" if coeficiente >= 0 else "negativa"
    return f"correlacao {intensidade} e {sentido}"


def calcular_estatisticas(df: pd.DataFrame) -> dict:
    """Reune tudo que a UC de Ciencia de Dados precisa demonstrar."""
    total = len(df)
    contador_servicos = Counter(s for lista in df["servicos_contratados"] for s in lista)
    return {
        "total_clientes": total,
        "total_contratos": int(df["qtd_servicos"].sum()),
        "faturamento": descrever(df["faturamento_anual"]),
        "faturamento_por_segmento": descrever_por_grupo(df, "segmento"),
        "faturamento_por_nivel": descrever_por_grupo(df, "nivel"),
        "clientes_por_segmento": df["segmento"].value_counts().to_dict(),
        "clientes_por_nivel": df["nivel"].value_counts().sort_index().to_dict(),
        "clientes_por_faixa": df["faixa_faturamento"].value_counts().to_dict(),
        "proporcao_por_nivel": df["nivel"].value_counts(normalize=True).mul(100).round(2).sort_index().to_dict(),
        "contratos_por_servico": dict(contador_servicos.most_common()),
        "servicos_por_cliente": descrever(df["qtd_servicos"]),
        "evolucao_contratacoes": evolucao_mensal(df),
        "tabela_segmento_x_nivel": pd.crosstab(df["segmento"], df["nivel"]).to_dict(),
        "teste_normalidade": teste_normalidade(df["faturamento_anual"]),
        "teste_associacao": teste_associacao(df),
        "correlacao_faturamento_servicos": correlacao(df),
    }


def evolucao_mensal(df: pd.DataFrame) -> dict:
    datas = df["data_inicio"].dropna()
    if datas.empty:
        return {}
    serie = datas.dt.to_period("M").value_counts().sort_index()
    return {str(periodo): int(quantidade) for periodo, quantidade in serie.items()}


# ----------------------------------------------------------------------
# Saida para o Back-end
# ----------------------------------------------------------------------
def montar_clientes(df: pd.DataFrame) -> list[dict]:
    """Formato consumido pelo ImportacaoService do Spring Boot."""
    registros = []
    for _, linha in df.iterrows():
        faturamento = linha["faturamento_anual"]
        data = linha["data_inicio"]
        registros.append({
            "codigo_cti": linha["cliente_cti"],
            "consultor": linha["consultor"],
            "segmento": linha["segmento"],
            "nivel": linha["nivel"],
            "faturamento_anual": None if faturamento is None or pd.isna(faturamento) else float(faturamento),
            "faixa_faturamento": linha["faixa_faturamento"],
            "servicos_contratados": list(linha["servicos_contratados"]),
            "data_inicio": None if pd.isna(data) else data.date().isoformat(),
            "status": str(linha.get("status", "ATIVO")).upper(),
        })
    return registros


def sanitizar(valor):
    """NaN e infinito nao sao JSON valido: viram null antes de gravar."""
    if isinstance(valor, dict):
        return {str(chave): sanitizar(item) for chave, item in valor.items()}
    if isinstance(valor, (list, tuple)):
        return [sanitizar(item) for item in valor]
    if isinstance(valor, float) and (math.isnan(valor) or math.isinf(valor)):
        return None
    if hasattr(valor, "item"):
        return sanitizar(valor.item())
    return valor


def gravar_json(dados, caminho: Path) -> None:
    caminho.parent.mkdir(parents=True, exist_ok=True)
    caminho.write_text(
        json.dumps(sanitizar(dados), ensure_ascii=False, indent=2),
        encoding="utf-8",
    )


def executar(entrada: Path, saida: Path) -> dict:
    bruto = limpeza.carregar(str(entrada))
    df, relatorio = limpeza.tratar(bruto)
    if df.empty:
        raise ValueError("Depois do tratamento nao sobrou nenhuma linha valida na planilha.")

    estatisticas = calcular_estatisticas(df)
    estatisticas["relatorio_tratamento"] = relatorio
    lista_insights = modulo_insights.gerar(df, estatisticas, relatorio)
    arquivos = graficos.gerar_todos(df, saida / "graficos")
    estatisticas["graficos"] = arquivos

    gravar_json(montar_clientes(df), saida / "clientes.json")
    gravar_json(estatisticas, saida / "indicadores.json")
    gravar_json(lista_insights, saida / "insights.json")
    df.to_csv(saida / "base_tratada.csv", index=False, encoding="utf-8")

    return {
        "clientes": len(df),
        "insights": len(lista_insights),
        "graficos": len(arquivos),
        "relatorio": relatorio,
    }


def main() -> int:
    parser = argparse.ArgumentParser(description="Tratamento e analise da planilha comercial da CTI")
    parser.add_argument("--entrada", required=True, help="caminho da planilha .xlsx enviada")
    parser.add_argument("--saida", default="output", help="pasta onde os JSON e graficos sao gravados")
    argumentos = parser.parse_args()

    entrada = Path(argumentos.entrada)
    if not entrada.exists():
        print(f"[erro] planilha nao encontrada: {entrada}", file=sys.stderr)
        return 2

    try:
        resumo = executar(entrada, Path(argumentos.saida))
    except ValueError as erro:
        print(f"[erro] {erro}", file=sys.stderr)
        return 3

    print(
        f"[ok] {resumo['clientes']} clientes tratados, "
        f"{resumo['insights']} insights e {resumo['graficos']} graficos gerados."
    )
    print(f"[ok] relatorio de tratamento: {json.dumps(resumo['relatorio'], ensure_ascii=False)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
