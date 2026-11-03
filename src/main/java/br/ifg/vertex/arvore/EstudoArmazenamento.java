package br.ifg.vertex.arvore;

/**
 * Números do estudo comparativo de armazenamento: quantas posições um vetor por
 * níveis (índice começando em 1) precisaria para guardar a árvore, e quantas
 * delas ficariam vazias.
 *
 * @param nos                 quantidade de nós reais da árvore
 * @param altura              altura da árvore em arestas (-1 se vazia)
 * @param posicoesNecessarias posições 1..N do vetor, com N = 2^(altura+1) - 1
 *                            (0 se vazia; {@link Long#MAX_VALUE} se nem um long comporta)
 * @param posicoesVazias      posições necessárias menos nós reais: memória desperdiçada
 * @param percentualVazio     posições vazias em % das necessárias (0 se vazia)
 */
public record EstudoArmazenamento(
        int nos,
        int altura,
        long posicoesNecessarias,
        long posicoesVazias,
        double percentualVazio) {

    /** Monta o estudo a partir da quantidade de nós e da altura da árvore. */
    public static EstudoArmazenamento de(int nos, int altura) {
        long necessarias = ArvoreBinariaBusca.posicoesNecessarias(altura);
        long vazias = necessarias - nos;
        double percentual = necessarias == 0 ? 0.0 : (double) vazias / necessarias * 100.0;
        return new EstudoArmazenamento(nos, altura, necessarias, vazias, percentual);
    }

    /** Dá para realmente alocar o vetor ({@link ArvoreBinariaBusca#paraVetor()})? */
    public boolean cabeNoVetor() {
        return posicoesNecessarias <= ArvoreBinariaBusca.LIMITE_VETOR;
    }
}