package Exceptions;

/**
 * Erro de regra de negócio: algo que o USUÁRIO pode corrigir (campo vazio, nome repetido,
 * sala com fileiras que não pode ser excluída...). A mensagem já vem pronta para ser
 * mostrada na tela.
 *
 * Estende RuntimeException para não obrigar todo método a declarar "throws NegocioException".
 * O controller captura essa exceção e mostra a mensagem num aviso.
 */
public class NegocioException extends RuntimeException {

    public NegocioException(String mensagem) {
        super(mensagem);
    }
}
