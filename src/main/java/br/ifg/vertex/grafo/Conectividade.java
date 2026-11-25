package br.ifg.vertex.grafo;

import br.ifg.vertex.modelo.Aresta;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public final class Conectividade {

    private static final int CINZA = 1;
    private static final int PRETO = 2;

    private Conectividade() {
    }

    public static List<List<Integer>> componentes(Grafo grafo) {
        Map<Integer, Set<Integer>> vizinhosNaoDirecionados = new HashMap<>();
        for (int vertice : grafo.vertices()) {
            vizinhosNaoDirecionados.put(vertice, new HashSet<>());
        }
        for (Aresta aresta : grafo.arestas()) {
            vizinhosNaoDirecionados.get(aresta.origem()).add(aresta.destino());
            vizinhosNaoDirecionados.get(aresta.destino()).add(aresta.origem());
        }

        Set<Integer> visitados = new HashSet<>();
        List<List<Integer>> componentes = new ArrayList<>();
        for (int inicio : grafo.vertices()) {
            if (visitados.contains(inicio)) {
                continue;
            }
            List<Integer> componente = new ArrayList<>();
            Queue<Integer> fila = new ArrayDeque<>();
            visitados.add(inicio);
            fila.add(inicio);
            while (!fila.isEmpty()) {
                int atual = fila.remove();
                componente.add(atual);
                for (int vizinho : vizinhosNaoDirecionados.get(atual)) {
                    if (visitados.add(vizinho)) {
                        fila.add(vizinho);
                    }
                }
            }
            Collections.sort(componente);
            componentes.add(componente);
        }
        return componentes;
    }

    /** A rede é conexa se não está vazia e tem uma única componente. */
    public static boolean redeConexa(Grafo grafo) {
        return componentes(grafo).size() == 1;
    }

    public static List<List<Integer>> ciclos(Grafo grafo) {
        Map<Aresta, Integer> cor = new HashMap<>(); // ausente = branco
        List<List<Integer>> achados = new ArrayList<>();
        Set<List<Integer>> jaVistos = new HashSet<>();
        for (Aresta inicial : grafo.arestas()) {
            if (!cor.containsKey(inicial)) {
                explorar(grafo, inicial, cor, achados, jaVistos);
            }
        }
        return achados;
    }

    private static void explorar(Grafo grafo, Aresta inicial, Map<Aresta, Integer> cor,
                                 List<List<Integer>> achados, Set<List<Integer>> jaVistos) {
        Deque<Quadro> pilha = new ArrayDeque<>();
        cor.put(inicial, CINZA);
        pilha.push(new Quadro(inicial, seguintes(grafo, inicial)));
        while (!pilha.isEmpty()) {
            Quadro topo = pilha.peek();
            if (topo.proximo < topo.seguintes.size()) {
                Aresta proxima = topo.seguintes.get(topo.proximo++);
                Integer c = cor.get(proxima);
                if (c == null) {
                    cor.put(proxima, CINZA);
                    pilha.push(new Quadro(proxima, seguintes(grafo, proxima)));
                } else if (c.intValue() == CINZA) { // voltou a uma aresta ainda "na pilha": ciclo
                    List<Integer> ciclo = canonico(simplificar(origensAte(pilha, proxima)));
                    if (jaVistos.add(ciclo)) {
                        achados.add(ciclo);
                    }
                }
            } else {
                cor.put(topo.aresta, PRETO);
                pilha.pop();
            }
        }
    }

    private static List<Aresta> seguintes(Grafo grafo, Aresta aresta) {
        List<Aresta> lista = new ArrayList<>();
        for (Aresta proxima : grafo.arestasDeSaida(aresta.destino())) {
            if (proxima.destino() != aresta.origem()) {
                lista.add(proxima);
            }
        }
        return lista;
    }

    private static List<Integer> origensAte(Deque<Quadro> pilha, Aresta alvo) {
        List<Integer> origens = new ArrayList<>();
        for (Quadro quadro : pilha) { // do topo para a base
            origens.add(quadro.aresta.origem());
            if (quadro.aresta.equals(alvo)) {
                break;
            }
        }
        Collections.reverse(origens);
        return origens;
    }

    /** Se o percurso passou duas vezes pelo mesmo vértice, fica só com o laço simples. */
    private static List<Integer> simplificar(List<Integer> percurso) {
        Map<Integer, Integer> posicao = new HashMap<>();
        for (int j = 0; j < percurso.size(); j++) {
            Integer anterior = posicao.put(percurso.get(j), j);
            if (anterior != null) {
                return new ArrayList<>(percurso.subList(anterior, j));
            }
        }
        return percurso;
    }

    /** Gira a lista para começar pelo menor ID, assim o mesmo ciclo nunca aparece duas vezes. */
    private static List<Integer> canonico(List<Integer> ciclo) {
        int indiceMenor = 0;
        for (int i = 1; i < ciclo.size(); i++) {
            if (ciclo.get(i) < ciclo.get(indiceMenor)) {
                indiceMenor = i;
            }
        }
        List<Integer> girado = new ArrayList<>(ciclo.subList(indiceMenor, ciclo.size()));
        girado.addAll(ciclo.subList(0, indiceMenor));
        return girado;
    }

    private static final class Quadro {
        private final Aresta aresta;
        private final List<Aresta> seguintes;
        private int proximo;

        private Quadro(Aresta aresta, List<Aresta> seguintes) {
            this.aresta = aresta;
            this.seguintes = seguintes;
        }
    }
}