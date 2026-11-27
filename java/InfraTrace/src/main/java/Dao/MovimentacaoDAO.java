package Dao;

import Model.Movimentacao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MovimentacaoDAO {

    public int inserir(Connection con, Movimentacao mov) throws SQLException {
        String sql = """
                INSERT INTO movimentacao (mov_motivo,
                                          mov_slot_inicio_origem, mov_slot_fim_origem, fk_rac_origem_id,
                                          mov_slot_inicio_destino, mov_slot_fim_destino, fk_rac_destino_id,
                                          fk_atv_id, fk_usr_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, mov.getMotivo());
            ps.setObject(2, mov.getSlotInicioOrigem());
            ps.setObject(3, mov.getSlotFimOrigem());
            ps.setObject(4, mov.getRackOrigemId());
            ps.setObject(5, mov.getSlotInicioDestino());
            ps.setObject(6, mov.getSlotFimDestino());
            ps.setObject(7, mov.getRackDestinoId());
            ps.setInt(8, mov.getAtivoId());
            ps.setInt(9, mov.getUsuarioId());
            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                return chaves.getInt(1);
            }
        }
    }
}
