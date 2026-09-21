"""
Traducao das estatisticas em frases de negocio.

Numero solto nao ajuda o consultor da CTI: o que aparece no dashboard e o texto
"Indústria concentra 42,1% da carteira". Cada funcao daqui devolve dicionarios no
formato que o Back-end Java sabe converter em InsightSegmento, InsightFaturamento
e InsightServico.
"""

from __future__ import annotations

from collections import Counter

import pandas as pd


def _decimal(valor: float) -> str:
    """1.8 -> '1,8': o texto vai para a tela em português."""
    return f"{valor:.1f}".replace(".", ",")


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
            f"São {int(contagem.iloc[0])} de {total} clientes. "
            "Concentrar o esforço comercial aqui rende mais por contato."
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
                    "São segmentos com pouca presença na carteira: podem virar frente de "
                    "prospecção ou sair do foco comercial."
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
    assimetria = "Poucos clientes muito grandes puxam a média para cima"
    if media < mediana:
        assimetria = "Um grupo de clientes menores puxa a média para baixo"
    elif abs(media - mediana) / max(mediana, 1) < 0.1:
        assimetria = "A carteira é homogênea: os valores são próximos entre os clientes"

    resultado = [{
        "tipo": "FATURAMENTO",
        "faturamento_medio": round(media, 2),
        "faturamento_mediano": round(mediana, 2),
        "descricao": f"{assimetria}.",
    }]

    # O p-valor (Shapiro-Wilk) continua em indicadores.json; na tela vai só a leitura.
    normalidade = estatisticas.get("teste_normalidade", {})
    if normalidade.get("p_valor") is not None:
        normal = normalidade["p_valor"] > 0.05
        leitura = (
            "O faturamento se distribui de forma equilibrada: a média representa bem o cliente típico."
            if normal
            else "O faturamento é desigual entre os clientes: o valor mediano representa melhor o "
            "cliente típico do que a média."
        )
        resultado.append({
            "tipo": "FATURAMENTO",
            "faturamento_medio": None,
            "faturamento_mediano": None,
            "descricao": leitura,
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
        "descricao": "É o serviço principal da carteira e o candidato natural a um pacote combinado.",
    }]

    media_servicos = float(df["qtd_servicos"].mean())
    um_servico = int(df["qtd_servicos"].eq(1).sum())
    if um_servico:
        resultado.append({
            "tipo": "SERVICO",
            "servico": "Venda cruzada",
            # Sem total: o número aqui é de clientes, não de contratos.
            "total_contratos": None,
            "descricao": (
                f"{um_servico} clientes têm um único serviço contratado, contra a média de "
                f"{_decimal(media_servicos)} serviços por cliente. É a lista mais curta de "
                "abordagem para ampliar a receita."
            ),
        })
    return resultado


def insight_niveis(df: pd.DataFrame, estatisticas: dict) -> list[dict]:
    """Composicao por nivel A/B/C e associacao com o segmento."""
    if df.empty:
        return []
    proporcoes = df["nivel"].value_counts(normalize=True).mul(100).round(1).to_dict()
    composicao = ", ".join(f"{nivel}: {_decimal(valor)}%" for nivel, valor in sorted(proporcoes.items()))

    resultado = [{
        "tipo": "SEGMENTO",
        "segmento": "Composição por nível",
        "participacao_percentual": None,
        "descricao": f"Distribuição da carteira por nível de cliente: {composicao}.",
    }]

    # O p-valor (qui-quadrado) continua em indicadores.json; na tela vai só a leitura.
    associacao = estatisticas.get("teste_associacao", {})
    if associacao.get("p_valor") is not None:
        dependente = associacao["p_valor"] < 0.05
        leitura = (
            "O nível do cliente muda conforme o segmento: cada setor tem um perfil próprio."
            if dependente
            else "O nível do cliente não depende do segmento: a classificação se distribui de "
            "forma parecida entre os setores."
        )
        resultado.append({
            "tipo": "SEGMENTO",
            "segmento": "Segmento x nível",
            "participacao_percentual": None,
            "descricao": leitura,
        })
    return resultado


def insight_qualidade(relatorio: dict) -> list[dict]:
    """Transparencia sobre o que a planilha nao entregou (exigencia das hipoteses)."""
    problemas = []
    if relatorio.get("codigos_duplicados_removidos"):
        problemas.append(f"{relatorio['codigos_duplicados_removidos']} código(s) de cliente repetido(s)")
    if relatorio.get("faturamento_ausente"):
        problemas.append(f"{relatorio['faturamento_ausente']} cliente(s) sem faturamento informado")
    if relatorio.get("clientes_sem_servico"):
        problemas.append(f"{relatorio['clientes_sem_servico']} cliente(s) sem serviço informado")
    if not problemas:
        return []
    return [{
        "tipo": "SEGMENTO",
        "segmento": "Qualidade dos dados",
        "participacao_percentual": None,
        "descricao": (
            "A planilha recebida tinha " + ", ".join(problemas)
            + ". As análises que dependem desses campos ficam menos precisas."
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
