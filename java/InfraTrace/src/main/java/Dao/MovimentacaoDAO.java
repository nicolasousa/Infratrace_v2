package Dao;

import Model.Movimentacao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public class MovimentacaoDAO {

    public int movimentar(Connection con, Movimentacao mov) throws SQLException {
        try (CallableStatement cs = con.prepareCall("{CALL Procedure_movimentar_ativo(?, ?, ?, ?, ?, ?, ?)}")) {
            cs.setInt(1, mov.getAtivoId());
            cs.setObject(2, mov.getRackDestinoId(), Types.INTEGER);
            cs.setObject(3, mov.getSlotInicioDestino(), Types.INTEGER);
            cs.setObject(4, mov.getSlotFimDestino(), Types.INTEGER);
            cs.setString(5, mov.getMotivo());
            cs.setInt(6, mov.getUsuarioId());
            cs.registerOutParameter(7, Types.INTEGER);
            cs.execute();
            return cs.getInt(7);
        }
    }
}
