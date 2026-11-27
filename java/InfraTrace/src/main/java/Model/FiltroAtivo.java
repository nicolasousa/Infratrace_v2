package Model;

/**
 * O que o usuário escolheu na barra de filtros da tela de ativos.
 * Cada campo vazio (null ou false) quer dizer "não filtrar por isso".
 *
 * Não é uma tabela do banco: é só um "pacote" para levar os filtros da tela até o DAO
 * sem precisar de um método com cinco parâmetros.
 */
public class FiltroAtivo {

    private StatusAtivo status;
    private Integer tipoId;
    private Integer rackId;
    private boolean somenteEstoque;
    private String texto;

    public StatusAtivo getStatus() {
        return status;
    }

    public void setStatus(StatusAtivo status) {
        this.status = status;
    }

    public Integer getTipoId() {
        return tipoId;
    }

    public void setTipoId(Integer tipoId) {
        this.tipoId = tipoId;
    }

    public Integer getRackId() {
        return rackId;
    }

    public void setRackId(Integer rackId) {
        this.rackId = rackId;
    }

    /** true = só ativos fora de rack que ainda não foram descartados. */
    public boolean isSomenteEstoque() {
        return somenteEstoque;
    }

    public void setSomenteEstoque(boolean somenteEstoque) {
        this.somenteEstoque = somenteEstoque;
    }

    /** Trecho do nome, do número de série ou do patrimônio. */
    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }
}
