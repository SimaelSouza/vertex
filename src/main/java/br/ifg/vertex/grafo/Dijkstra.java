package br.ifg.vertex.grafo;

import br.ifg.vertex.modelo.Aresta;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Dijkstra {

    private static final double INFINITO = Double.POSITIVE_INFINITY;

    private Dijkstra() {
    }

    public static ResultadoCaminho menorCaminho(Grafo grafo, int origem, int destino, CriterioPeso criterio) {
        exigir(grafo, origem);
        exigir(grafo, destino);

        Map<Integer, Double> custo = new HashMap<>();
        Map<Integer, Aresta> anterior = new HashMap<>();
        Set<Integer> fechados = new HashSet<>();
        List<Integer> vertices = grafo.vertices();

        custo.put(origem, 0.0);
        while (true) {
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
                break;
            }
            fechados.add(atual);
            if (atual == destino) {
                break;
            }
            for (Aresta aresta : grafo.arestasDeSaida(atual)) {
                if (fechados.contains(aresta.destino())) {
                    continue;
                }
                double novoCusto = menor + criterio.pesoDe(aresta);
                if (novoCusto < custo.getOrDefault(aresta.destino(), INFINITO)) {
                    custo.put(aresta.destino(), novoCusto);
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