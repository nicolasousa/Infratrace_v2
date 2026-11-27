package Model;

/**
 * Sala física do data center. Espelha a tabela `sala`:
 *
 *   sal_id        INT          -> id
 *   sal_nome      VARCHAR(128) -> nome       (obrigatório e único)
 *   sal_descricao VARCHAR(512) -> descricao  (opcional)
 *
 * O model só guarda dados: não tem SQL nem regra de negócio.
 */
public class Sala {

    // Integer (e não int) porque uma sala ainda não gravada não tem id: fica null.
    private Integer id;
    private String nome;
    private String descricao;

    public Sala() {
    }

    public Sala(Integer id, String nome, String descricao) {
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

    /** O JComboBox do Swing mostra o toString(): assim a lista de salas exibe o nome. */
    @Override
    public String toString() {
        return nome;
    }
}
