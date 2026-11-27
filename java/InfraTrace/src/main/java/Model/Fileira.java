package Model;

/**
 * Fileira de racks dentro de uma sala. Espelha a tabela `fileira`:
 *
 *   fil_id    INT         -> id
 *   fil_nome  VARCHAR(10) -> nome     (obrigatório; único dentro da mesma sala)
 *   fk_sal_id INT         -> salaId   (obrigatório: toda fileira pertence a uma sala)
 *
 * Dois campos a mais, só para exibição (não são colunas de fileira; vêm da consulta):
 *   salaNome        -> nome da sala, trazido pelo JOIN com a tabela sala;
 *   quantidadeRacks -> quantos racks a fileira tem, contados com COUNT na consulta.
 */
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

    /** Usado quando a fileira aparecer num JComboBox (ex.: no cadastro de racks). */
    @Override
    public String toString() {
        return salaNome + " › Fileira " + nome;
    }
}
