package Model;

import java.util.List;

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

    public static List<StatusAtivo> escolhiveisNoCadastro() {
        return List.of(ATIVO, INATIVO, EM_MANUTENCAO, AGUARDANDO_DESCARTE);
    }

    @Override
    public String toString() {
        return rotulo;
    }
}
