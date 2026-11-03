package br.ifg.vertex.servico;

import br.ifg.vertex.modelo.Localidade;

/**
 * O que a pesquisa por ID devolve: a localidade e dados da busca na árvore.
 *
 * @param localidade  a localidade encontrada
 * @param nivel       nível do nó na árvore (a raiz é o nível 0)
 * @param comparacoes quantos nós a busca visitou até achar
 */
public record ResultadoPesquisa(Localidade localidade, int nivel, int comparacoes) {
}