package br.ifg.vertex.grafo;

import br.ifg.vertex.modelo.Aresta;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Grafo {

    private final Map<Integer, List<Aresta>> adjacencia = new HashMap<>();

    // Vértices

    public boolean adicionarVertice(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("O ID do vértice deve ser um número positivo.");
        }
        if (adjacencia.containsKey(id)) {
            return false;
        }
        adjacencia.put(id, new ArrayList<>());
        return true;
    }

    public boolean removerVertice(int id) {
        if (!adjacencia.containsKey(id)) {
            return false;
        }
        adjacencia.remove(id);
        for (List<Aresta> saidas : adjacencia.values()) {
            saidas.removeIf(aresta -> aresta.destino() == id);
        }
        return true;
    }

    public boolean contemVertice(int id) {
        return adjacencia.containsKey(id);
    }

    public List<Integer> vertices() {
        List<Integer> ids = new ArrayList<>(adjacencia.keySet());
        Collections.sort(ids);
        return ids;
    }

    public int quantidadeVertices() {
        return adjacencia.size();
    }


    // Arestas

    public void adicionarAresta(Aresta aresta) {
        exigirVertice(aresta.origem());
        exigirVertice(aresta.destino());
        if (existeAresta(aresta.origem(), aresta.destino())) {
            throw new IllegalArgumentException("Já existe uma conexão de "
                    + aresta.origem() + " para " + aresta.destino() + ".");
        }
        inserirOrdenado(adjacencia.get(aresta.origem()), aresta);
    }

    public void adicionarConexaoBidirecional(int a, int b, double distanciaKm, int tempoMin) {
        Aresta ida = new Aresta(a, b, distanciaKm, tempoMin);
        Aresta volta = ida.inversa();
        exigirVertice(a);
        exigirVertice(b);
        if (existeAresta(a, b)) {
            throw new IllegalArgumentException("Já existe uma conexão de " + a + " para " + b + ".");
        }
        if (existeAresta(b, a)) {
            throw new IllegalArgumentException("Já existe uma conexão de " + b + " para " + a + ".");
        }
        inserirOrdenado(adjacencia.get(a), ida);
        inserirOrdenado(adjacencia.get(b), volta);
    }

    public boolean removerAresta(int origem, int destino) {
        List<Aresta> saidas = adjacencia.get(origem);
        return saidas != null && saidas.removeIf(aresta -> aresta.destino() == destino);
    }

    public boolean alterarPesos(int origem, int destino, double distanciaKm, int tempoMin) {
        List<Aresta> saidas = adjacencia.get(origem);
        if (saidas == null) {
            return false;
        }
        for (int i = 0; i < saidas.size(); i++) {
            Aresta atual = saidas.get(i);
            if (atual.destino() == destino) {
                saidas.set(i, atual.comPesos(distanciaKm, tempoMin));
                return true;
            }
        }
        return false;
    }

    public boolean existeAresta(int origem, int destino) {
        return obterAresta(origem, destino).isPresent();
    }

    public Optional<Aresta> obterAresta(int origem, int destino) {
        List<Aresta> saidas = adjacencia.get(origem);
        if (saidas == null) {
            return Optional.empty();
        }
        for (Aresta aresta : saidas) {
            if (aresta.destino() == destino) {
                return Optional.of(aresta);
            }
        }
        return Optional.empty();
    }

    public List<Aresta> arestas() {
        List<Aresta> todas = new ArrayList<>();
        for (int id : vertices()) {
            todas.addAll(adjacencia.get(id));
        }
        return todas;
    }

    public int quantidadeArestas() {
        int total = 0;
        for (List<Aresta> saidas : adjacencia.values()) {
            total += saidas.size();
        }
        return total;
    }


    // Vizinhança e graus

    public List<Aresta> arestasDeSaida(int id) {
        exigirVertice(id);
        return new ArrayList<>(adjacencia.get(id));
    }

    public List<Integer> vizinhos(int id) {
        exigirVertice(id);
        List<Integer> ids = new ArrayList<>();
        for (Aresta aresta : adjacencia.get(id)) {
            ids.add(aresta.destino());
        }
        return ids;
    }

    public int grauSaida(int id) {
        exigirVertice(id);
        return adjacencia.get(id).size();
    }

    public int grauEntrada(int id) {
        exigirVertice(id);
        int grau = 0;
        for (List<Aresta> saidas : adjacencia.values()) {
            for (Aresta aresta : saidas) {
                if (aresta.destino() == id) {
                    grau++;
                }
            }
        }
        return grau;
    }

    public List<Integer> verticesIsolados() {
        List<Integer> isolados = new ArrayList<>();
        for (int id : vertices()) {
            if (grauSaida(id) == 0 && grauEntrada(id) == 0) {
                isolados.add(id);
            }
        }
        return isolados;
    }

    private void exigirVertice(int id) {
        if (!adjacencia.containsKey(id)) {
            throw new IllegalArgumentException("O vértice " + id + " não existe no grafo.");
        }
    }

    private static void inserirOrdenado(List<Aresta> saidas, Aresta nova) {
        int posicao = 0;
        while (posicao < saidas.size() && saidas.get(posicao).destino() < nova.destino()) {
            posicao++;
        }
        saidas.add(posicao, nova);
    }
}