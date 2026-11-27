package Dao;

import Model.Sala;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SalaDAO {

    public List<Sala> listar(Connection con) throws SQLException {
        String sql = "SELECT sal_id, sal_nome, sal_descricao FROM sala ORDER BY sal_nome";

        List<Sala> salas = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                salas.add(montar(rs));
            }
        }
        return salas;
    }

    public Sala buscarPorId(Connection con, int id) throws SQLException {
        String sql = "SELECT sal_id, sal_nome, sal_descricao FROM sala WHERE sal_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        }
    }

    public int inserir(Connection con, Sala sala) throws SQLException {
        String sql = "INSERT INTO sala (sal_nome, sal_descricao) VALUES (?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, sala.getNome());
            ps.setString(2, sala.getDescricao());
            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                return chaves.getInt(1);
            }
        }
    }

    public boolean atualizar(Connection con, Sala sala) throws SQLException {
        String sql = "UPDATE sala SET sal_nome = ?, sal_descricao = ? WHERE sal_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sala.getNome());
            ps.setString(2, sala.getDescricao());
            ps.setInt(3, sala.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean excluir(Connection con, int id) throws SQLException {
        String sql = "DELETE FROM sala WHERE sal_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Sala montar(ResultSet rs) throws SQLException {
        return new Sala(
                rs.getInt("sal_id"),
                rs.getString("sal_nome"),
                rs.getString("sal_descricao"));
    }
}
