"""
Testes do tratamento da planilha (limpeza.py).

Rodar de dentro de analytics-python:
    python -m unittest discover tests
"""

import unittest

import pandas as pd

import limpeza


class TestPadronizacao(unittest.TestCase):

    def test_cabecalho_do_modelo_da_aula(self):
        self.assertEqual(limpeza.normalizar_cabecalho("codigo_cliente"), "cliente_cti")
        self.assertEqual(limpeza.normalizar_cabecalho("nivel_cliente"), "nivel")
        self.assertEqual(limpeza.normalizar_cabecalho("Data Contratação"), "data_inicio")

    def test_segmento_escrito_de_formas_diferentes(self):
        for escrito in ["IND.", "Industria", "INDUSTRIA", " industria "]:
            self.assertEqual(limpeza.padronizar_segmento(escrito), "Indústria")
        self.assertEqual(limpeza.padronizar_segmento(None), "Não informado")

    def test_nivel(self):
        self.assertEqual(limpeza.padronizar_nivel("a"), "A")
        self.assertEqual(limpeza.padronizar_nivel("Nivel B"), "B")
        self.assertEqual(limpeza.padronizar_nivel("classe c"), "C")

    def test_faturamento(self):
        self.assertEqual(limpeza.converter_faturamento("R$ 1.250.000,00"), 1_250_000.0)
        self.assertEqual(limpeza.converter_faturamento("1,2 mi"), 1_200_000.0)
        self.assertEqual(limpeza.converter_faturamento(850000), 850_000.0)
        self.assertIsNone(limpeza.converter_faturamento(""))

    def test_servicos(self):
        self.assertEqual(
            limpeza.separar_servicos("Link Dedicado; cloud / TELEFONIA"),
            ["Link Dedicado", "Cloud", "Telefonia"],
        )
        self.assertEqual(limpeza.separar_servicos(None), [])


class TestTratamento(unittest.TestCase):

    def test_codigo_repetido_mantem_a_ultima_linha(self):
        bruto = pd.DataFrame({
            "cliente_cti": ["cti001", "CTI001", ""],
            "consultor": ["ana souza", "Ana Souza", "Bruno"],
            "segmento": ["IND", "Comercio", "GOV"],
            "nivel": ["a", "b", "c"],
            "faturamento_anual": ["1 mi", "2 mi", "3 mi"],
            "servicos_contratados": ["Internet", "Internet; Cloud", "VoIP"],
        })

        df, relatorio = limpeza.tratar(bruto)

        self.assertEqual(len(df), 1)
        self.assertEqual(df.loc[0, "cliente_cti"], "CTI001")
        self.assertEqual(df.loc[0, "segmento"], "Comércio")
        self.assertEqual(relatorio["codigos_duplicados_removidos"], 1)
        self.assertEqual(relatorio["linhas_sem_codigo_removidas"], 1)


if __name__ == "__main__":
    unittest.main()
