package Model;

/**
 * Categoria de ativo (Servidor, Switch, Storage...). Espelha a tabela `tipo_ativo`:
 *
 *   tpa_id        INT          -> id
 *   tpa_nome      VARCHAR(128) -> nome       (obrigatório e único)
 *   tpa_descricao VARCHAR(512) -> descricao  (opcional)
 */
public class TipoAtivo {

    private Integer id;
    private String nome;
    private String descricao;

    public TipoAtivo() {
    }

    public TipoAtivo(Integer id, String nome, String descricao) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    /** O JComboBox de tipos mostra o que o toString devolve. */
    @Override
    public String toString() {
        return nome;
    }
}
