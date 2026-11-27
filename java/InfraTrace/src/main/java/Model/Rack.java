package Model;

/**
 * Rack (armário de servidores) dentro de uma fileira. Espelha a tabela `rack`:
 *
 *   rac_id          INT          -> id
 *   rac_nome        VARCHAR(128) -> nome        (obrigatório; único dentro da mesma fileira)
 *   rac_total_slots INT          -> totalSlots  (capacidade em U, de 1 a 60)
 *   fk_fil_id       INT          -> fileiraId   (obrigatório: todo rack pertence a uma fileira)
 *
 * "U" é a unidade de altura de rack (1U = 4,445 cm). Um rack de 42U tem 42 posições
 * (slots), numeradas de 1 a 42; um servidor de 2U ocupa dois slots seguidos.
 *
 * Campos a mais, só para exibição (não são colunas de rack; vêm da consulta):
 *   salaNome, fileiraNome -> onde o rack está (JOIN com fileira e sala);
 *   quantidadeAtivos      -> quantos ativos estão instalados nele (COUNT);
 *   maiorSlotOcupado      -> o slot mais alto ocupado (MAX), ou null se o rack está vazio.
 *                            A capacidade não pode ficar menor que ele (trigger do banco).
 */
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

    /** Usado quando o rack aparecer num JComboBox (ex.: no módulo de Ativos). */
    @Override
    public String toString() {
        return nome + " (" + salaNome + " › Fileira " + fileiraNome + ")";
    }
}
