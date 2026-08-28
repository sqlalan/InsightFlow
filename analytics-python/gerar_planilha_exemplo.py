"""
Gera uma planilha de exemplo com os mesmos vicios da planilha real da CTI.

Serve para dois momentos: testar o pipeline sem depender do arquivo da empresa
(que e confidencial) e para a demonstracao da apresentacao final, onde o grupo
precisa enviar uma planilha despadronizada e mostrar o tratamento automatico.

    python gerar_planilha_exemplo.py
"""

from __future__ import annotations

import random
from pathlib import Path

import pandas as pd

random.seed(42)

CONSULTORES = ["ana paula", "  Bruno Lima", "CARLA MENDES", "diego souza", "Bruno Lima "]

# Cada segmento aparece escrito de varias formas de proposito.
SEGMENTOS = [
    "IND.", "Industria", "INDUSTRIA", "industria ",
    "COM", "Comercio", "comercio", "Varejo",
    "SERV", "Servicos", "servico", "  Servicos",
    "GOV", "Governo", "EDU", "Educacao", "Saude", "AGRO",
    "",
]

NIVEIS = ["A", "a", " B", "b ", "C", "Nivel A", "classe c", ""]

SERVICOS = [
    "Link Dedicado", "link dedicado", "CLOUD", "Cloud Server", "Telefonia",
    "VPN", "Firewall", "Backup", "Wi-Fi Corporativo", "Monitoramento",
]

FATURAMENTOS = [
    "R$ 250.000,00", "180000", "1,2 mi", "R$ 4.500.000,00", "12000000",
    "Ate 360 mil", "360 mil a 4,8 mi", "acima de 30 mi", "", "R$ 780.000,00",
    "2,4 MI", "35000000", "90 mil",
]

STATUS = ["ATIVO", "ativo", "Encerrado", "SUSPENSO", ""]


def montar_servicos() -> str:
    escolhidos = random.sample(SERVICOS, k=random.randint(1, 3))
    separador = random.choice(["; ", " / ", ", ", " | "])
    return separador.join(escolhidos)


def gerar(linhas: int = 120) -> pd.DataFrame:
    registros = []
    for i in range(1, linhas + 1):
        registros.append({
            "Consultor Responsavel": random.choice(CONSULTORES),
            "Cliente CTI": f"CTI{i:04d}",
            "Segmento de Atuacao": random.choice(SEGMENTOS),
            "Nivel do Cliente": random.choice(NIVEIS),
            "Faixa de Faturamento": random.choice(FATURAMENTOS),
            "Servicos Contratados": montar_servicos() if random.random() > 0.05 else "",
            "Data Inicio": pd.Timestamp("2024-01-01") + pd.Timedelta(days=random.randint(0, 700)),
            "Status": random.choice(STATUS),
        })

    # dois codigos repetidos e uma linha sem codigo: o tratamento precisa lidar com isso
    registros.append(dict(registros[3]))
    registros.append(dict(registros[10]))
    orfao = dict(registros[5])
    orfao["Cliente CTI"] = ""
    registros.append(orfao)

    return pd.DataFrame(registros)


if __name__ == "__main__":
    destino = Path(__file__).parent / "planilha_exemplo.xlsx"
    gerar().to_excel(destino, index=False)
    print(f"[ok] planilha de exemplo gravada em {destino}")
