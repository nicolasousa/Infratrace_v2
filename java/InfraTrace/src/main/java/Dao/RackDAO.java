package Dao;

import Model.Rack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso à tabela `rack` com JDBC puro. Mesmo padrão do FileiraDAO:
 * só SQL, recebe a Connection de quem chama, sempre PreparedStatement com "?".
 */
public class RackDAO {

    /**
     * Todos os racks, com sala, fileira, quantidade de ativos e o slot mais alto ocupado.
     *
     *   JOIN fileira e JOIN sala -> todo rack tem fileira, e toda fileira tem sala: JOIN normal;
     *   LEFT JOIN ativo          -> LEFT porque um rack SEM ativos também precisa aparecer;
     *   COUNT(a.atv_id)          -> conta só as linhas que encontraram ativo (rack vazio = 0);
     *   MAX(a.atv_slot_fim)      -> o slot mais alto ocupado (rack vazio = NULL).
     *
     * Ativos descartados ou no estoque têm fk_rac_id NULL, então não entram na conta.
     */
    public List<Rack> listar(Connection con) throws SQLException {
        String sql = """
                SELECT r.rac_id, r.rac_nome, r.rac_total_slots, r.fk_fil_id,
                       f.fil_nome, s.sal_nome,
                       COUNT(a.atv_id)     AS qtd_ativos,
                       MAX(a.atv_slot_fim) AS maior_slot
                  FROM rack r
                  JOIN fileira f    ON f.fil_id = r.fk_fil_id
                  JOIN sala s       ON s.sal_id = f.fk_sal_id
                  LEFT JOIN ativo a ON a.fk_rac_id = r.rac_id
                 GROUP BY r.rac_id, r.rac_nome, r.rac_total_slots, r.fk_fil_id, f.fil_nome, s.sal_nome
                 ORDER BY s.sal_nome, f.fil_nome, r.rac_nome
                """;

        List<Rack> racks = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Rack rack = new Rack(rs.getInt("rac_id"), rs.getString("rac_nome"),
                        rs.getInt("rac_total_slots"), rs.getInt("fk_fil_id"));
                rack.setFileiraNome(rs.getString("fil_nome"));
                rack.setSalaNome(rs.getString("sal_nome"));
                rack.setQuantidadeAtivos(rs.getInt("qtd_ativos"));
                // getInt devolveria 0 para NULL; getObject com Integer.class devolve null mesmo
                rack.setMaiorSlotOcupado(rs.getObject("maior_slot", Integer.class));
                racks.add(rack);
            }
        }
        return racks;
    }

    /**
     * Grava um rack novo e devolve o id gerado.
     * Erros possíveis do MySQL: 1062 (já existe rack com esse nome na fileira, UNIQUE
     * uq_rac_fileira_nome), 1452 (a fileira não existe, FK fk_rac_fileira) e
     * 3819 (capacidade fora de 1 a 60, CHECK ck_rac_total_slots).
     */
    public int inserir(Connection con, Rack rack) throws SQLException {
        String sql = "INSERT INTO rack (rac_nome, rac_total_slots, fk_fil_id) VALUES (?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, rack.getNome());
            ps.setInt(2, rack.getTotalSlots());
            ps.setInt(3, rack.getFileiraId());
            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                return chaves.getInt(1);
            }
        }
    }

    /**
     * Altera nome, capacidade e fileira do rack. Devolve true se o rack existia.
     * Além dos erros do inserir, aqui pode vir o 1644: o Trigger_rack_reducao_capacidade
     * recusa uma capacidade menor que o slot mais alto ocupado.
     */
    public boolean atualizar(Connection con, Rack rack) throws SQLException {
        String sql = "UPDATE rack SET rac_nome = ?, rac_total_slots = ?, fk_fil_id = ? WHERE rac_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, rack.getNome());
            ps.setInt(2, rack.getTotalSlots());
            ps.setInt(3, rack.getFileiraId());
            ps.setInt(4, rack.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Quantos registros de cada tabela apontam para o rack. Usado para explicar ao usuário
     * por que o rack não pode ser excluído (o erro 1451 do MySQL cita só a primeira FK que
     * bloqueou, e não todas).
     *
     * Três subconsultas com COUNT num único SELECT: uma ida ao banco devolve os três números.
     */
    public Vinculos contarVinculos(Connection con, int id) throws SQLException {
        String sql = """
                SELECT (SELECT COUNT(*) FROM ativo   WHERE fk_rac_id = ?) AS ativos,
                       (SELECT COUNT(*) FROM aluguel WHERE fk_rac_id = ?) AS alugueis,
                       (SELECT COUNT(*) FROM movimentacao
                         WHERE fk_rac_origem_id = ? OR fk_rac_destino_id = ?) AS movimentacoes
                """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int posicao = 1; posicao <= 4; posicao++) {
                ps.setInt(posicao, id);   // o mesmo id nos quatro "?"
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new Vinculos(rs.getInt("ativos"), rs.getInt("alugueis"), rs.getInt("movimentacoes"));
            }
        }
    }

    /**
     * O resultado do contarVinculos. Um "record" é uma classe só de dados: o Java cria sozinho
     * o construtor e os métodos ativos(), alugueis() e movimentacoes().
     */
    public record Vinculos(int ativos, int alugueis, int movimentacoes) {
    }

    /**
     * Exclui o rack. Devolve true se o rack existia.
     * Se algum ativo, aluguel ou movimentação apontar para ele, o MySQL recusa com o
     * código 1451 (FKs fk_atv_rack, fk_alu_rack, fk_mov_rack_origem e fk_mov_rack_destino).
     */
    public boolean excluir(Connection con, int id) throws SQLException {
        String sql = "DELETE FROM rack WHERE rac_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
