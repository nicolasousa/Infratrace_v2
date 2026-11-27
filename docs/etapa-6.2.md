# Etapa 6.2 — Projeto Java no NetBeans e primeira conexão JDBC

Nesta etapa o Java passa a conversar com o MySQL. Ainda não há telas: o teste roda no console.

## 1. Preparar

1. **`.env`**: na raiz do projeto (`infratrace_v2/`), copie `.env.example` para `.env` e preencha
   `INFRATRACE_DB_PASSWORD` com a senha do **seu** MySQL. O `.env` não vai para o Git.
2. **Banco**: rode `database/infratrace_v2.sql` e `database/dados_exemplo.sql` no Workbench
   (se já rodaram na etapa 6.1, não precisa de novo).

## 2. Abrir e executar no NetBeans

1. *File → Open Project* → selecione a pasta `java/InfraTrace` (o ícone é de projeto Maven).
2. Na primeira abertura o NetBeans baixa o driver do MySQL (aparece "Downloading..." na barra).
3. Abram `Source Packages → br.com.infratrace`, cliquem com o botão direito em `TesteConexao.java`
   → **Run File** (Shift+F6).
   > Nesta etapa o `TesteConexao` era o `exec.mainClass` e rodava com F6. Desde a etapa 6.3, o F6
   > abre a tela principal (`App`), e o teste roda pelo *Run File*. Guia completo do NetBeans:
   > [guia-netbeans.md](guia-netbeans.md).

Saída esperada na aba *Output*:

```
1. Conectado ao MySQL 8.0.xx, banco 'infratrace_v2'
2. Salas cadastradas: 2
     1 - Sala 1 - Principal (Sala principal do data center)
     2 - Sala 2 - Expansão (Sala nova, ainda com pouco uso)
3. INSERT funcionou: a sala de teste recebeu o id 3 e o banco agora tem 3 salas (dentro da transação)
   Depois do rollback: 2 salas (nada foi gravado)
4. Erro esperado do MySQL, código 1062: Duplicate entry 'Sala 1 - Principal' for key 'sala.uq_sal_nome'
Tudo certo: Java + JDBC + MySQL funcionando.
```

Rodando de novo, o id do passo 3 aumenta (4, 5, 6...) mesmo sem nada ser gravado: o
`AUTO_INCREMENT` não volta atrás no rollback, e o INSERT recusado do passo 4 também gasta um número.

## 3. Erros comuns

| Mensagem | Causa | Solução |
|---|---|---|
| `Configuração 'INFRATRACE_DB_HOST' não encontrada` | Não existe `.env` na raiz | Copiar `.env.example` para `.env` |
| `Access denied for user 'root'@'localhost'` (código 1045) | Senha errada no `.env` | Corrigir `INFRATRACE_DB_PASSWORD` |
| `Unknown database 'infratrace_v2'` (código 1049) | Script do banco não foi rodado | Rodar `database/infratrace_v2.sql` |
| `Communications link failure` | MySQL desligado ou porta errada | Ligar o serviço MySQL80 / conferir `INFRATRACE_DB_PORT` |
| Acentos aparecem como `Expans�o` no console | Codificação da janela de saída, não do banco | Não afeta o banco nem as telas Swing |

## 4. Quem faz o quê (o caminho de um SELECT)

```
TesteConexao ──► Conexao.getConexao() ──► Configuracao.obter("INFRATRACE_DB_...")  (.env)
     │                    └──► DriverManager.getConnection(url, usuario, senha) ──► MySQL
     └──► SalaDAO.listar(con) ──► PreparedStatement ──► ResultSet ──► List<Sala>
```

| Classe | Responsabilidade |
|---|---|
| `config/Configuracao` | Lê as configurações: variável de ambiente primeiro, depois o `.env` |
| `config/Conexao` | Monta a URL JDBC e abre a conexão |
| `model/Sala` | Guarda os dados de uma linha da tabela `sala` |
| `dao/SalaDAO` | SQL da tabela `sala` (recebe a `Connection` de quem chama) |
| `TesteConexao` | Teste temporário desta etapa |

## 5. Exercício do grupo: completar o `SalaDAO`

> **Atualização (etapa 6.3):** a tela de salas precisou desses métodos, então eles já estão
> no `SalaDAO`. Usem o enunciado abaixo para conferir se entenderam a solução.

Escrevam os dois métodos que faltam, no mesmo estilo de `inserir`:

```java
/** Altera nome e descrição. Devolve true se a sala existia. */
public boolean atualizar(Connection con, Sala sala) throws SQLException

/** Exclui a sala. Devolve true se a sala existia. */
public boolean excluir(Connection con, int id) throws SQLException
```

Dicas:
- SQL: `UPDATE sala SET sal_nome = ?, sal_descricao = ? WHERE sal_id = ?` e
  `DELETE FROM sala WHERE sal_id = ?`.
- `ps.executeUpdate()` devolve **quantas linhas foram afetadas**: 0 quer dizer que o id não existe.
- Testem no `TesteConexao`, dentro da transação com `rollback()`, para não mudar o banco.
- Tentem excluir a sala 1: o MySQL recusa com o código **1451**, porque ela tem fileiras
  (FK `fk_fil_sala`). Na etapa do service, esse código vira a mensagem
  "Esta sala tem fileiras cadastradas e não pode ser excluída".
