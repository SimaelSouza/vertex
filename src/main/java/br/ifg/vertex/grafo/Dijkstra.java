package br.ifg.vertex.grafo;

import br.ifg.vertex.modelo.Aresta;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Algoritmo de Dijkstra escrito à mão: o caminho de menor custo entre duas localidades.
 *
 * <p>Um único método serve para os dois critérios; só muda o peso lido de cada aresta
 * ({@link CriterioPeso}). Em vez de fila de prioridade, a cada passo faz uma varredura linear
 * procurando o vértice ainda aberto de menor custo, o que dá O(V²). Os pesos são sempre
 * positivos (a {@link Aresta} garante), condição necessária para o Dijkstra funcionar.</p>
 *
 * <p>Se houver dois caminhos com o mesmo custo, vale o que foi encontrado primeiro; como a
 * varredura segue a ordem crescente de ID, o resultado é sempre o mesmo.</p>
 */
public final class Dijkstra {

    private static final double INFINITO = Double.POSITIVE_INFINITY;

    private Dijkstra() {
    }

    public static ResultadoCaminho menorCaminho(Grafo grafo, int origem, int destino, CriterioPeso criterio) {
        exigir(grafo, origem);
        exigir(grafo, destino);

        Map<Integer, Double> custo = new HashMap<>();      // ausente = ainda infinito
        Map<Integer, Aresta> anterior = new HashMap<>();   // aresta usada para chegar em cada vértice
        Set<Integer> fechados = new HashSet<>();           // vértices com custo já definitivo
        List<Integer> vertices = grafo.vertices();         // em ordem crescente de ID

        custo.put(origem, 0.0);
        while (true) {
            // varredura linear: vértice aberto de menor custo
            int atual = -1;
            double menor = INFINITO;
            for (int vertice : vertices) {
                double c = custo.getOrDefault(vertice, INFINITO);
                if (!fechados.contains(vertice) && c < menor) {
                    menor = c;
                    atual = vertice;
                }
            }
            if (atual == -1) {
                break; // nada mais é alcançável
            }
            fechados.add(atual);
            if (atual == destino) {
                break; // o custo do destino já é definitivo
            }
            for (Aresta aresta : grafo.arestasDeSaida(atual)) {
                if (fechados.contains(aresta.destino())) {
                    continue;
                }
                double novoCusto = menor + criterio.pesoDe(aresta);
                if (novoCusto < custo.getOrDefault(aresta.destino(), INFINITO)) {
                    custo.put(aresta.destino(), novoCusto); // relaxamento
                    anterior.put(aresta.destino(), aresta);
                }
            }
        }

        if (!fechados.contains(destino)) {
            return ResultadoCaminho.inexistente(origem, destino);
        }
        List<Aresta> trechos = new ArrayList<>();
        int atual = destino;
        while (atual != origem) {
            Aresta aresta = anterior.get(atual);
            trechos.add(aresta);
            atual = aresta.origem();
        }
        Collections.reverse(trechos);
        return ResultadoCaminho.deTrechos(origem, destino, trechos);
    }

    private static void exigir(Grafo grafo, int vertice) {
        if (!grafo.contemVertice(vertice)) {
            throw new IllegalArgumentException("O vértice " + vertice + " não existe no grafo.");
        }
    }
}