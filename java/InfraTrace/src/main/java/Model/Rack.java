package Model;

public class Rack {

    private Integer id;
    private String nome;
    private Integer totalSlots;
    private Integer fileiraId;

    private String salaNome;
    private String fileiraNome;
    private int quantidadeAtivos;
    private Integer maiorSlotOcupado;

    public Rack() {
    }

    public Rack(Integer id, String nome, Integer totalSlots, Integer fileiraId) {
        this.id = id;
        this.nome = nome;
        this.totalSlots = totalSlots;
        this.fileiraId = fileiraId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getTotalSlots() {
        return totalSlots;
    }

    public void setTotalSlots(Integer totalSlots) {
        this.totalSlots = totalSlots;
    }

    public Integer getFileiraId() {
        return fileiraId;
    }

    public void setFileiraId(Integer fileiraId) {
        this.fileiraId = fileiraId;
    }

    public String getSalaNome() {
        return salaNome;
    }

    public void setSalaNome(String salaNome) {
        this.salaNome = salaNome;
    }

    public String getFileiraNome() {
        return fileiraNome;
    }

    public void setFileiraNome(String fileiraNome) {
        this.fileiraNome = fileiraNome;
    }

    public int getQuantidadeAtivos() {
        return quantidadeAtivos;
    }

    public void setQuantidadeAtivos(int quantidadeAtivos) {
        this.quantidadeAtivos = quantidadeAtivos;
    }

    public Integer getMaiorSlotOcupado() {
        return maiorSlotOcupado;
    }

    public void setMaiorSlotOcupado(Integer maiorSlotOcupado) {
        this.maiorSlotOcupado = maiorSlotOcupado;
    }

    @Override
    public String toString() {
        return nome + " (" + salaNome + " › Fileira " + fileiraNome + ")";
    }
}
