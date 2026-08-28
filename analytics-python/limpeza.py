"""
Tratamento e padronizacao da planilha comercial da CTI.

A planilha e preenchida a mao por pessoas diferentes: o mesmo segmento aparece
como "IND.", "Industria" e "INDUSTRIA", ha espacos sobrando, celulas em branco e
capitalizacao misturada. Este modulo transforma esse arquivo bruto em um
DataFrame previsivel, que os demais modulos podem usar sem checar caso a caso.
"""

from __future__ import annotations

import re
import unicodedata

import pandas as pd

# Colunas que o restante do pipeline espera encontrar depois do tratamento.
COLUNAS_OBRIGATORIAS = [
    "consultor",
    "cliente_cti",
    "segmento",
    "nivel",
    "faturamento_anual",
    "servicos_contratados",
]

# Variacoes de escrita encontradas na planilha -> forma canonica.
SEGMENTOS_CANONICOS = {
    "ind": "Industria",
    "industria": "Industria",
    "industrias": "Industria",
    "industrial": "Industria",
    "com": "Comercio",
    "comercio": "Comercio",
    "comercial": "Comercio",
    "varejo": "Comercio",
    "serv": "Servicos",
    "servico": "Servicos",
    "servicos": "Servicos",
    "prestador de servicos": "Servicos",
    "gov": "Governo",
    "governo": "Governo",
    "publico": "Governo",
    "setor publico": "Governo",
    "edu": "Educacao",
    "educacao": "Educacao",
    "ensino": "Educacao",
    "saude": "Saude",
    "hospitalar": "Saude",
    "agro": "Agronegocio",
    "agronegocio": "Agronegocio",
    "rural": "Agronegocio",
}

# Faixas escritas por extenso na planilha -> valor representativo em reais.
FAIXAS_TEXTUAIS = {
    "ate 360 mil": 180_000.0,
    "ate 360mil": 180_000.0,
    "360 mil a 4,8 mi": 2_580_000.0,
    "360 mil a 4,8 milhoes": 2_580_000.0,
    "4,8 mi a 30 mi": 17_400_000.0,
    "4,8 milhoes a 30 milhoes": 17_400_000.0,
    "acima de 30 mi": 45_000_000.0,
    "acima de 30 milhoes": 45_000_000.0,
}

SEPARADORES_DE_SERVICO = re.compile(r"[;/|]|,\s|\se\s")


def normalizar_texto(valor: object) -> str:
    """Minusculas, sem acento, sem pontuacao de borda e sem espaco duplicado."""
    if valor is None or (isinstance(valor, float) and pd.isna(valor)):
        return ""
    texto = str(valor).strip()
    texto = unicodedata.normalize("NFD", texto)
    texto = "".join(c for c in texto if unicodedata.category(c) != "Mn")
    texto = texto.lower().strip(" .;,-")
    return re.sub(r"\s+", " ", texto)


def normalizar_cabecalho(nome: object) -> str:
    """'Faturamento Anual' e 'FATURAMENTO  ANUAL' viram 'faturamento_anual'."""
    base = normalizar_texto(nome)
    base = re.sub(r"[^a-z0-9]+", "_", base).strip("_")
    equivalentes = {
        "cliente": "cliente_cti",
        "codigo_cti": "cliente_cti",
        "cliente_cti_codigo": "cliente_cti",
        "codigo": "cliente_cti",
        "consultor_responsavel": "consultor",
        "segmento_de_atuacao": "segmento",
        "nivel_do_cliente": "nivel",
        "classificacao": "nivel",
        "faixa_de_faturamento": "faturamento_anual",
        "faixa_faturamento_anual": "faturamento_anual",
        "faturamento": "faturamento_anual",
        "servicos": "servicos_contratados",
        "servico_contratado": "servicos_contratados",
        "servicos_contratado": "servicos_contratados",
    }
    return equivalentes.get(base, base)


def padronizar_segmento(valor: object) -> str:
    """Unifica as variacoes de escrita do segmento; vazio vira 'Nao informado'."""
    chave = normalizar_texto(valor)
    if not chave:
        return "Nao informado"
    if chave in SEGMENTOS_CANONICOS:
        return SEGMENTOS_CANONICOS[chave]
    # Ainda tenta casar pelo comeco da palavra (ex.: "industria de alimentos").
    for prefixo, canonico in SEGMENTOS_CANONICOS.items():
        if chave.startswith(prefixo):
            return canonico
    return chave.title()


def padronizar_nivel(valor: object) -> str:
    """Aceita 'a', 'A ', 'Nivel A', 'classe a' e devolve sempre A, B ou C."""
    texto = normalizar_texto(valor).replace("nivel", "").replace("classe", "").strip()
    letra = texto[:1].upper() if texto else ""
    return letra if letra in {"A", "B", "C"} else "C"


def padronizar_status(valor: object) -> str:
    """Celula vazia, 'nan' ou lixo de digitacao viram ATIVO, o padrao da CTI."""
    texto = normalizar_texto(valor)
    if texto.startswith("encerr") or texto.startswith("cancel") or texto.startswith("inativ"):
        return "ENCERRADO"
    if texto.startswith("suspens"):
        return "SUSPENSO"
    return "ATIVO"


def converter_faturamento(valor: object) -> float | None:
    """Converte 'R$ 1.250.000,00', '1250000', '1,2 mi' e faixas por extenso."""
    if valor is None or (isinstance(valor, float) and pd.isna(valor)):
        return None
    if isinstance(valor, (int, float)):
        return float(valor) if float(valor) > 0 else None

    texto = normalizar_texto(valor)
    if not texto:
        return None
    if texto in FAIXAS_TEXTUAIS:
        return FAIXAS_TEXTUAIS[texto]

    multiplicador = 1.0
    if "mi" in texto or "milh" in texto:
        multiplicador = 1_000_000.0
    elif "mil" in texto or texto.endswith("k"):
        multiplicador = 1_000.0

    numeros = re.sub(r"[^0-9,.]", "", texto)
    if not numeros:
        return None
    if multiplicador > 1:
        # "1,2 mi" -> a virgula e decimal
        numeros = numeros.replace(".", "").replace(",", ".")
    else:
        # "1.250.000,00" -> ponto e separador de milhar
        numeros = numeros.replace(".", "").replace(",", ".")
    try:
        resultado = float(numeros) * multiplicador
    except ValueError:
        return None
    return resultado if resultado > 0 else None


def separar_servicos(valor: object) -> list[str]:
    """Quebra 'Link Dedicado; cloud / TELEFONIA' em uma lista padronizada."""
    texto = str(valor).strip() if valor is not None and not pd.isna(valor) else ""
    if not texto:
        return []
    partes = SEPARADORES_DE_SERVICO.split(texto)
    servicos = []
    for parte in partes:
        limpo = re.sub(r"\s+", " ", str(parte).strip(" .;,-"))
        if limpo:
            servicos.append(limpo.title())
    # remove repeticao mantendo a ordem original
    return list(dict.fromkeys(servicos))


def carregar(caminho: str) -> pd.DataFrame:
    """Le a planilha e renomeia o cabecalho para o padrao interno."""
    bruto = pd.read_excel(caminho, dtype=object)
    bruto.columns = [normalizar_cabecalho(coluna) for coluna in bruto.columns]
    faltantes = [c for c in COLUNAS_OBRIGATORIAS if c not in bruto.columns]
    if faltantes:
        raise ValueError(f"Colunas obrigatorias ausentes na planilha: {', '.join(faltantes)}")
    return bruto


def tratar(bruto: pd.DataFrame) -> tuple[pd.DataFrame, dict]:
    """Aplica a padronizacao e devolve o DataFrame limpo mais o relatorio do que mudou."""
    total_original = len(bruto)
    df = bruto.copy()

    df["cliente_cti"] = df["cliente_cti"].map(lambda v: normalizar_texto(v).upper())
    df["consultor"] = df["consultor"].map(lambda v: str(v).strip().title() if pd.notna(v) else "Nao informado")
    df["segmento"] = df["segmento"].map(padronizar_segmento)
    df["nivel"] = df["nivel"].map(padronizar_nivel)
    df["faturamento_anual"] = df["faturamento_anual"].map(converter_faturamento)
    df["servicos_contratados"] = df["servicos_contratados"].map(separar_servicos)

    if "data_inicio" in df.columns:
        df["data_inicio"] = pd.to_datetime(df["data_inicio"], errors="coerce", dayfirst=True)
    else:
        df["data_inicio"] = pd.NaT
    if "status" not in df.columns:
        df["status"] = "ATIVO"
    df["status"] = df["status"].map(padronizar_status)

    sem_codigo = int(df["cliente_cti"].eq("").sum())
    df = df[df["cliente_cti"] != ""]

    duplicados = int(df["cliente_cti"].duplicated().sum())
    df = df.drop_duplicates(subset="cliente_cti", keep="last")

    df["qtd_servicos"] = df["servicos_contratados"].map(len)
    df["faixa_faturamento"] = df["faturamento_anual"].map(classificar_faixa)

    relatorio = {
        "linhas_originais": total_original,
        "linhas_tratadas": len(df),
        "linhas_sem_codigo_removidas": sem_codigo,
        "codigos_duplicados_removidos": duplicados,
        "faturamento_ausente": int(df["faturamento_anual"].isna().sum()),
        "clientes_sem_servico": int(df["qtd_servicos"].eq(0).sum()),
        "segmentos_encontrados": sorted(df["segmento"].unique().tolist()),
    }
    return df.reset_index(drop=True), relatorio


def classificar_faixa(faturamento: float | None) -> str:
    """Mesmos cortes usados no enum FaixaFaturamento do Back-end."""
    if faturamento is None or pd.isna(faturamento):
        return "Nao informado"
    if faturamento <= 360_000:
        return "Ate R$ 360 mil"
    if faturamento <= 4_800_000:
        return "R$ 360 mil a R$ 4,8 mi"
    if faturamento <= 30_000_000:
        return "R$ 4,8 mi a R$ 30 mi"
    return "Acima de R$ 30 mi"
