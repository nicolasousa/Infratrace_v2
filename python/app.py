"""
InfraTrace v2 - API Python (Flask) do relatório de garantias dos ativos.

O Java (tela de Ativos, botão "Relatório de garantias") chama esta API por HTTP e mostra o
resultado numa tabela. O Python só LÊ o banco: quem grava os ativos continua sendo o Java.

Como rodar (na pasta python/):
    pip install -r requirements.txt
    python app.py

Rotas:
    GET /status                          -> {"status": "ok"}  (para testar se a API está no ar)
    GET /relatorios/garantias?dias=90    -> garantias vencidas e as que vencem nos próximos N dias
"""

from datetime import datetime
from pathlib import Path

import mysql.connector
from flask import Flask, jsonify, request

# Limites do parâmetro "dias" (10 anos é mais do que suficiente para uma garantia)
DIAS_PADRAO = 90
DIAS_MAXIMO = 3650

# Os mesmos nomes que o enum StatusAtivo do Java mostra na tela
NOMES_STATUS = {
    "ATIVO": "Ativo",
    "INATIVO": "Inativo",
    "EM_MANUTENCAO": "Em manutenção",
    "AGUARDANDO_DESCARTE": "Aguardando descarte",
    "DESCARTADO": "Descartado",
}

app = Flask(__name__)
app.json.ensure_ascii = False   # devolve "manutenção" em vez de "manutenção"


# ----------------------------------------------------------------------
# Configuração (.env) e conexão
# ----------------------------------------------------------------------

def ler_env():
    """Lê o .env da raiz do projeto (o mesmo arquivo que o Java usa). Formato: CHAVE=VALOR."""
    pasta = Path(__file__).resolve().parent
    for candidata in [pasta, *pasta.parents]:
        arquivo = candidata / ".env"
        if arquivo.exists():
            valores = {}
            for linha in arquivo.read_text(encoding="utf-8").splitlines():
                linha = linha.strip()
                if linha and not linha.startswith("#") and "=" in linha:
                    chave, valor = linha.split("=", 1)
                    valores[chave.strip()] = valor.strip()
            return valores
    raise RuntimeError("Arquivo .env não encontrado. Copie o .env.example para .env na raiz do projeto.")


CONFIG = ler_env()


def conectar():
    return mysql.connector.connect(
        host=CONFIG["INFRATRACE_DB_HOST"],
        port=int(CONFIG["INFRATRACE_DB_PORT"]),
        database=CONFIG["INFRATRACE_DB_NAME"],
        user=CONFIG["INFRATRACE_DB_USER"],
        password=CONFIG["INFRATRACE_DB_PASSWORD"],
    )


# ----------------------------------------------------------------------
# Relatório
# ----------------------------------------------------------------------

def buscar_garantias(dias):
    """
    Lê a View_garantia_ativo (criada pelo script do banco). A view já deixa de fora os ativos
    descartados e os sem garantia, e calcula dias_restantes (negativo = vencida).
    """
    sql = """
        SELECT atv_id, atv_nome, atv_patrimonio, atv_status, atv_garantia, dias_restantes,
               tpa_nome, rac_nome, fil_nome, sal_nome, atv_slot_inicio, atv_slot_fim
          FROM View_garantia_ativo
         WHERE dias_restantes <= %s
         ORDER BY dias_restantes, atv_nome
    """
    con = conectar()
    try:
        cursor = con.cursor(dictionary=True)   # cada linha vira um dicionário {coluna: valor}
        cursor.execute(sql, (dias,))           # %s com parâmetro: sem risco de SQL Injection
        return cursor.fetchall()
    finally:
        con.close()


def situacao(dias_restantes):
    if dias_restantes < 0:
        return "Vencida"
    if dias_restantes == 0:
        return "Vence hoje"
    return "A vencer"


def local(linha):
    """Ex.: "Sala 1 - Principal / Fileira A / R01, slots 1 a 2" ou "Estoque"."""
    if linha["rac_nome"] is None:
        return "Estoque"
    inicio, fim = linha["atv_slot_inicio"], linha["atv_slot_fim"]
    slots = f"slot {inicio}" if inicio == fim else f"slots {inicio} a {fim}"
    return f"{linha['sal_nome']} / Fileira {linha['fil_nome']} / {linha['rac_nome']}, {slots}"


def montar_item(linha):
    return {
        "id": linha["atv_id"],
        "nome": linha["atv_nome"],
        "tipo": linha["tpa_nome"],
        "patrimonio": linha["atv_patrimonio"] or "",
        "status": NOMES_STATUS.get(linha["atv_status"], linha["atv_status"]),
        "garantia": linha["atv_garantia"].strftime("%d/%m/%Y"),
        "dias_restantes": linha["dias_restantes"],
        "situacao": situacao(linha["dias_restantes"]),
        "local": local(linha),
    }


@app.get("/status")
def status():
    return jsonify({"status": "ok"})


@app.get("/relatorios/garantias")
def relatorio_garantias():
    texto = request.args.get("dias", str(DIAS_PADRAO))
    if not texto.isdigit() or int(texto) > DIAS_MAXIMO:
        return jsonify({"erro": f"O parâmetro 'dias' deve ser um número inteiro de 0 a {DIAS_MAXIMO}."}), 400
    dias = int(texto)

    try:
        itens = [montar_item(linha) for linha in buscar_garantias(dias)]
    except mysql.connector.Error as e:
        return jsonify({"erro": f"Erro no banco de dados: {e.msg}"}), 500

    vencidas = sum(1 for item in itens if item["situacao"] == "Vencida")
    return jsonify({
        "gerado_em": datetime.now().strftime("%d/%m/%Y %H:%M"),
        "limite_dias": dias,
        "total": len(itens),
        "vencidas": vencidas,
        "a_vencer": len(itens) - vencidas,
        "itens": itens,
    })


if __name__ == "__main__":
    # 127.0.0.1 = a API só aceita conexões deste computador
    app.run(host="127.0.0.1", port=5000)
