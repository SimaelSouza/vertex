package br.ifg.vertex.arvore;

/**
 * Resultado da análise de balanceamento de UM nó: o ID e a altura das subárvores
 * esquerda e direita dele (em arestas; subárvore vazia tem altura -1).
 *
 * <p>Critério do enunciado: o nó está balanceado se a diferença entre as alturas
 * for de no máximo uma unidade.</p>
 *
 * @param id             ID da localidade guardada no nó
 * @param alturaEsquerda altura da subárvore esquerda (-1 se não houver filho esquerdo)
 * @param alturaDireita  altura da subárvore direita (-1 se não houver filho direito)
 */
public record BalanceamentoNo(int id, int alturaEsquerda, int alturaDireita) {

    /** Esquerda menos direita: positivo = pesa mais à esquerda, negativo = à direita. */
    public int diferenca() {
        return alturaEsquerda - alturaDireita;
    }

    /** Balanceado quando |diferença| &lt;= 1. */
    public boolean estaBalanceado() {
        return Math.abs(diferenca()) <= 1;
    }
}