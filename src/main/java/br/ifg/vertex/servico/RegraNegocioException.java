package br.ifg.vertex.servico;

/**
 * Lançada quando uma regra do sistema é violada (ID repetido, localidade inexistente,
 * aresta duplicada etc.). A mensagem já vem pronta para ser mostrada ao usuário.
 *
 * <p>Ainda não é usada na etapa 1; passa a ser lançada pelo {@code SistemaCidades}
 * a partir da etapa 5.</p>
 */
public class RegraNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}