package Dao;

import Model.Sala;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso à tabela `sala` com JDBC puro. Aqui fica SÓ o SQL: nada de regra de negócio
 * (validações ficam no service) e nada de tela.
 *
 * Todos os métodos RECEBEM a Connection em vez de abrir a sua. Motivo: quem controla a
 * transação é o service. Se o service chamar dois DAOs com a mesma Connection, os dois
 * comandos ficam na mesma transação e podem ser confirmados (commit) ou desfeitos
 * (rollback) juntos.
 *
 * Sempre PreparedStatement com "?": o driver envia o valor separado do SQL, o que
 * impede SQL Injection (alguém digitar ' OR 1=1 -- no campo nome, por exemplo).
 */
public class SalaDAO {

    /** Todas as salas, em ordem alfabética. */
    public List<Sala> listar(Connection con) throws SQLException {
        String sql = "SELECT sal_id, sal_nome, sal_descricao FROM sala ORDER BY sal_nome";

        List<Sala> salas = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {            // cada rs.next() avança para a próxima linha
                salas.add(montar(rs));
            }
        }
        return salas;
    }

    /** A sala com esse id, ou null se não existir. */
    public Sala buscarPorId(Connection con, int id) throws SQLException {
        String sql = "SELECT sal_id, sal_nome, sal_descricao FROM sala WHERE sal_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);              // 1 = primeiro "?" do SQL
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        }
    }

    /**
     * Grava uma sala nova e devolve o id gerado pelo AUTO_INCREMENT.
     * Se o nome já existir, o MySQL recusa por causa do UNIQUE (uq_sal_nome) e
     * lança SQLException com código 1062.
     */
    public int inserir(Connection con, Sala sala) throws SQLException {
        String sql = "INSERT INTO sala (sal_nome, sal_descricao) VALUES (?, ?)";

        // RETURN_GENERATED_KEYS pede ao driver para devolver o id criado pelo banco.
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, sala.getNome());
            ps.setString(2, sala.getDescricao());   // setString com null grava NULL
            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                return chaves.getInt(1);
            }
        }
    }

    /**
     * Altera nome e descrição da sala. Devolve true se a sala existia.
     * executeUpdate() devolve quantas linhas foram afetadas: 0 = nenhuma sala com esse id.
     */
    public boolean atualizar(Connection con, Sala sala) throws SQLException {
        String sql = "UPDATE sala SET sal_nome = ?, sal_descricao = ? WHERE sal_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sala.getNome());
            ps.setString(2, sala.getDescricao());
            ps.setInt(3, sala.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Exclui a sala. Devolve true se a sala existia.
     * Se a sala tiver fileiras, o MySQL recusa por causa da FK fk_fil_sala e lança
     * SQLException com código 1451.
     */
    public boolean excluir(Connection con, int id) throws SQLException {
        String sql = "DELETE FROM sala WHERE sal_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /** Converte a linha atual do ResultSet em um objeto Sala. */
    private Sala montar(ResultSet rs) throws SQLException {
        return new Sala(
                rs.getInt("sal_id"),
                rs.getString("sal_nome"),
                rs.getString("sal_descricao"));
    }
}
