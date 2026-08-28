"""
Traducao das estatisticas em frases de negocio.

Numero solto nao ajuda o consultor da CTI: o que aparece no dashboard e o texto
"Industria concentra 42,1% da carteira". Cada funcao daqui devolve dicionarios no
formato que o Back-end Java sabe converter em InsightSegmento, InsightFaturamento
e InsightServico.
"""

from __future__ import annotations

from collections import Counter

import pandas as pd


def _moeda(valor: float) -> str:
    return f"R$ {valor:,.2f}".replace(",", "_").replace(".", ",").replace("_", ".")


def insight_segmentos(df: pd.DataFrame) -> list[dict]:
    """Concentracao da carteira por segmento de atuacao."""
    if df.empty:
        return []
    contagem = df["segmento"].value_counts()
    total = int(contagem.sum())
    lider = contagem.index[0]
    participacao = float(contagem.iloc[0]) / total * 100

    resultado = [{
        "tipo": "SEGMENTO",
        "segmento": lider,
        "participacao_percentual": round(participacao, 2),
        "descricao": (
            f"Sao {int(contagem.iloc[0])} de {total} clientes. "
            "Esforco comercial concentrado aqui rende mais por contato."
        ),
    }]

    if len(contagem) > 1:
        cauda = contagem[contagem <= max(1, total * 0.05)]
        if not cauda.empty:
            resultado.append({
                "tipo": "SEGMENTO",
                "segmento": ", ".join(cauda.index[:3]),
                "participacao_percentual": round(float(cauda.sum()) / total * 100, 2),
                "descricao": (
                    "Segmentos com presenca marginal na carteira: ou viram frente de "
                    "prospeccao, ou saem do foco comercial."
                ),
            })
    return resultado


def insight_faturamento(df: pd.DataFrame, estatisticas: dict) -> list[dict]:
    """Perfil de faturamento e o que a diferenca media/mediana revela."""
    serie = df["faturamento_anual"].dropna()
    if serie.empty:
        return []

    media = float(serie.mean())
    mediana = float(serie.median())
    assimetria = "a carteira tem poucos clientes muito grandes puxando a media para cima"
    if media < mediana:
        assimetria = "a carteira e puxada para baixo por um grupo de clientes menores"
    elif abs(media - mediana) / max(mediana, 1) < 0.1:
        assimetria = "a carteira e homogenea: media e mediana praticamente iguais"

    resultado = [{
        "tipo": "FATURAMENTO",
        "faturamento_medio": round(media, 2),
        "faturamento_mediano": round(mediana, 2),
        "descricao": f"Com desvio padrao de {_moeda(float(serie.std(ddof=1)))}, {assimetria}.",
    }]

    normalidade = estatisticas.get("teste_normalidade", {})
    if normalidade.get("p_valor") is not None:
        normal = normalidade["p_valor"] > 0.05
        leitura = (
            "distribuicao compativel com a normal, entao media e desvio padrao descrevem bem a carteira"
            if normal
            else "distribuicao longe da normal, entao mediana e quartis descrevem melhor a carteira do que a media"
        )
        resultado.append({
            "tipo": "FATURAMENTO",
            "faturamento_medio": round(media, 2),
            "faturamento_mediano": round(mediana, 2),
            "descricao": (
                f"Shapiro-Wilk com p = {normalidade['p_valor']:.4f}: {leitura}."
            ),
        })
    return resultado


def insight_servicos(df: pd.DataFrame) -> list[dict]:
    """Servicos mais contratados e espaco para venda cruzada."""
    contador = Counter(servico for lista in df["servicos_contratados"] for servico in lista)
    if not contador:
        return []

    nome, quantidade = contador.most_common(1)[0]
    resultado = [{
        "tipo": "SERVICO",
        "servico": nome,
        "total_contratos": int(quantidade),
        "descricao": "E o servico ancora da carteira e o candidato natural a pacote combinado.",
    }]

    media_servicos = float(df["qtd_servicos"].mean())
    um_servico = int(df["qtd_servicos"].eq(1).sum())
    if um_servico:
        resultado.append({
            "tipo": "SERVICO",
            "servico": "Venda cruzada",
            "total_contratos": um_servico,
            "descricao": (
                f"{um_servico} clientes tem um unico servico contratado, contra media de "
                f"{media_servicos:.1f} servicos por cliente: e a lista de abordagem mais curta para ampliar receita."
            ),
        })
    return resultado


def insight_niveis(df: pd.DataFrame, estatisticas: dict) -> list[dict]:
    """Composicao por nivel A/B/C e associacao com o segmento."""
    if df.empty:
        return []
    proporcoes = df["nivel"].value_counts(normalize=True).mul(100).round(1).to_dict()
    composicao = ", ".join(f"{nivel}: {valor}%" for nivel, valor in sorted(proporcoes.items()))

    resultado = [{
        "tipo": "SEGMENTO",
        "segmento": "Composicao por nivel",
        "participacao_percentual": float(proporcoes.get("A", 0.0)),
        "descricao": f"Distribuicao da carteira por classificacao ({composicao}).",
    }]

    associacao = estatisticas.get("teste_associacao", {})
    if associacao.get("p_valor") is not None:
        dependente = associacao["p_valor"] < 0.05
        leitura = (
            "ha associacao estatistica entre segmento e nivel: o perfil de cliente muda conforme o setor"
            if dependente
            else "nao ha associacao estatistica entre segmento e nivel: a classificacao se distribui de forma parecida entre os setores"
        )
        resultado.append({
            "tipo": "SEGMENTO",
            "segmento": "Segmento x nivel",
            "participacao_percentual": None,
            "descricao": f"Qui-quadrado com p = {associacao['p_valor']:.4f}: {leitura}.",
        })
    return resultado


def insight_qualidade(relatorio: dict) -> list[dict]:
    """Transparencia sobre o que a planilha nao entregou (exigencia das hipoteses)."""
    problemas = []
    if relatorio.get("codigos_duplicados_removidos"):
        problemas.append(f"{relatorio['codigos_duplicados_removidos']} codigos CTI duplicados")
    if relatorio.get("faturamento_ausente"):
        problemas.append(f"{relatorio['faturamento_ausente']} clientes sem faturamento informado")
    if relatorio.get("clientes_sem_servico"):
        problemas.append(f"{relatorio['clientes_sem_servico']} clientes sem servico informado")
    if not problemas:
        return []
    return [{
        "tipo": "SEGMENTO",
        "segmento": "Qualidade dos dados",
        "participacao_percentual": None,
        "descricao": (
            "A base recebida tinha " + ", ".join(problemas)
            + ". As analises que dependem desses campos tem alcance reduzido."
        ),
    }]


def gerar(df: pd.DataFrame, estatisticas: dict, relatorio: dict) -> list[dict]:
    """Lista final de insights, na ordem em que aparecem no dashboard."""
    return [
        *insight_segmentos(df),
        *insight_faturamento(df, estatisticas),
        *insight_servicos(df),
        *insight_niveis(df, estatisticas),
        *insight_qualidade(relatorio),
    ]
