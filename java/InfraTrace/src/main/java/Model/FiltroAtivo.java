package Model;

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

    public boolean isSomenteEstoque() {
        return somenteEstoque;
    }

    public void setSomenteEstoque(boolean somenteEstoque) {
        this.somenteEstoque = somenteEstoque;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }
}
