package br.ifg.vertex.servico;

import br.ifg.vertex.arvore.ArvoreBinariaBusca;
import br.ifg.vertex.grafo.BuscasGrafo;
import br.ifg.vertex.grafo.Grafo;
import br.ifg.vertex.grafo.ResultadoBusca;
import br.ifg.vertex.modelo.Aresta;
import br.ifg.vertex.modelo.Localidade;
import br.ifg.vertex.modelo.TipoLocalidade;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A "porteira" que integra a árvore e o grafo.
 *
 * <p>O {@link SistemaCidades} é dono de uma {@link ArvoreBinariaBusca} (os dados das
 * localidades) e de um {@link Grafo} (as conexões entre elas) e é a ÚNICA forma de
 * alterar os dois. Assim, as regras de consistência do enunciado valem sempre:</p>
 * <ul>
 *   <li>nenhum vértice existe sem a localidade correspondente na árvore;</li>
 *   <li>nenhuma aresta usa localidade inexistente (a árvore é consultada ANTES do grafo);</li>
 *   <li>remover uma localidade remove também as conexões dela;</li>
 *   <li>atualizar uma localidade vale para as rotas, porque o grafo guarda só IDs e o
 *       nome é buscado na árvore na hora de mostrar;</li>
 *   <li>reconstruir a árvore balanceada não altera o grafo.</li>
 * </ul>
 *
 * <p>Toda violação de regra vira uma {@link RegraNegocioException} com mensagem pronta
 * para o usuário.</p>
 */
public class SistemaCidades {

    private final ArvoreBinariaBusca arvore;
    private final Grafo grafo;

    public SistemaCidades() {
        this(new ArvoreBinariaBusca(), new Grafo());
    }

    /** Só para testes: permite montar um sistema (até inconsistente) com árvore e grafo prontos. */
    SistemaCidades(ArvoreBinariaBusca arvore, Grafo grafo) {
        this.arvore = arvore;
        this.grafo = grafo;
    }

    /**
     * Acesso de LEITURA à árvore (percursos, altura, balanceamento...). Quem usa
     * não deve chamar métodos que alteram (inserir, remover, atualizar, reconstruir):
     * para isso existem os métodos deste sistema.
     */
    public ArvoreBinariaBusca getArvore() {
        return arvore;
    }

    /**
     * Acesso de LEITURA ao grafo (vizinhos, graus, algoritmos...). Para alterar,
     * use os métodos deste sistema.
     */
    public Grafo getGrafo() {
        return grafo;
    }

    // ------------------------------------------------------------------
    // Localidades
    // ------------------------------------------------------------------

    public boolean existeLocalidade(int id) {
        return arvore.contem(id);
    }

    /** Devolve a localidade ou lança erro se o ID não estiver cadastrado. */
    public Localidade exigirLocalidade(int id) {
        return arvore.pesquisar(id).orElseThrow(() ->
                new RegraNegocioException("Não existe localidade cadastrada com o ID " + id + "."));
    }

    /** Nome da localidade (buscado na árvore), ou "?" se o ID não existir. */
    public String nomeDe(int id) {
        return arvore.pesquisar(id).map(Localidade::getNome).orElse("?");
    }

    /**
     * Cadastra a localidade na árvore E cria o vértice dela no grafo.
     * Se o segundo passo falhar, o primeiro é desfeito: nunca fica uma localidade
     * "meio cadastrada".
     */
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

    /** Pesquisa por ID; junto vêm o nível do nó e o número de comparações da busca. */
    public Optional<ResultadoPesquisa> pesquisarLocalidade(int id) {
        return arvore.pesquisar(id).map(localidade ->
                new ResultadoPesquisa(localidade, arvore.nivel(id), arvore.comparacoesNaBusca(id)));
    }

    /** Atualiza nome, tipo e população; o ID não muda e o grafo nem é tocado. */
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

    /**
     * Quantas arestas (direcionadas) tocam a localidade: as que saem dela mais as que
     * chegam nela. Uma via de mão dupla conta 2.
     */
    public int quantidadeConexoes(int id) {
        exigirLocalidade(id);
        return grafo.grauEntrada(id) + grafo.grauSaida(id);
    }

    /**
     * Remove a localidade e todas as conexões dela. Mexe no grafo ANTES da árvore:
     * assim, se algo falhar no meio, nunca sobra uma aresta apontando para uma
     * localidade que já saiu da árvore.
     *
     * @return quantas arestas foram removidas junto
     */
    public int removerLocalidade(int id) {
        exigirLocalidade(id);
        int arestasRemovidas = grafo.grauEntrada(id) + grafo.grauSaida(id);
        grafo.removerVertice(id);
        arvore.remover(id);
        return arestasRemovidas;
    }

    /** Todas as localidades em ordem crescente de ID (percurso em ordem). */
    public List<Localidade> listarLocalidades() {
        return arvore.emOrdem();
    }

    // ------------------------------------------------------------------
    // Conexões
    // ------------------------------------------------------------------

    public boolean existeConexao(int origem, int destino) {
        exigirLocalidade(origem);
        exigirLocalidade(destino);
        return grafo.existeAresta(origem, destino);
    }

    /** Conexões que saem da localidade, em ordem crescente de destino. */
    public List<Aresta> conexoesDe(int id) {
        exigirLocalidade(id);
        return grafo.arestasDeSaida(id);
    }

    /**
     * Cria uma conexão. Primeiro consulta a árvore (as duas localidades precisam
     * existir); só então mexe no grafo.
     *
     * @param maoDupla {@code true} cria as duas arestas (ida e volta, com os mesmos pesos)
     */
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

    /**
     * Altera distância e tempo de uma conexão.
     *
     * @param tambemVolta {@code true} altera também a conexão de volta (que precisa existir),
     *                    mantendo os pesos das duas equivalentes
     */
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

    /**
     * Remove a conexão de {@code origem} para {@code destino}.
     *
     * @param tambemVolta {@code true} remove também a de volta (que precisa existir)
     */
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

    // ------------------------------------------------------------------
    // Árvore: balanceamento
    // ------------------------------------------------------------------

    /**
     * Reconstrói a árvore balanceada e confere que o grafo não mudou (mesmos vértices
     * e mesmas arestas, com os mesmos pesos).
     */
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

    // ------------------------------------------------------------------
    // Consistência
    // ------------------------------------------------------------------

    /**
     * Confere as regras de consistência entre árvore e grafo.
     *
     * @return a lista de problemas encontrados; vazia quando tudo está consistente
     */
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

    // ------------------------------------------------------------------
    // Apoio interno
    // ------------------------------------------------------------------

    private void exigirConexao(int origem, int destino) {
        if (!existeConexao(origem, destino)) {
            throw new RegraNegocioException("Não existe conexão de " + descrever(origem)
                    + " para " + descrever(destino) + ".");
        }
    }

    /** "50 (Luziânia)" para mensagens; só o ID se a localidade não existir. */
    private String descrever(int id) {
        return arvore.pesquisar(id)
                .map(localidade -> id + " (" + localidade.getNome() + ")")
                .orElse(String.valueOf(id));
    }

    /** BFS a partir de uma localidade: visita por "ondas" e dá o caminho com menos paradas. */
    public ResultadoBusca buscaEmLargura(int origem) {
        exigirLocalidade(origem);
        return BuscasGrafo.emLargura(grafo, origem);
    }

    /** DFS a partir de uma localidade: vai o mais fundo possível antes de voltar. */
    public ResultadoBusca buscaEmProfundidade(int origem) {
        exigirLocalidade(origem);
        return BuscasGrafo.emProfundidade(grafo, origem);
    }
}