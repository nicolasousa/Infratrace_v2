"""
Testes da API Python (relatório de garantias). Usa o unittest, que já vem com o Python.

Como rodar (na pasta python/, com o .venv ativado):
    python -m unittest -v

Não é preciso iniciar a API antes: o Flask tem um "cliente de teste" que chama as rotas
direto, sem abrir a porta 5000. Os testes da classe TesteRotas leem o banco (o mesmo do .env),
mas não gravam nada.
"""

import unittest
from datetime import date

from app import app, local, situacao


class TesteFuncoes(unittest.TestCase):
    """Funções que não usam o banco."""

    def test_situacao(self):
        self.assertEqual(situacao(-1), "Vencida")
        self.assertEqual(situacao(0), "Vence hoje")
        self.assertEqual(situacao(30), "A vencer")

    def test_local_no_estoque(self):
        linha = {"rac_nome": None}
        self.assertEqual(local(linha), "Estoque")

    def test_local_no_rack(self):
        linha = {"rac_nome": "R01", "fil_nome": "A", "sal_nome": "Sala 1",
                 "atv_slot_inicio": 1, "atv_slot_fim": 2}
        self.assertEqual(local(linha), "Sala 1 / Fileira A / R01, slots 1 a 2")

    def test_local_um_slot(self):
        linha = {"rac_nome": "R02", "fil_nome": "A", "sal_nome": "Sala 1",
                 "atv_slot_inicio": 10, "atv_slot_fim": 10}
        self.assertEqual(local(linha), "Sala 1 / Fileira A / R02, slot 10")


class TesteRotas(unittest.TestCase):
    """Chama as rotas como o Java chamaria. Precisa do MySQL ligado e do .env preenchido."""

    def setUp(self):
        self.cliente = app.test_client()

    def test_status(self):
        resposta = self.cliente.get("/status")
        self.assertEqual(resposta.status_code, 200)
        self.assertEqual(resposta.get_json(), {"status": "ok"})

    def test_dias_invalido(self):
        for valor in ["abc", "-5", "9999"]:
            resposta = self.cliente.get("/relatorios/garantias?dias=" + valor)
            self.assertEqual(resposta.status_code, 400, valor)
            self.assertIn("erro", resposta.get_json())

    def test_relatorio(self):
        resposta = self.cliente.get("/relatorios/garantias?dias=90")
        self.assertEqual(resposta.status_code, 200)
        dados = resposta.get_json()

        self.assertEqual(dados["limite_dias"], 90)
        self.assertEqual(dados["total"], len(dados["itens"]))
        self.assertEqual(dados["vencidas"] + dados["a_vencer"], dados["total"])

        dias = [item["dias_restantes"] for item in dados["itens"]]
        self.assertEqual(dias, sorted(dias), "os itens devem vir do mais urgente para o menos urgente")
        for item in dados["itens"]:
            self.assertLessEqual(item["dias_restantes"], 90)
            self.assertNotEqual(item["status"], "Descartado")
            # a garantia vem no formato dd/mm/aaaa
            dia, mes, ano = item["garantia"].split("/")
            date(int(ano), int(mes), int(dia))

    def test_padrao_90_dias(self):
        resposta = self.cliente.get("/relatorios/garantias")
        self.assertEqual(resposta.get_json()["limite_dias"], 90)


if __name__ == "__main__":
    unittest.main()
