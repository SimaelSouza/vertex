package br.ifg.vertex.grafo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.ifg.vertex.modelo.Aresta;
import java.util.List;
import org.junit.jupiter.api.Test;

class DijkstraTest {

    private static Grafo grafoCom(int quantidade) {
        Grafo grafo = new Grafo();
        for (int id = 1; id <= quantidade; id++) {
            grafo.adicionarVertice(id);
        }
        return grafo;
    }

    /**
     * 1->4 direto: 100 km, 100 min (1 aresta).
     * 1->2->3->4: 10+10+10 = 30 km mas 60+60+60 = 180 min (3 arestas, estrada ruim).
     * 1->5->4: 40+40 = 80 km, 20+20 = 40 min (2 arestas, rodovia).
     */
    private static Grafo tresCaminhos() {
        Grafo grafo = grafoCom(5);
        grafo.adicionarAresta(new Aresta(1, 4, 100, 100));
        grafo.adicionarAresta(new Aresta(1, 2, 10, 60));
        grafo.adicionarAresta(new Aresta(2, 3, 10, 60));
        grafo.adicionarAresta(new Aresta(3, 4, 10, 60));
        grafo.adicionarAresta(new Aresta(1, 5, 40, 20));
        grafo.adicionarAresta(new Aresta(5, 4, 40, 20));
        return grafo;
    }

    @Test
    void menorDistanciaPodeTerMaisArestasQueAsOutras() {
        ResultadoCaminho r = Dijkstra.menorCaminho(tresCaminhos(), 1, 4, CriterioPeso.DISTANCIA);

        assertTrue(r.existe());
        assertEquals(List.of(1, 2, 3, 4), r.caminho());
        assertEquals(30.0, r.distanciaKm(), 1e-9);
        assertEquals(180, r.tempoMin());
        assertEquals(3, r.quantidadeTrechos());
    }

    @Test
    void menorTempoEscolheOutroCaminhoQueAMenorDistancia() {
        ResultadoCaminho r = Dijkstra.menorCaminho(tresCaminhos(), 1, 4, CriterioPeso.TEMPO);

        assertEquals(List.of(1, 5, 4), r.caminho());
        assertEquals(80.0, r.distanciaKm(), 1e-9);
        assertEquals(40, r.tempoMin());
        assertEquals(40.0, r.custo(CriterioPeso.TEMPO), 1e-9);
        assertEquals(80.0, r.custo(CriterioPeso.DISTANCIA), 1e-9);
    }

    @Test
    void trechosListamAsArestasNaOrdemDoCaminho() {
        ResultadoCaminho r = Dijkstra.menorCaminho(tresCaminhos(), 1, 4, CriterioPeso.TEMPO);
        assertEquals(List.of(new Aresta(1, 5, 40, 20), new Aresta(5, 4, 40, 20)), r.trechos());
    }

    @Test
    void destinoInalcancavelDevolveResultadoSemCaminho() {
        Grafo grafo = grafoCom(3);
        grafo.adicionarAresta(new Aresta(1, 2, 5, 5)); // 3 está isolado

        ResultadoCaminho r = Dijkstra.menorCaminho(grafo, 1, 3, CriterioPeso.DISTANCIA);

        assertFalse(r.existe());
        assertTrue(r.caminho().isEmpty());
        assertEquals(0, r.quantidadeTrechos());
    }

    @Test
    void respeitaOSentidoDasArestas() {
        Grafo grafo = grafoCom(2);
        grafo.adicionarAresta(new Aresta(1, 2, 5, 5)); // só ida

        assertTrue(Dijkstra.menorCaminho(grafo, 1, 2, CriterioPeso.TEMPO).existe());
        assertFalse(Dijkstra.menorCaminho(grafo, 2, 1, CriterioPeso.TEMPO).existe());
    }

    @Test
    void origemIgualAoDestinoTemCustoZero() {
        ResultadoCaminho r = Dijkstra.menorCaminho(tresCaminhos(), 2, 2, CriterioPeso.DISTANCIA);

        assertTrue(r.existe());
        assertEquals(List.of(2), r.caminho());
        assertEquals(0, r.quantidadeTrechos());
        assertEquals(0.0, r.distanciaKm(), 0);
        assertEquals(0, r.tempoMin());
    }

    @Test
    void vaiPorCaminhoMaisLongoEmArestasQuandoECompensa() {
        // 1->3 direto custa 10; 1->2->3 custa 1+1
        Grafo grafo = grafoCom(3);
        grafo.adicionarAresta(new Aresta(1, 3, 10, 10));
        grafo.adicionarAresta(new Aresta(1, 2, 1, 1));
        grafo.adicionarAresta(new Aresta(2, 3, 1, 1));

        assertEquals(List.of(1, 2, 3), Dijkstra.menorCaminho(grafo, 1, 3, CriterioPeso.DISTANCIA).caminho());
    }

    @Test
    void somaPesosDecimaisCorretamente() {
        Grafo grafo = grafoCom(3);
        grafo.adicionarAresta(new Aresta(1, 2, 12.5, 20));
        grafo.adicionarAresta(new Aresta(2, 3, 9.25, 15));

        ResultadoCaminho r = Dijkstra.menorCaminho(grafo, 1, 3, CriterioPeso.DISTANCIA);

        assertEquals(21.75, r.distanciaKm(), 1e-9);
        assertEquals(35, r.tempoMin());
    }

    @Test
    void empateEmCustoDaSempreOMesmoCaminho() {
        // dois caminhos de custo 2: via 2 e via 3 (vence o encontrado primeiro, o do menor ID)
        Grafo grafo = grafoCom(4);
        grafo.adicionarAresta(new Aresta(1, 2, 1, 1));
        grafo.adicionarAresta(new Aresta(1, 3, 1, 1));
        grafo.adicionarAresta(new Aresta(2, 4, 1, 1));
        grafo.adicionarAresta(new Aresta(3, 4, 1, 1));

        for (int i = 0; i < 3; i++) {
            assertEquals(List.of(1, 2, 4), Dijkstra.menorCaminho(grafo, 1, 4, CriterioPeso.DISTANCIA).caminho());
        }
    }

    @Test
    void funcionaComCiclosNoGrafo() {
        Grafo grafo = grafoCom(3);
        grafo.adicionarAresta(new Aresta(1, 2, 1, 1));
        grafo.adicionarAresta(new Aresta(2, 3, 1, 1));
        grafo.adicionarAresta(new Aresta(3, 1, 1, 1));

        assertEquals(List.of(1, 2, 3), Dijkstra.menorCaminho(grafo, 1, 3, CriterioPeso.TEMPO).caminho());
        assertEquals(List.of(2, 3, 1), Dijkstra.menorCaminho(grafo, 2, 1, CriterioPeso.TEMPO).caminho());
    }

    @Test
    void verticeInexistenteLancaErro() {
        Grafo grafo = tresCaminhos();
        assertThrows(IllegalArgumentException.class, () -> Dijkstra.menorCaminho(grafo, 99, 1, CriterioPeso.TEMPO));
        assertThrows(IllegalArgumentException.class, () -> Dijkstra.menorCaminho(grafo, 1, 99, CriterioPeso.TEMPO));
    }

    @Test
    void naoAlteraOGrafo() {
        Grafo grafo = tresCaminhos();
        List<Aresta> antes = grafo.arestas();
        Dijkstra.menorCaminho(grafo, 1, 4, CriterioPeso.DISTANCIA);
        Dijkstra.menorCaminho(grafo, 1, 4, CriterioPeso.TEMPO);
        assertEquals(antes, grafo.arestas());
    }

    @Test
    void menosArestasDaBfsIgnoraKmEMinutos() {
        ResultadoCaminho r = BuscasGrafo.menosArestas(tresCaminhos(), 1, 4);

        assertEquals(List.of(1, 4), r.caminho()); // 1 aresta, mesmo sendo a mais longa e lenta
        assertEquals(100.0, r.distanciaKm(), 1e-9);
        assertEquals(100, r.tempoMin());
    }

    @Test
    void menosArestasInalcancavelOuOrigemIgualDestino() {
        Grafo grafo = grafoCom(3);
        grafo.adicionarAresta(new Aresta(1, 2, 5, 5));

        assertFalse(BuscasGrafo.menosArestas(grafo, 1, 3).existe());
        ResultadoCaminho mesmo = BuscasGrafo.menosArestas(grafo, 2, 2);
        assertTrue(mesmo.existe());
        assertEquals(List.of(2), mesmo.caminho());
        assertThrows(IllegalArgumentException.class, () -> BuscasGrafo.menosArestas(grafo, 1, 99));
    }

    @Test
    void criterioPesoLeOCampoCerto() {
        Aresta aresta = new Aresta(1, 2, 7.5, 12);
        assertEquals(7.5, CriterioPeso.DISTANCIA.pesoDe(aresta), 0);
        assertEquals(12.0, CriterioPeso.TEMPO.pesoDe(aresta), 0);
        assertEquals("km", CriterioPeso.DISTANCIA.getUnidade());
        assertEquals("min", CriterioPeso.TEMPO.getUnidade());
    }
}