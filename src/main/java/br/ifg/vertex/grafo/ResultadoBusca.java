package br.ifg.vertex.grafo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Resultado de uma busca (BFS ou DFS) a partir de uma origem.
 *
 * @param origem        vértice onde a busca começou
 * @param ordemVisita   vértices alcançados, na ordem em que foram visitados
 * @param pai           para cada vértice alcançado (exceto a origem), de quem ele "veio"
 * @param profundidade  para cada vértice alcançado, quantas arestas a busca percorreu até ele
 * @param naoAlcancados vértices do grafo que a busca não conseguiu alcançar (em ordem crescente)
 */
public record ResultadoBusca(
        int origem,
        List<Integer> ordemVisita,
        Map<Integer, Integer> pai,
        Map<Integer, Integer> profundidade,
        List<Integer> naoAlcancados) {

    public ResultadoBusca {
        ordemVisita = List.copyOf(ordemVisita);
        pai = Map.copyOf(pai);
        profundidade = Map.copyOf(profundidade);
        naoAlcancados = List.copyOf(naoAlcancados);
    }

    public boolean alcancou(int vertice) {
        return profundidade.containsKey(vertice);
    }

    public int quantidadeAlcancados() {
        return ordemVisita.size();
    }

    /**
     * Caminho que a busca encontrou da origem até o destino (os dois incluídos), montado
     * seguindo os "pais" de trás para frente. Lista vazia se o destino não foi alcançado.
     */
    public List<Integer> caminhoAte(int destino) {
        if (!alcancou(destino)) {
            return List.of();
        }
        List<Integer> caminho = new ArrayList<>();
        Integer atual = destino;
        while (atual != null) {
            caminho.add(atual);
            atual = pai.get(atual); // a origem não tem pai: o laço termina nela
        }
        Collections.reverse(caminho);
        return caminho;
    }
}