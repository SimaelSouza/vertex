package br.ifg.vertex.grafo;

import br.ifg.vertex.modelo.Aresta;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Grafo direcionado e ponderado, implementado com LISTAS DE ADJACÊNCIA.
 *
 * <p>Cada vértice (identificado pelo ID da localidade) tem uma lista com as arestas
 * que SAEM dele, mantida em ordem crescente de ID de destino. Isso deixa BFS e DFS
 * determinísticos sem precisar ordenar a cada busca. O {@link HashMap} e o
 * {@link ArrayList} são só contêineres; a estrutura, as regras e os algoritmos
 * são nossos.</p>
 *
 * <p>Esta classe não conhece a árvore de localidades: quem garante que todo vértice
 * tem uma localidade é o {@code SistemaCidades}. Aqui o grafo apenas protege as
 * próprias regras (sem arestas duplicadas, sem vértices inexistentes) e, quando algo
 * é violado, lança {@link IllegalArgumentException} com mensagem em português.</p>
 */
public class Grafo {

    private final Map<Integer, List<Aresta>> adjacencia = new HashMap<>();

    // ------------------------------------------------------------------
    // Vértices
    // ------------------------------------------------------------------

    /**
     * @return {@code true} se criou; {@code false} se o vértice já existia
     * @throws IllegalArgumentException se o ID não for positivo
     */
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

    /**
     * Remove o vértice e TODAS as arestas incidentes: as que saem dele (somem junto
     * com a lista) e as que chegam nele (precisam ser procuradas nas listas dos outros).
     *
     * @return {@code true} se removeu; {@code false} se o vértice não existia
     */
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

    /** Todos os vértices, em ordem crescente de ID. */
    public List<Integer> vertices() {
        List<Integer> ids = new ArrayList<>(adjacencia.keySet());
        Collections.sort(ids);
        return ids;
    }

    public int quantidadeVertices() {
        return adjacencia.size();
    }

    // ------------------------------------------------------------------
    // Arestas
    // ------------------------------------------------------------------

    /**
     * Adiciona uma aresta direcionada (mão única).
     *
     * @throws IllegalArgumentException se origem ou destino não existirem no grafo
     *                                  ou se já existir aresta de origem para destino
     */
    public void adicionarAresta(Aresta aresta) {
        exigirVertice(aresta.origem());
        exigirVertice(aresta.destino());
        if (existeAresta(aresta.origem(), aresta.destino())) {
            throw new IllegalArgumentException("Já existe uma conexão de "
                    + aresta.origem() + " para " + aresta.destino() + ".");
        }
        inserirOrdenado(adjacencia.get(aresta.origem()), aresta);
    }

    /**
     * Adiciona uma via de mão dupla: DUAS arestas, com os mesmos pesos. É tudo ou
     * nada: se qualquer uma das duas já existir (ou algum dado for inválido), nada
     * é criado.
     */
    public void adicionarConexaoBidirecional(int a, int b, double distanciaKm, int tempoMin) {
        Aresta ida = new Aresta(a, b, distanciaKm, tempoMin); // valida os dados
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

    /**
     * Remove só a aresta de {@code origem} para {@code destino} (a volta, se existir, fica).
     *
     * @return {@code true} se removeu; {@code false} se a aresta não existia
     */
    public boolean removerAresta(int origem, int destino) {
        List<Aresta> saidas = adjacencia.get(origem);
        return saidas != null && saidas.removeIf(aresta -> aresta.destino() == destino);
    }

    /**
     * Troca a distância e o tempo da aresta de {@code origem} para {@code destino}.
     * Os novos valores são validados antes de qualquer mudança.
     *
     * @return {@code true} se alterou; {@code false} se a aresta não existia
     * @throws IllegalArgumentException se os novos pesos forem inválidos
     */
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

    /**
     * Adjacência: existe aresta saindo de {@code origem} e chegando em {@code destino}?
     * É direcional: A→B existir não significa que B→A exista.
     */
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

    /** Todas as arestas, ordenadas por origem e, dentro de cada origem, por destino. */
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

    // ------------------------------------------------------------------
    // Vizinhança e graus
    // ------------------------------------------------------------------

    /** Arestas que SAEM do vértice, em ordem crescente de destino (cópia; pode ser alterada à vontade). */
    public List<Aresta> arestasDeSaida(int id) {
        exigirVertice(id);
        return new ArrayList<>(adjacencia.get(id));
    }

    /** IDs dos vizinhos (destinos das arestas de saída), em ordem crescente. */
    public List<Integer> vizinhos(int id) {
        exigirVertice(id);
        List<Integer> ids = new ArrayList<>();
        for (Aresta aresta : adjacencia.get(id)) {
            ids.add(aresta.destino());
        }
        return ids;
    }

    /** Grau de saída: quantas arestas saem do vértice. */
    public int grauSaida(int id) {
        exigirVertice(id);
        return adjacencia.get(id).size();
    }

    /** Grau de entrada: quantas arestas chegam ao vértice (exige olhar as listas de todos). */
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

    /** Vértices isolados: sem nenhuma aresta saindo nem chegando. Em ordem crescente. */
    public List<Integer> verticesIsolados() {
        List<Integer> isolados = new ArrayList<>();
        for (int id : vertices()) {
            if (grauSaida(id) == 0 && grauEntrada(id) == 0) {
                isolados.add(id);
            }
        }
        return isolados;
    }

    // ------------------------------------------------------------------
    // Apoio interno
    // ------------------------------------------------------------------

    private void exigirVertice(int id) {
        if (!adjacencia.containsKey(id)) {
            throw new IllegalArgumentException("O vértice " + id + " não existe no grafo.");
        }
    }

    /** Insere mantendo a lista em ordem crescente de ID de destino. */
    private static void inserirOrdenado(List<Aresta> saidas, Aresta nova) {
        int posicao = 0;
        while (posicao < saidas.size() && saidas.get(posicao).destino() < nova.destino()) {
            posicao++;
        }
        saidas.add(posicao, nova);
    }
}