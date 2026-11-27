package Model;

public class Fileira {

    private Integer id;
    private String nome;
    private Integer salaId;

    private String salaNome;
    private int quantidadeRacks;

    public Fileira() {
    }

    public Fileira(Integer id, String nome, Integer salaId) {
        this.id = id;
        this.nome = nome;
        this.salaId = salaId;
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

    public Integer getSalaId() {
        return salaId;
    }

    public void setSalaId(Integer salaId) {
        this.salaId = salaId;
    }

    public String getSalaNome() {
        return salaNome;
    }

    public void setSalaNome(String salaNome) {
        this.salaNome = salaNome;
    }

    public int getQuantidadeRacks() {
        return quantidadeRacks;
    }

    public void setQuantidadeRacks(int quantidadeRacks) {
        this.quantidadeRacks = quantidadeRacks;
    }

    @Override
    public String toString() {
        return salaNome + " › Fileira " + nome;
    }
}
