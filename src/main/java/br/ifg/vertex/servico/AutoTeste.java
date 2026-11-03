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
import java.util.List;

/**
 * Autoteste do programa (opção 15 do menu).
 *
 * <p>Cada caso monta as PRÓPRIAS estruturas (uma árvore, um grafo ou um sistema novo), então
 * rodar os testes nunca mexe nos dados que estão cadastrados no programa. Os casos que usam
 * o {@link SistemaCidades} terminam conferindo {@code verificarConsistencia()}.</p>
 */
public final class AutoTeste {

    private static final String ARVORE = "Árvore";
    private static final String BALANCEAMENTO = "Balanceamento";
    private static final String VETOR = "Vetor";
    private static final String GRAFO = "Grafo";
    private static final String ALGORITMOS = "Algoritmos";
    private static final String INTEGRACAO = "Integração";

    private AutoTeste() {
    }

    /** Roda os casos na ordem; uma falha (ou exceção inesperada) não impede os demais. */
    public static List<ResultadoTeste> executar(List<CasoDeTeste> casos) {
        List<ResultadoTeste> resultados = new ArrayList<>();
        for (CasoDeTeste caso : casos) {
            try {
                caso.verificacao().run();
                resultados.add(new ResultadoTeste(caso.grupo(), caso.nome(), true, ""));
            } catch (AssertionError e) {
                resultados.add(new ResultadoTeste(caso.grupo(), caso.nome(), false, e.getMessage()));
            } catch (RuntimeException e) {
                resultados.add(new ResultadoTeste(caso.grupo(), caso.nome(), false,
                        "exceção inesperada: " + e.getClass().getSimpleName() + " - " + e.getMessage()));
            }
        }
        return resultados;
    }

    /** Os casos da parte de domínio (árvore, grafo, algoritmos e integração). */
    public static List<CasoDeTeste> casos() {
        List<CasoDeTeste> c = new ArrayList<>();
        casosDaArvore(c);
        casosDeBalanceamentoEVetor(c);
        casosDoGrafo(c);
        casosDeAlgoritmos(c);
        casosDeIntegracao(c);
        return c;
    }

    // ---------------- ferramentas de montagem ----------------

    private static Localidade loc(int id) {
        return new Localidade(id, "Local " + id, TipoLocalidade.CIDADE, 1000 + id);
    }

    private static ArvoreBinariaBusca arvoreCom(int... ids) {
        ArvoreBinariaBusca arvore = new ArvoreBinariaBusca();
        for (int id : ids) {
            arvore.inserir(loc(id));
        }
        return arvore;
    }

    private static List<Integer> ids(ArvoreBinariaBusca arvore) {
        List<Integer> ids = new ArrayList<>();
        for (Localidade l : arvore.emOrdem()) {
            ids.add(l.getId());
        }
        return ids;
    }

    private static Grafo grafoCom(int quantidade) {
        Grafo grafo = new Grafo();
        for (int id = 1; id <= quantidade; id++) {
            grafo.adicionarVertice(id);
        }
        return grafo;
    }

    private static SistemaCidades sistemaDeTeste() {
        SistemaCidades sistema = new SistemaCidades();
        DadosTeste.carregarTudo(sistema);
        return sistema;
    }

    private static void adicionar(List<CasoDeTeste> casos, String grupo, String nome, Runnable verificacao) {
        casos.add(new CasoDeTeste(grupo, nome, verificacao));
    }

    // ---------------- árvore ----------------

    private static void casosDaArvore(List<CasoDeTeste> c) {
        adicionar(c, ARVORE, "Árvore vazia: pesquisar, remover e percursos não quebram", () -> {
            ArvoreBinariaBusca arvore = new ArvoreBinariaBusca();
            Verifica.verdadeiro(arvore.estaVazia(), "deveria estar vazia");
            Verifica.verdadeiro(arvore.pesquisar(1).isEmpty(), "pesquisar numa árvore vazia");
            Verifica.verdadeiro(!arvore.remover(1), "remover numa árvore vazia");
            Verifica.verdadeiro(arvore.emOrdem().isEmpty() && arvore.porNiveis().isEmpty(), "percursos vazios");
            Verifica.igual(-1, arvore.altura(), "altura da árvore vazia");
        });
        adicionar(c, ARVORE, "ID duplicado é rejeitado", () -> {
            ArvoreBinariaBusca arvore = arvoreCom(10);
            Verifica.verdadeiro(!arvore.inserir(loc(10)), "inserir ID repetido deveria devolver false");
            Verifica.igual(1, arvore.quantidadeNos(), "quantidade de nós");
        });
        adicionar(c, ARVORE, "Remover folha, nó com 1 filho, nó com 2 filhos e a raiz", () -> {
            ArvoreBinariaBusca arvore = arvoreCom(50, 30, 70, 20, 40, 60, 80);
            Verifica.verdadeiro(arvore.remover(20), "remover folha");
            Verifica.igual(List.of(30, 40, 50, 60, 70, 80), ids(arvore), "depois de remover a folha 20");
            Verifica.verdadeiro(arvore.remover(30), "remover nó com 1 filho (o 40)");
            Verifica.igual(List.of(40, 50, 60, 70, 80), ids(arvore), "depois de remover o 30");
            Verifica.verdadeiro(arvore.remover(70), "remover nó com 2 filhos");
            Verifica.igual(List.of(40, 50, 60, 80), ids(arvore), "depois de remover o 70");
            Verifica.verdadeiro(arvore.remover(50), "remover a raiz");
            Verifica.igual(List.of(40, 60, 80), ids(arvore), "depois de remover a raiz");
            Verifica.verdadeiro(arvore.raiz().isPresent() && arvore.raiz().get().getId() != 50, "a raiz mudou");
        });
        adicionar(c, ARVORE, "Atualizar mantém o ID e a posição do nó", () -> {
            ArvoreBinariaBusca arvore = arvoreCom(50, 30, 70);
            int nivelAntes = arvore.nivel(30);
            Verifica.verdadeiro(arvore.atualizar(30, "Novo nome", TipoLocalidade.BAIRRO, 5), "atualizar");
            Localidade l = arvore.pesquisar(30).orElseThrow();
            Verifica.igual("Novo nome", l.getNome(), "nome novo");
            Verifica.igual(30, l.getId(), "o ID não muda");
            Verifica.igual(nivelAntes, arvore.nivel(30), "o nível não muda");
        });
        adicionar(c, ARVORE, "Em ordem continua crescente depois de várias remoções", () -> {
            ArvoreBinariaBusca arvore = arvoreCom(50, 30, 70, 20, 40, 60, 80, 10, 5, 3, 15, 90, 95);
            for (int id : new int[] {5, 50, 90, 30, 3}) {
                arvore.remover(id);
            }
            List<Integer> ordem = ids(arvore);
            for (int i = 1; i < ordem.size(); i++) {
                Verifica.verdadeiro(ordem.get(i - 1) < ordem.get(i), "fora de ordem: " + ordem);
            }
            Verifica.igual(8, ordem.size(), "sobraram 8 nós");
        });
    }

    // ---------------- balanceamento e vetor ----------------

    private static void casosDeBalanceamentoEVetor(List<CasoDeTeste> c) {
        adicionar(c, BALANCEAMENTO, "Árvore em escada vira altura mínima", () -> {
            ArvoreBinariaBusca escada = arvoreCom(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
            Verifica.igual(14, escada.altura(), "altura da escada");
            Verifica.verdadeiro(!escada.estaBalanceada(), "a escada não é balanceada");
            escada.reconstruirBalanceada();
            Verifica.igual(3, escada.altura(), "altura depois de balancear 15 nós");
            Verifica.verdadeiro(escada.estaBalanceada(), "deveria estar balanceada");
        });
        adicionar(c, BALANCEAMENTO, "Árvore já balanceada continua válida", () -> {
            ArvoreBinariaBusca arvore = arvoreCom(4, 2, 6, 1, 3, 5, 7);
            Verifica.verdadeiro(arvore.estaBalanceada(), "já era balanceada");
            arvore.reconstruirBalanceada();
            Verifica.verdadeiro(arvore.estaBalanceada(), "continua balanceada");
            Verifica.igual(2, arvore.altura(), "altura");
        });
        adicionar(c, BALANCEAMENTO, "O conjunto de IDs é o mesmo antes e depois", () -> {
            ArvoreBinariaBusca arvore = arvoreCom(50, 30, 70, 20, 40, 60, 80, 10, 5, 3, 15, 90, 95);
            List<Integer> antes = ids(arvore);
            arvore.reconstruirBalanceada();
            Verifica.igual(antes, ids(arvore), "IDs depois de balancear");
        });
        adicionar(c, VETOR, "Tamanho do vetor: árvore balanceada × escada", () -> {
            Verifica.igual(7L, ArvoreBinariaBusca.posicoesNecessarias(arvoreCom(4, 2, 6, 1, 3, 5, 7).altura()),
                    "balanceada com 7 nós");
            Verifica.igual(127L, ArvoreBinariaBusca.posicoesNecessarias(arvoreCom(1, 2, 3, 4, 5, 6, 7).altura()),
                    "escada com 7 nós");
        });
        adicionar(c, VETOR, "Fórmulas de filho esquerdo, filho direito e pai (índice a partir de 1)", () -> {
            Verifica.igual(6, ArvoreBinariaBusca.indiceFilhoEsquerdo(3), "filho esquerdo de 3");
            Verifica.igual(7, ArvoreBinariaBusca.indiceFilhoDireito(3), "filho direito de 3");
            Verifica.igual(3, ArvoreBinariaBusca.indicePai(7), "pai de 7");
            Verifica.igual(3, ArvoreBinariaBusca.indicePai(6), "pai de 6");
        });
    }

    // ---------------- grafo ----------------

    private static void casosDoGrafo(List<CasoDeTeste> c) {
        adicionar(c, GRAFO, "Aresta duplicada, laço, peso inválido e vértice inexistente são rejeitados", () -> {
            Grafo grafo = grafoCom(2);
            grafo.adicionarAresta(new Aresta(1, 2, 5, 5));
            Verifica.lanca(IllegalArgumentException.class, () -> grafo.adicionarAresta(new Aresta(1, 2, 9, 9)),
                    "aresta duplicada");
            Verifica.lanca(IllegalArgumentException.class, () -> new Aresta(1, 1, 5, 5), "laço");
            Verifica.lanca(IllegalArgumentException.class, () -> new Aresta(1, 2, 0, 5), "distância zero");
            Verifica.lanca(IllegalArgumentException.class, () -> new Aresta(1, 2, 5, -1), "tempo negativo");
            Verifica.lanca(IllegalArgumentException.class, () -> grafo.adicionarAresta(new Aresta(1, 9, 5, 5)),
                    "vértice inexistente");
            Verifica.igual(1, grafo.quantidadeArestas(), "só a primeira aresta ficou");
        });
        adicionar(c, GRAFO, "Via de mão dupla cria duas arestas", () -> {
            Grafo grafo = grafoCom(2);
            grafo.adicionarConexaoBidirecional(1, 2, 7.5, 12);
            Verifica.verdadeiro(grafo.existeAresta(1, 2) && grafo.existeAresta(2, 1), "ida e volta");
            Verifica.igual(2, grafo.quantidadeArestas(), "duas arestas");
        });
        adicionar(c, GRAFO, "Remover vértice apaga as arestas que chegam e as que saem", () -> {
            Grafo grafo = grafoCom(3);
            grafo.adicionarConexaoBidirecional(1, 2, 1, 1);
            grafo.adicionarAresta(new Aresta(3, 2, 1, 1));
            grafo.adicionarAresta(new Aresta(1, 3, 1, 1));
            grafo.removerVertice(2);
            Verifica.igual(List.of(new Aresta(1, 3, 1, 1)), grafo.arestas(), "arestas restantes");
        });
        adicionar(c, GRAFO, "Graus de entrada e saída", () -> {
            Grafo grafo = grafoCom(3);
            grafo.adicionarAresta(new Aresta(1, 2, 1, 1));
            grafo.adicionarAresta(new Aresta(1, 3, 1, 1));
            grafo.adicionarAresta(new Aresta(3, 2, 1, 1));
            Verifica.igual(2, grafo.grauSaida(1), "grau de saída do 1");
            Verifica.igual(2, grafo.grauEntrada(2), "grau de entrada do 2");
            Verifica.igual(0, grafo.grauEntrada(1), "grau de entrada do 1");
        });
    }

    // ---------------- algoritmos ----------------

    private static void casosDeAlgoritmos(List<CasoDeTeste> c) {
        adicionar(c, ALGORITMOS, "BFS em grafo desconexo lista os vértices não alcançados", () -> {
            Grafo grafo = grafoCom(4);
            grafo.adicionarAresta(new Aresta(1, 2, 1, 1));
            grafo.adicionarAresta(new Aresta(2, 3, 1, 1));
            ResultadoBusca r = BuscasGrafo.emLargura(grafo, 1);
            Verifica.igual(List.of(1, 2, 3), r.ordemVisita(), "ordem de visita");
            Verifica.igual(List.of(4), r.naoAlcancados(), "não alcançados");
        });
        adicionar(c, ALGORITMOS, "DFS: ciclo é encontrado; sem ciclo não aparece nenhum", () -> {
            Grafo comCiclo = grafoCom(3);
            comCiclo.adicionarAresta(new Aresta(1, 2, 1, 1));
            comCiclo.adicionarAresta(new Aresta(2, 3, 1, 1));
            comCiclo.adicionarAresta(new Aresta(3, 1, 1, 1));
            Verifica.igual(List.of(List.of(1, 2, 3)), Conectividade.ciclos(comCiclo), "ciclo 1-2-3");
            Grafo semCiclo = grafoCom(3);
            semCiclo.adicionarAresta(new Aresta(1, 2, 1, 1));
            semCiclo.adicionarAresta(new Aresta(2, 3, 1, 1));
            semCiclo.adicionarAresta(new Aresta(1, 3, 1, 1));
            Verifica.verdadeiro(Conectividade.ciclos(semCiclo).isEmpty(), "grafo sem ciclo");
        });
        adicionar(c, ALGORITMOS, "Ir e voltar pela mesma via de mão dupla não conta como ciclo", () -> {
            Grafo grafo = grafoCom(3);
            grafo.adicionarConexaoBidirecional(1, 2, 1, 1);
            grafo.adicionarConexaoBidirecional(2, 3, 1, 1);
            Verifica.verdadeiro(Conectividade.ciclos(grafo).isEmpty(), "cadeia de vias de mão dupla");
        });
        adicionar(c, ALGORITMOS, "Dijkstra: destino inalcançável", () -> {
            Grafo grafo = grafoCom(3);
            grafo.adicionarAresta(new Aresta(1, 2, 1, 1));
            Verifica.verdadeiro(!Dijkstra.menorCaminho(grafo, 1, 3, CriterioPeso.TEMPO).existe(), "sem caminho até o 3");
            Verifica.verdadeiro(!Dijkstra.menorCaminho(grafo, 2, 1, CriterioPeso.TEMPO).existe(), "sentido contrário");
        });
        adicionar(c, ALGORITMOS, "Dijkstra: origem igual ao destino", () -> {
            ResultadoCaminho r = Dijkstra.menorCaminho(grafoCom(2), 2, 2, CriterioPeso.DISTANCIA);
            Verifica.verdadeiro(r.existe() && r.quantidadeTrechos() == 0, "caminho de custo zero");
            Verifica.igual(List.of(2), r.caminho(), "caminho");
        });
        adicionar(c, ALGORITMOS, "Dijkstra: caminho com mais arestas e menor custo", () -> {
            Grafo grafo = grafoCom(3);
            grafo.adicionarAresta(new Aresta(1, 3, 10, 10));
            grafo.adicionarAresta(new Aresta(1, 2, 1, 1));
            grafo.adicionarAresta(new Aresta(2, 3, 1, 1));
            Verifica.igual(List.of(1, 2, 3), Dijkstra.menorCaminho(grafo, 1, 3, CriterioPeso.DISTANCIA).caminho(),
                    "menor distância");
            Verifica.igual(List.of(1, 3), BuscasGrafo.menosArestas(grafo, 1, 3).caminho(), "menos arestas (BFS)");
        });
        adicionar(c, ALGORITMOS, "Dados de teste: BFS, menor km e menor tempo divergem no par 20 → 70", () -> {
            ComparacaoRotas comparacao = sistemaDeTeste().compararRotas(20, 70);
            Verifica.igual(3, comparacao.caminhosDistintos(), "caminhos distintos");
        });
        adicionar(c, ALGORITMOS, "Dados de teste: três componentes conexas e rede não conexa", () -> {
            SistemaCidades sistema = sistemaDeTeste();
            Verifica.igual(3, sistema.componentesConexas().size(), "componentes");
            Verifica.verdadeiro(!sistema.redeConexa(), "a rede de teste não é conexa");
        });
    }

    // ---------------- integração ----------------

    private static void casosDeIntegracao(List<CasoDeTeste> c) {
        adicionar(c, INTEGRACAO, "Cadastrar, conectar e consultar", () -> {
            SistemaCidades sistema = new SistemaCidades();
            sistema.cadastrarLocalidade(1, "A", TipoLocalidade.CIDADE, 10);
            sistema.cadastrarLocalidade(2, "B", TipoLocalidade.BAIRRO, 20);
            sistema.conectar(1, 2, 5.5, 9, true);
            Verifica.verdadeiro(sistema.existeConexao(1, 2) && sistema.existeConexao(2, 1), "via de mão dupla");
            Verifica.igual(2, sistema.quantidadeConexoes(1), "conexões do 1 (entrada + saída)");
            Verifica.consistente(sistema);
        });
        adicionar(c, INTEGRACAO, "Não é possível conectar localidade inexistente nem cadastrar ID repetido", () -> {
            SistemaCidades sistema = new SistemaCidades();
            sistema.cadastrarLocalidade(1, "A", TipoLocalidade.CIDADE, 10);
            Verifica.lanca(RegraNegocioException.class, () -> sistema.conectar(1, 99, 1, 1, true), "destino inexistente");
            Verifica.lanca(RegraNegocioException.class,
                    () -> sistema.cadastrarLocalidade(1, "B", TipoLocalidade.CIDADE, 1), "ID repetido");
            Verifica.igual(0, sistema.getGrafo().quantidadeArestas(), "nenhuma aresta foi criada");
            Verifica.consistente(sistema);
        });
        adicionar(c, INTEGRACAO, "Atualizar o nome de uma localidade aparece nas rotas", () -> {
            SistemaCidades sistema = sistemaDeTeste();
            ResultadoCaminho rota = sistema.menorRota(20, 70, CriterioPeso.DISTANCIA);
            Verifica.verdadeiro(rota.caminho().contains(10), "a rota passa pelo 10");
            sistema.atualizarLocalidade(10, "Hospital Novo", TipoLocalidade.INSTITUICAO, 2400);
            Verifica.igual("Hospital Novo", sistema.nomeDe(10), "nome mostrado nas rotas");
            Verifica.igual(rota.caminho(), sistema.menorRota(20, 70, CriterioPeso.DISTANCIA).caminho(),
                    "a rota continua a mesma");
            Verifica.consistente(sistema);
        });
        adicionar(c, INTEGRACAO, "Remover localidade apaga as conexões e mantém a consistência", () -> {
            SistemaCidades sistema = sistemaDeTeste();
            int conexoes = sistema.quantidadeConexoes(50);
            Verifica.igual(conexoes, sistema.removerLocalidade(50), "conexões removidas junto");
            Verifica.verdadeiro(!sistema.getGrafo().contemVertice(50), "vértice removido");
            Verifica.consistente(sistema);
        });
        adicionar(c, INTEGRACAO, "Balancear a árvore não altera o grafo", () -> {
            SistemaCidades sistema = sistemaDeTeste();
            List<Aresta> antes = sistema.getGrafo().arestas();
            ResultadoBalanceamento resultado = sistema.balancearArvore();
            Verifica.verdadeiro(!resultado.estavaBalanceada(), "a árvore de teste nasce desbalanceada");
            Verifica.igual(3, resultado.alturaDepois(), "altura depois de balancear");
            Verifica.igual(antes, sistema.getGrafo().arestas(), "arestas do grafo");
            Verifica.consistente(sistema);
        });
        adicionar(c, INTEGRACAO, "O verificador de consistência detecta um sistema quebrado de propósito", () -> {
            ArvoreBinariaBusca arvore = arvoreCom(1, 2);
            Grafo grafo = grafoCom(0);
            grafo.adicionarVertice(2);
            grafo.adicionarVertice(3);
            SistemaCidades quebrado = new SistemaCidades(arvore, grafo);
            Verifica.igual(2, quebrado.verificarConsistencia().size(), "problemas encontrados");
        });
    }
}