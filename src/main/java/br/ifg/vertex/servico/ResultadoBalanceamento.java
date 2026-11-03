package br.ifg.vertex.servico;

/**
 * Resumo de uma reconstrução balanceada da árvore.
 *
 * @param estavaBalanceada se a árvore já estava balanceada antes
 * @param alturaAntes      altura antes de reconstruir (em arestas)
 * @param alturaDepois     altura depois de reconstruir
 */
public record ResultadoBalanceamento(boolean estavaBalanceada, int alturaAntes, int alturaDepois) {
}