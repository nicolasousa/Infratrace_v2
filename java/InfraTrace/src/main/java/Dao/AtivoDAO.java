package Dao;

import Model.Ativo;
import Model.FiltroAtivo;
import Model.StatusAtivo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AtivoDAO {

    private static final String SELECT_ATIVO = """
            SELECT a.atv_id, a.atv_nome, a.atv_fabricante, a.atv_modelo, a.atv_num_serie,
                   a.atv_patrimonio, a.atv_status, a.atv_data_aquisicao, a.atv_valor_compra,
                   a.atv_garantia, a.atv_slot_inicio, a.atv_slot_fim, a.atv_data_descarte,
                   a.atv_motivo_descarte, a.fk_tpa_id, t.tpa_nome,
                   a.fk_rac_id, r.rac_nome, f.fil_nome, s.sal_nome
              FROM ativo a
              JOIN tipo_ativo t   ON t.tpa_id = a.fk_tpa_id
              LEFT JOIN rack r    ON r.rac_id = a.fk_rac_id
              LEFT JOIN fileira f ON f.fil_id = r.fk_fil_id
              LEFT JOIN sala s    ON s.sal_id = f.fk_sal_id
            """;

    public List<Ativo> listar(Connection con, FiltroAtivo filtro) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_ATIVO).append(" WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();

        if (filtro.getStatus() != null) {
            sql.append(" AND a.atv_status = ?");
            parametros.add(filtro.getStatus().name());
        }
        if (filtro.getTipoId() != null) {
            sql.append(" AND a.fk_tpa_id = ?");
            parametros.add(filtro.getTipoId());
        }
        if (filtro.isSomenteEstoque()) {
            sql.append(" AND a.fk_rac_id IS NULL AND a.atv_status <> 'DESCARTADO'");
        } else if (filtro.getRackId() != null) {
            sql.append(" AND a.fk_rac_id = ?");
            parametros.add(filtro.getRackId());
        }
        if (filtro.getTexto() != null && !filtro.getTexto().isBlank()) {
            sql.append(" AND (a.atv_nome LIKE ? OR a.atv_num_serie LIKE ? OR a.atv_patrimonio LIKE ?)");
            String trecho = "%" + escaparLike(filtro.getTexto().trim()) + "%";
            parametros.add(trecho);
            parametros.add(trecho);
            parametros.add(trecho);
        }
        sql.append(" ORDER BY a.atv_nome, a.atv_id");

        List<Ativo> ativos = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ativos.add(lerAtivo(rs));
                }
            }
        }
        return ativos;
    }

    public Ativo buscarPorId(Connection con, int id, boolean travar) throws SQLException {
        String sql = SELECT_ATIVO + " WHERE a.atv_id = ?" + (travar ? " FOR UPDATE OF a" : "");

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? lerAtivo(rs) : null;
            }
        }
    }

    public int inserir(Connection con, Ativo ativo) throws SQLException {
        String sql = """
                INSERT INTO ativo (atv_nome, atv_fabricante, atv_modelo, atv_num_serie, atv_patrimonio,
                                   atv_status, atv_data_aquisicao, atv_valor_compra, atv_garantia, fk_tpa_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencherDadosCadastrais(ps, ativo);
            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                return chaves.getInt(1);
            }
        }
    }

    public boolean atualizar(Connection con, Ativo ativo) throws SQLException {
        String sql = """
                UPDATE ativo
                   SET atv_nome = ?, atv_fabricante = ?, atv_modelo = ?, atv_num_serie = ?,
                       atv_patrimonio = ?, atv_status = ?, atv_data_aquisicao = ?,
                       atv_valor_compra = ?, atv_garantia = ?, fk_tpa_id = ?
                 WHERE atv_id = ? AND atv_status <> 'DESCARTADO'
                """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            preencherDadosCadastrais(ps, ativo);
            ps.setInt(11, ativo.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean descartar(Connection con, int id, LocalDate data, String motivo) throws SQLException {
        String sql = """
                UPDATE ativo
                   SET atv_status = 'DESCARTADO', atv_data_descarte = ?, atv_motivo_descarte = ?,
                       fk_rac_id = NULL, atv_slot_inicio = NULL, atv_slot_fim = NULL
                 WHERE atv_id = ?
                """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, data);
            ps.setString(2, motivo);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }

    private static void preencherDadosCadastrais(PreparedStatement ps, Ativo ativo) throws SQLException {
        ps.setString(1, ativo.getNome());
        ps.setString(2, ativo.getFabricante());
        ps.setString(3, ativo.getModelo());
        ps.setString(4, ativo.getNumSerie());
        ps.setString(5, ativo.getPatrimonio());
        ps.setString(6, ativo.getStatus().name());
        ps.setObject(7, ativo.getDataAquisicao());
        ps.setBigDecimal(8, ativo.getValorCompra());
        ps.setObject(9, ativo.getGarantia());
        ps.setInt(10, ativo.getTipoId());
    }

    private static Ativo lerAtivo(ResultSet rs) throws SQLException {
        Ativo ativo = new Ativo();
        ativo.setId(rs.getInt("atv_id"));
        ativo.setNome(rs.getString("atv_nome"));
        ativo.setFabricante(rs.getString("atv_fabricante"));
        ativo.setModelo(rs.getString("atv_modelo"));
        ativo.setNumSerie(rs.getString("atv_num_serie"));
        ativo.setPatrimonio(rs.getString("atv_patrimonio"));
        ativo.setStatus(StatusAtivo.valueOf(rs.getString("atv_status")));
        ativo.setDataAquisicao(rs.getObject("atv_data_aquisicao", LocalDate.class));
        ativo.setValorCompra(rs.getObject("atv_valor_compra", BigDecimal.class));
        ativo.setGarantia(rs.getObject("atv_garantia", LocalDate.class));
        ativo.setSlotInicio(rs.getObject("atv_slot_inicio", Integer.class));
        ativo.setSlotFim(rs.getObject("atv_slot_fim", Integer.class));
        ativo.setDataDescarte(rs.getObject("atv_data_descarte", LocalDate.class));
        ativo.setMotivoDescarte(rs.getString("atv_motivo_descarte"));
        ativo.setTipoId(rs.getInt("fk_tpa_id"));
        ativo.setTipoNome(rs.getString("tpa_nome"));
        ativo.setRackId(rs.getObject("fk_rac_id", Integer.class));
        ativo.setRackNome(rs.getString("rac_nome"));
        ativo.setFileiraNome(rs.getString("fil_nome"));
        ativo.setSalaNome(rs.getString("sal_nome"));
        return ativo;
    }

    private static String escaparLike(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
