package br.ifg.vertex.grafo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Buscas em grafo implementadas à mão: em largura (BFS) e em profundidade (DFS).
 *
 * <p>As duas respeitam o sentido das arestas e visitam os vizinhos em ordem crescente de
 * ID (é a ordem das listas de adjacência), então o resultado é sempre o mesmo para o
 * mesmo grafo. Cada busca visita cada vértice e cada aresta no máximo uma vez: O(V + E).</p>
 */
public final class BuscasGrafo {

    private BuscasGrafo() {
    }

    /**
     * BFS: visita primeiro todos os vizinhos, depois os vizinhos dos vizinhos (usa uma fila).
     * A profundidade de cada vértice é o MENOR número de arestas desde a origem, então o
     * caminho encontrado é o que tem menos paradas.
     */
    public static ResultadoBusca emLargura(Grafo grafo, int origem) {
        exigirOrigem(grafo, origem);
        List<Integer> ordem = new ArrayList<>();
        Map<Integer, Integer> pai = new HashMap<>();
        Map<Integer, Integer> profundidade = new HashMap<>(); // também serve de "já visitado"
        Queue<Integer> fila = new ArrayDeque<>();

        profundidade.put(origem, 0);
        fila.add(origem);
        while (!fila.isEmpty()) {
            int atual = fila.remove();
            ordem.add(atual);
            for (int vizinho : grafo.vizinhos(atual)) {
                if (!profundidade.containsKey(vizinho)) { // marca ao enfileirar: ninguém entra duas vezes
                    profundidade.put(vizinho, profundidade.get(atual) + 1);
                    pai.put(vizinho, atual);
                    fila.add(vizinho);
                }
            }
        }
        return montar(grafo, origem, ordem, pai, profundidade);
    }

    /**
     * DFS: vai o mais fundo possível por um caminho e só volta quando não há mais saída
     * (usa uma pilha). É feita sem recursão para não estourar a pilha de chamadas em
     * grafos grandes, mas visita na mesma ordem da versão recursiva. O caminho encontrado
     * NÃO é necessariamente o mais curto.
     */
    public static ResultadoBusca emProfundidade(Grafo grafo, int origem) {
        exigirOrigem(grafo, origem);
        List<Integer> ordem = new ArrayList<>();
        Map<Integer, Integer> pai = new HashMap<>();
        Map<Integer, Integer> profundidade = new HashMap<>();
        Deque<Quadro> pilha = new ArrayDeque<>();

        profundidade.put(origem, 0);
        ordem.add(origem);
        pilha.push(new Quadro(origem, grafo.vizinhos(origem)));
        while (!pilha.isEmpty()) {
            Quadro topo = pilha.peek();
            if (topo.proximo < topo.vizinhos.size()) {
                int vizinho = topo.vizinhos.get(topo.proximo++);
                if (!profundidade.containsKey(vizinho)) {
                    profundidade.put(vizinho, profundidade.get(topo.vertice) + 1);
                    pai.put(vizinho, topo.vertice);
                    ordem.add(vizinho);
                    pilha.push(new Quadro(vizinho, grafo.vizinhos(vizinho)));
                }
            } else {
                pilha.pop(); // acabaram os vizinhos deste vértice: volta um passo
            }
        }
        return montar(grafo, origem, ordem, pai, profundidade);
    }

    private static void exigirOrigem(Grafo grafo, int origem) {
        if (!grafo.contemVertice(origem)) {
            throw new IllegalArgumentException("O vértice " + origem + " não existe no grafo.");
        }
    }

    private static ResultadoBusca montar(Grafo grafo, int origem, List<Integer> ordem,
                                         Map<Integer, Integer> pai, Map<Integer, Integer> profundidade) {
        List<Integer> naoAlcancados = new ArrayList<>();
        for (int vertice : grafo.vertices()) {
            if (!profundidade.containsKey(vertice)) {
                naoAlcancados.add(vertice);
            }
        }
        return new ResultadoBusca(origem, ordem, pai, profundidade, naoAlcancados);
    }

    /** Um "passo" da DFS: o vértice, os vizinhos dele e qual vizinho tentar a seguir. */
    private static final class Quadro {
        private final int vertice;
        private final List<Integer> vizinhos;
        private int proximo;

        private Quadro(int vertice, List<Integer> vizinhos) {
            this.vertice = vertice;
            this.vizinhos = vizinhos;
        }
    }
}