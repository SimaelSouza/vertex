package br.ifg.vertex.grafo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.ifg.vertex.modelo.Aresta;
import java.util.List;
import org.junit.jupiter.api.Test;

class BuscasGrafoTest {

    /**
     * Grafo dirigido de teste:  1->2, 2->3, 3->4, 1->4, 6->1; o vértice 5 está isolado.
     * Da origem 1 há dois caminhos até o 4: o curto (1,4) e o longo (1,2,3,4).
     */
    private static Grafo grafoDeTeste() {
        Grafo grafo = new Grafo();
        for (int id = 1; id <= 6; id++) {
            grafo.adicionarVertice(id);
        }
        grafo.adicionarAresta(new Aresta(1, 2, 1, 1));
        grafo.adicionarAresta(new Aresta(2, 3, 1, 1));
        grafo.adicionarAresta(new Aresta(3, 4, 1, 1));
        grafo.adicionarAresta(new Aresta(1, 4, 1, 1));
        grafo.adicionarAresta(new Aresta(6, 1, 1, 1));
        return grafo;
    }

    // ---------------- BFS ----------------

    @Test
    void larguraVisitaPorOndas() {
        ResultadoBusca r = BuscasGrafo.emLargura(grafoDeTeste(), 1);

        assertEquals(List.of(1, 2, 4, 3), r.ordemVisita());
        assertEquals(0, r.profundidade().get(1).intValue());
        assertEquals(1, r.profundidade().get(2).intValue());
        assertEquals(1, r.profundidade().get(4).intValue());
        assertEquals(2, r.profundidade().get(3).intValue());
        assertEquals(2, r.pai().get(3).intValue());
        assertFalse(r.pai().containsKey(1)); // a origem não tem pai
    }

    @Test
    void larguraAchaOCaminhoComMenosTrechos() {
        ResultadoBusca r = BuscasGrafo.emLargura(grafoDeTeste(), 1);
        assertEquals(List.of(1, 4), r.caminhoAte(4));
        assertEquals(List.of(1, 2, 3), r.caminhoAte(3));
        assertEquals(List.of(1), r.caminhoAte(1));
    }

    @Test
    void larguraRespeitaOSentidoDasArestasEListaOsNaoAlcancados() {
        Grafo grafo = grafoDeTeste();

        ResultadoBusca de1 = BuscasGrafo.emLargura(grafo, 1);
        assertEquals(List.of(5, 6), de1.naoAlcancados()); // 6 aponta para 1, mas 1 não chega no 6
        assertFalse(de1.alcancou(6));
        assertEquals(List.of(), de1.caminhoAte(6));

        ResultadoBusca de4 = BuscasGrafo.emLargura(grafo, 4); // o 4 não tem saída
        assertEquals(List.of(4), de4.ordemVisita());
        assertEquals(5, de4.naoAlcancados().size());
    }

    @Test
    void larguraVisitaVizinhosEmOrdemCrescenteDeId() {
        Grafo grafo = new Grafo();
        for (int id = 1; id <= 5; id++) {
            grafo.adicionarVertice(id);
        }
        grafo.adicionarAresta(new Aresta(1, 5, 1, 1));
        grafo.adicionarAresta(new Aresta(1, 3, 1, 1));
        grafo.adicionarAresta(new Aresta(1, 4, 1, 1));

        assertEquals(List.of(1, 3, 4, 5), BuscasGrafo.emLargura(grafo, 1).ordemVisita());
    }

    // ---------------- DFS ----------------

    @Test
    void profundidadeVaiFundoAntesDeVoltar() {
        ResultadoBusca r = BuscasGrafo.emProfundidade(grafoDeTeste(), 1);

        assertEquals(List.of(1, 2, 3, 4), r.ordemVisita());
        assertEquals(3, r.profundidade().get(4).intValue());
        assertEquals(3, r.pai().get(4).intValue());
        assertEquals(List.of(5, 6), r.naoAlcancados());
    }

    @Test
    void profundidadeNaoGaranteOCaminhoMaisCurto() {
        ResultadoBusca r = BuscasGrafo.emProfundidade(grafoDeTeste(), 1);
        assertEquals(List.of(1, 2, 3, 4), r.caminhoAte(4)); // existe (1,4), mas a DFS achou o longo
    }

    @Test
    void profundidadeVoltaParaExplorarOutrosRamos() {
        // 1->2, 1->3, 2->4: ao chegar no 4 volta ao 1 para visitar o 3
        Grafo grafo = new Grafo();
        for (int id = 1; id <= 4; id++) {
            grafo.adicionarVertice(id);
        }
        grafo.adicionarAresta(new Aresta(1, 2, 1, 1));
        grafo.adicionarAresta(new Aresta(1, 3, 1, 1));
        grafo.adicionarAresta(new Aresta(2, 4, 1, 1));

        ResultadoBusca r = BuscasGrafo.emProfundidade(grafo, 1);

        assertEquals(List.of(1, 2, 4, 3), r.ordemVisita());
        assertEquals(1, r.pai().get(3).intValue());
        assertEquals(1, r.profundidade().get(3).intValue());
    }

    @Test
    void profundidadeNaoTravaEmCadeiaMuitoLonga() {
        Grafo grafo = new Grafo();
        int n = 20_000; // uma versão recursiva estouraria a pilha de chamadas
        for (int id = 1; id <= n; id++) {
            grafo.adicionarVertice(id);
        }
        for (int id = 1; id < n; id++) {
            grafo.adicionarAresta(new Aresta(id, id + 1, 1, 1));
        }

        ResultadoBusca r = BuscasGrafo.emProfundidade(grafo, 1);

        assertEquals(n, r.quantidadeAlcancados());
        assertEquals(n - 1, r.profundidade().get(n).intValue());
    }

    // ---------------- comum às duas ----------------

    @Test
    void ciclosNaoCausamRepeticaoNemLoopInfinito() {
        Grafo grafo = new Grafo();
        for (int id = 1; id <= 3; id++) {
            grafo.adicionarVertice(id);
        }
        grafo.adicionarAresta(new Aresta(1, 2, 1, 1));
        grafo.adicionarAresta(new Aresta(2, 3, 1, 1));
        grafo.adicionarAresta(new Aresta(3, 1, 1, 1));

        assertEquals(List.of(1, 2, 3), BuscasGrafo.emLargura(grafo, 1).ordemVisita());
        assertEquals(List.of(1, 2, 3), BuscasGrafo.emProfundidade(grafo, 1).ordemVisita());
    }

    @Test
    void viaDeMaoDuplaPodeSerPercorridaNosDoisSentidos() {
        Grafo grafo = new Grafo();
        grafo.adicionarVertice(1);
        grafo.adicionarVertice(2);
        grafo.adicionarConexaoBidirecional(1, 2, 5, 5);

        assertEquals(List.of(2, 1), BuscasGrafo.emLargura(grafo, 2).ordemVisita());
        assertEquals(List.of(1, 2), BuscasGrafo.emProfundidade(grafo, 1).ordemVisita());
    }

    @Test
    void verticeIsoladoSoVisitaASiMesmo() {
        ResultadoBusca r = BuscasGrafo.emLargura(grafoDeTeste(), 5);
        assertEquals(List.of(5), r.ordemVisita());
        assertEquals(1, r.quantidadeAlcancados());
        assertTrue(r.alcancou(5));
    }

    @Test
    void origemInexistenteLancaErro() {
        assertThrows(IllegalArgumentException.class, () -> BuscasGrafo.emLargura(grafoDeTeste(), 99));
        assertThrows(IllegalArgumentException.class, () -> BuscasGrafo.emProfundidade(grafoDeTeste(), 99));
    }

    @Test
    void resultadoNaoPodeSerAlteradoPorFora() {
        ResultadoBusca r = BuscasGrafo.emLargura(grafoDeTeste(), 1);
        assertThrows(UnsupportedOperationException.class, () -> r.ordemVisita().add(99));
        assertThrows(UnsupportedOperationException.class, () -> r.pai().put(99, 1));
    }

    @Test
    void buscaNaoAlteraOGrafo() {
        Grafo grafo = grafoDeTeste();
        List<Aresta> antes = grafo.arestas();
        BuscasGrafo.emLargura(grafo, 1);
        BuscasGrafo.emProfundidade(grafo, 1);
        assertEquals(antes, grafo.arestas());
    }
}