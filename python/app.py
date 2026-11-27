from datetime import datetime
from pathlib import Path

import mysql.connector
from flask import Flask, jsonify, request

DIAS_PADRAO = 90
DIAS_MAXIMO = 3650

NOMES_STATUS = {
    "ATIVO": "Ativo",
    "INATIVO": "Inativo",
    "EM_MANUTENCAO": "Em manutenção",
    "AGUARDANDO_DESCARTE": "Aguardando descarte",
    "DESCARTADO": "Descartado",
}

app = Flask(__name__)
app.json.ensure_ascii = False

def ler_env():
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

def buscar_garantias(dias):
    sql = """
        SELECT atv_id, atv_nome, atv_patrimonio, atv_status, atv_garantia, dias_restantes,
               tpa_nome, rac_nome, fil_nome, sal_nome, atv_slot_inicio, atv_slot_fim
          FROM View_garantia_ativo
         WHERE dias_restantes <= %s
         ORDER BY dias_restantes, atv_nome
    """
    con = conectar()
    try:
        cursor = con.cursor(dictionary=True)
        cursor.execute(sql, (dias,))
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
    app.run(host="127.0.0.1", port=5000)
