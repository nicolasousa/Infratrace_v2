package Dao;

import Model.Rack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RackDAO {

    public List<Rack> listar(Connection con) throws SQLException {
        String sql = """
                SELECT v.rac_id, v.rac_nome, v.rac_total_slots, v.fil_id,
                       v.fil_nome, v.sal_nome, v.qtd_ativos,
                       (SELECT MAX(a.atv_slot_fim) FROM ativo a WHERE a.fk_rac_id = v.rac_id) AS maior_slot
                  FROM View_ocupacao_rack v
                 ORDER BY v.sal_nome, v.fil_nome, v.rac_nome
                """;

        List<Rack> racks = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Rack rack = new Rack(rs.getInt("rac_id"), rs.getString("rac_nome"),
                        rs.getInt("rac_total_slots"), rs.getInt("fil_id"));
                rack.setFileiraNome(rs.getString("fil_nome"));
                rack.setSalaNome(rs.getString("sal_nome"));
                rack.setQuantidadeAtivos(rs.getInt("qtd_ativos"));
                rack.setMaiorSlotOcupado(rs.getObject("maior_slot", Integer.class));
                racks.add(rack);
            }
        }
        return racks;
    }

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

    public Vinculos contarVinculos(Connection con, int id) throws SQLException {
        String sql = """
                SELECT (SELECT COUNT(*) FROM ativo   WHERE fk_rac_id = ?) AS ativos,
                       (SELECT COUNT(*) FROM aluguel WHERE fk_rac_id = ?) AS alugueis,
                       (SELECT COUNT(*) FROM movimentacao
                         WHERE fk_rac_origem_id = ? OR fk_rac_destino_id = ?) AS movimentacoes
                """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int posicao = 1; posicao <= 4; posicao++) {
                ps.setInt(posicao, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new Vinculos(rs.getInt("ativos"), rs.getInt("alugueis"), rs.getInt("movimentacoes"));
            }
        }
    }

    public record Vinculos(int ativos, int alugueis, int movimentacoes) {
    }

    public boolean excluir(Connection con, int id) throws SQLException {
        String sql = "DELETE FROM rack WHERE rac_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
