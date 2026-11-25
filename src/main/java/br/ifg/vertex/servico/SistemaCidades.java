package br.ifg.vertex.servico;

import br.ifg.vertex.arvore.ArvoreBinariaBusca;
import br.ifg.vertex.grafo.BuscasGrafo;
import br.ifg.vertex.grafo.Conectividade;
import br.ifg.vertex.grafo.CriterioPeso;
import br.ifg.vertex.grafo.Dijkstra;
import br.ifg.vertex.grafo.Grafo;
import br.ifg.vertex.grafo.ResultadoBusca;
import br.ifg.vertex.grafo.ResultadoCaminho;
import br.ifg.vertex.modelo.Aresta;
import br.ifg.vertex.modelo.Localidade;
import br.ifg.vertex.modelo.TipoLocalidade;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SistemaCidades {

    private final ArvoreBinariaBusca arvore;
    private final Grafo grafo;

    public SistemaCidades() {
        this(new ArvoreBinariaBusca(), new Grafo());
    }

    SistemaCidades(ArvoreBinariaBusca arvore, Grafo grafo) {
        this.arvore = arvore;
        this.grafo = grafo;
    }

    public ArvoreBinariaBusca getArvore() {
        return arvore;
    }

    public Grafo getGrafo() {
        return grafo;
    }

    // Localidades

    public boolean existeLocalidade(int id) {
        return arvore.contem(id);
    }

    public Localidade exigirLocalidade(int id) {
        return arvore.pesquisar(id).orElseThrow(() ->
                new RegraNegocioException("Não existe localidade cadastrada com o ID " + id + "."));
    }

    public String nomeDe(int id) {
        return arvore.pesquisar(id).map(Localidade::getNome).orElse("?");
    }

    public Localidade cadastrarLocalidade(int id, String nome, TipoLocalidade tipo, int populacao) {
        Localidade nova;
        try {
            nova = new Localidade(id, nome, tipo, populacao);
        } catch (IllegalArgumentException e) {
            throw new RegraNegocioException(e.getMessage());
        }

        if (!arvore.inserir(nova)) {
            throw new RegraNegocioException("Já existe uma localidade com o ID " + id + ".");
        }
        try {
            if (!grafo.adicionarVertice(id)) {
                throw new IllegalStateException("o grafo já tinha um vértice com o ID " + id);
            }
        } catch (RuntimeException e) {
            arvore.remover(id); // desfaz o passo 1
            throw new RegraNegocioException("Não foi possível cadastrar a localidade " + id
                    + ": " + e.getMessage() + ".");
        }
        return nova;
    }

    public Optional<ResultadoPesquisa> pesquisarLocalidade(int id) {
        return arvore.pesquisar(id).map(localidade ->
                new ResultadoPesquisa(localidade, arvore.nivel(id), arvore.comparacoesNaBusca(id)));
    }

    public void atualizarLocalidade(int id, String nome, TipoLocalidade tipo, int populacao) {
        boolean atualizou;
        try {
            atualizou = arvore.atualizar(id, nome, tipo, populacao);
        } catch (IllegalArgumentException e) {
            throw new RegraNegocioException(e.getMessage());
        }
        if (!atualizou) {
            throw new RegraNegocioException("Não existe localidade cadastrada com o ID " + id + ".");
        }
    }

    public int quantidadeConexoes(int id) {
        exigirLocalidade(id);
        return grafo.grauEntrada(id) + grafo.grauSaida(id);
    }

    public int removerLocalidade(int id) {
        exigirLocalidade(id);
        int arestasRemovidas = grafo.grauEntrada(id) + grafo.grauSaida(id);
        grafo.removerVertice(id);
        arvore.remover(id);
        return arestasRemovidas;
    }

    public List<Localidade> listarLocalidades() {
        return arvore.emOrdem();
    }

    // Conexões

    public boolean existeConexao(int origem, int destino) {
        exigirLocalidade(origem);
        exigirLocalidade(destino);
        return grafo.existeAresta(origem, destino);
    }

    public List<Aresta> conexoesDe(int id) {
        exigirLocalidade(id);
        return grafo.arestasDeSaida(id);
    }

    public void conectar(int origem, int destino, double distanciaKm, int tempoMin, boolean maoDupla) {
        exigirLocalidade(origem);
        exigirLocalidade(destino);

        if (grafo.existeAresta(origem, destino)) {
            throw new RegraNegocioException("Já existe uma conexão de " + descrever(origem)
                    + " para " + descrever(destino) + ".");
        }
        if (maoDupla && grafo.existeAresta(destino, origem)) {
            throw new RegraNegocioException("Já existe uma conexão de " + descrever(destino)
                    + " para " + descrever(origem) + "; não dá para criar a via de mão dupla.");
        }
        try {
            if (maoDupla) {
                grafo.adicionarConexaoBidirecional(origem, destino, distanciaKm, tempoMin);
            } else {
                grafo.adicionarAresta(new Aresta(origem, destino, distanciaKm, tempoMin));
            }
        } catch (IllegalArgumentException e) {
            throw new RegraNegocioException(e.getMessage());
        }
    }

    public void alterarConexao(int origem, int destino, double distanciaKm, int tempoMin,
                               boolean tambemVolta) {
        exigirConexao(origem, destino);
        if (tambemVolta) {
            exigirConexao(destino, origem);
        }
        try {
            // O primeiro alterarPesos valida os valores; se forem inválidos lança antes de mudar algo.
            grafo.alterarPesos(origem, destino, distanciaKm, tempoMin);
            if (tambemVolta) {
                grafo.alterarPesos(destino, origem, distanciaKm, tempoMin);
            }
        } catch (IllegalArgumentException e) {
            throw new RegraNegocioException(e.getMessage());
        }
    }

    public void desconectar(int origem, int destino, boolean tambemVolta) {
        exigirConexao(origem, destino);
        if (tambemVolta) {
            exigirConexao(destino, origem);
        }
        grafo.removerAresta(origem, destino);
        if (tambemVolta) {
            grafo.removerAresta(destino, origem);
        }
    }

    // Árvore: balanceamento

    public ResultadoBalanceamento balancearArvore() {
        List<Integer> verticesAntes = grafo.vertices();
        List<Aresta> arestasAntes = grafo.arestas();
        boolean estavaBalanceada = arvore.estaBalanceada();
        int alturaAntes = arvore.altura();

        arvore.reconstruirBalanceada();

        if (!verticesAntes.equals(grafo.vertices()) || !arestasAntes.equals(grafo.arestas())) {
            throw new IllegalStateException("A reconstrução da árvore alterou o grafo.");
        }
        return new ResultadoBalanceamento(estavaBalanceada, alturaAntes, arvore.altura());
    }

    // Consistência

    public List<String> verificarConsistencia() {
        List<String> problemas = new ArrayList<>();
        Set<Integer> verticesDoGrafo = new HashSet<>(grafo.vertices());
        Set<Integer> idsDaArvore = new HashSet<>();

        int anterior = Integer.MIN_VALUE;
        boolean emOrdem = true;
        for (Localidade localidade : arvore.emOrdem()) {
            int id = localidade.getId();
            idsDaArvore.add(id);
            if (id <= anterior) {
                emOrdem = false;
            }
            anterior = id;
            if (!verticesDoGrafo.contains(id)) {
                problemas.add("A localidade " + id + " está na árvore, mas não tem vértice no grafo.");
            }
        }
        if (!emOrdem) {
            problemas.add("A árvore não está em ordem crescente de ID.");
        }
        for (int vertice : grafo.vertices()) {
            if (!idsDaArvore.contains(vertice)) {
                problemas.add("O vértice " + vertice + " não tem localidade cadastrada na árvore.");
            }
        }
        for (Aresta aresta : grafo.arestas()) {
            if (!idsDaArvore.contains(aresta.origem()) || !idsDaArvore.contains(aresta.destino())) {
                problemas.add("A conexão " + aresta.origem() + " -> " + aresta.destino()
                        + " usa uma localidade inexistente.");
            }
        }
        return problemas;
    }

    public boolean estaConsistente() {
        return verificarConsistencia().isEmpty();
    }

    // Apoio interno

    private void exigirConexao(int origem, int destino) {
        if (!existeConexao(origem, destino)) {
            throw new RegraNegocioException("Não existe conexão de " + descrever(origem)
                    + " para " + descrever(destino) + ".");
        }
    }

    private String descrever(int id) {
        return arvore.pesquisar(id)
                .map(localidade -> id + " (" + localidade.getNome() + ")")
                .orElse(String.valueOf(id));
    }

    public ResultadoBusca buscaEmLargura(int origem) {
        exigirLocalidade(origem);
        return BuscasGrafo.emLargura(grafo, origem);
    }

    public ResultadoBusca buscaEmProfundidade(int origem) {
        exigirLocalidade(origem);
        return BuscasGrafo.emProfundidade(grafo, origem);
    }

    public ResultadoCaminho menorRota(int origem, int destino, CriterioPeso criterio) {
        exigirLocalidade(origem);
        exigirLocalidade(destino);
        return Dijkstra.menorCaminho(grafo, origem, destino, criterio);
    }

    public ResultadoCaminho rotaComMenosTrechos(int origem, int destino) {
        exigirLocalidade(origem);
        exigirLocalidade(destino);
        return BuscasGrafo.menosArestas(grafo, origem, destino);
    }

    public ComparacaoRotas compararRotas(int origem, int destino) {
        return new ComparacaoRotas(
                rotaComMenosTrechos(origem, destino),
                menorRota(origem, destino, CriterioPeso.DISTANCIA),
                menorRota(origem, destino, CriterioPeso.TEMPO));
    }

    public List<List<Integer>> componentesConexas() {
        return Conectividade.componentes(grafo);
    }

    public boolean redeConexa() {
        return Conectividade.redeConexa(grafo);
    }

    public boolean alcanca(int origem, int destino) {
        exigirLocalidade(origem);
        exigirLocalidade(destino);
        return BuscasGrafo.emLargura(grafo, origem).alcancou(destino);
    }

    public List<List<Integer>> ciclos() {
        return Conectividade.ciclos(grafo);
    }
}