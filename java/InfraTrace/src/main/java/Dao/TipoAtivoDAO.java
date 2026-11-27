package Dao;

import Model.TipoAtivo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TipoAtivoDAO {

    public List<TipoAtivo> listar(Connection con) throws SQLException {
        String sql = "SELECT tpa_id, tpa_nome, tpa_descricao FROM tipo_ativo ORDER BY tpa_nome";

        List<TipoAtivo> tipos = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tipos.add(new TipoAtivo(rs.getInt("tpa_id"), rs.getString("tpa_nome"), rs.getString("tpa_descricao")));
            }
        }
        return tipos;
    }

    public int inserir(Connection con, TipoAtivo tipo) throws SQLException {
        String sql = "INSERT INTO tipo_ativo (tpa_nome, tpa_descricao) VALUES (?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tipo.getNome());
            ps.setString(2, tipo.getDescricao());
            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                return chaves.getInt(1);
            }
        }
    }

    public boolean atualizar(Connection con, TipoAtivo tipo) throws SQLException {
        String sql = "UPDATE tipo_ativo SET tpa_nome = ?, tpa_descricao = ? WHERE tpa_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tipo.getNome());
            ps.setString(2, tipo.getDescricao());
            ps.setInt(3, tipo.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean excluir(Connection con, int id) throws SQLException {
        String sql = "DELETE FROM tipo_ativo WHERE tpa_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
