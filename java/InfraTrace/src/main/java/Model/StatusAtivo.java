package Model;

import java.util.List;

/**
 * Situação de um ativo. Os valores são os mesmos do CHECK ck_atv_status da tabela `ativo`:
 *   CHECK (atv_status IN ('ATIVO', 'INATIVO', 'EM_MANUTENCAO', 'AGUARDANDO_DESCARTE', 'DESCARTADO'))
 *
 * Por que um enum e não uma String? Com String, um erro de digitação ("ATIVA") só seria
 * descoberto pelo banco. Com enum, o compilador só aceita os cinco valores existentes.
 *
 * No banco fica o nome da constante (name(), ex.: "EM_MANUTENCAO").
 * Na tela aparece o rótulo (toString(), ex.: "Em manutenção").
 */
public enum StatusAtivo {

    ATIVO("Ativo"),
    INATIVO("Inativo"),
    EM_MANUTENCAO("Em manutenção"),
    AGUARDANDO_DESCARTE("Aguardando descarte"),
    DESCARTADO("Descartado");

    private final String rotulo;

    StatusAtivo(String rotulo) {
        this.rotulo = rotulo;
    }

    /**
     * Os status que o usuário pode escolher no cadastro. DESCARTADO fica de fora: ele só é
     * gravado pelo descarte, que também pede data e motivo e tira o ativo do rack.
     */
    public static List<StatusAtivo> escolhiveisNoCadastro() {
        return List.of(ATIVO, INATIVO, EM_MANUTENCAO, AGUARDANDO_DESCARTE);
    }

    @Override
    public String toString() {
        return rotulo;
    }
}
