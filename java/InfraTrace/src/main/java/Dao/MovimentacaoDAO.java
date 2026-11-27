package Dao;

import Model.Movimentacao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Acesso à tabela `movimentacao` (histórico de posições dos ativos).
 * Nesta etapa só grava; a consulta do histórico vem no módulo Ocupação e Movimentação.
 */
public class MovimentacaoDAO {

    /**
     * Grava uma movimentação e devolve o id gerado. mov_data fica com o valor padrão da
     * coluna (CURRENT_TIMESTAMP): a data e a hora em que a movimentação foi registrada.
     *
     * Este método NÃO faz commit: ele usa a transação de quem chamou. Se o passo seguinte
     * falhar e quem chamou fizer rollback, esta linha também é desfeita.
     */
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
            // setObject aceita null (grava NULL); setInt não aceitaria um Integer nulo
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
