package Dao;

import Model.Fileira;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FileiraDAO {

    public List<Fileira> listar(Connection con) throws SQLException {
        String sql = """
                SELECT f.fil_id, f.fil_nome, f.fk_sal_id, s.sal_nome,
                       COUNT(r.rac_id) AS qtd_racks
                  FROM fileira f
                  JOIN sala s      ON s.sal_id = f.fk_sal_id
                  LEFT JOIN rack r ON r.fk_fil_id = f.fil_id
                 GROUP BY f.fil_id, f.fil_nome, f.fk_sal_id, s.sal_nome
                 ORDER BY s.sal_nome, f.fil_nome
                """;

        List<Fileira> fileiras = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Fileira fileira = new Fileira(rs.getInt("fil_id"), rs.getString("fil_nome"), rs.getInt("fk_sal_id"));
                fileira.setSalaNome(rs.getString("sal_nome"));
                fileira.setQuantidadeRacks(rs.getInt("qtd_racks"));
                fileiras.add(fileira);
            }
        }
        return fileiras;
    }

    public int inserir(Connection con, Fileira fileira) throws SQLException {
        String sql = "INSERT INTO fileira (fil_nome, fk_sal_id) VALUES (?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fileira.getNome());
            ps.setInt(2, fileira.getSalaId());
            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                return chaves.getInt(1);
            }
        }
    }

    public boolean atualizar(Connection con, Fileira fileira) throws SQLException {
        String sql = "UPDATE fileira SET fil_nome = ?, fk_sal_id = ? WHERE fil_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fileira.getNome());
            ps.setInt(2, fileira.getSalaId());
            ps.setInt(3, fileira.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean excluir(Connection con, int id) throws SQLException {
        String sql = "DELETE FROM fileira WHERE fil_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
