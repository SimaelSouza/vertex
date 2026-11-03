package br.ifg.vertex.grafo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.ifg.vertex.modelo.Aresta;
import java.util.List;
import org.junit.jupiter.api.Test;

class GrafoTest {

    private static Grafo grafoCom(int... ids) {
        Grafo grafo = new Grafo();
        for (int id : ids) {
            grafo.adicionarVertice(id);
        }
        return grafo;
    }

    // ---------------- vértices ----------------

    @Test
    void adicionarVerticeRejeitaRepetidoEIdInvalido() {
        Grafo grafo = new Grafo();
        assertTrue(grafo.adicionarVertice(10));
        assertFalse(grafo.adicionarVertice(10));
        assertEquals(1, grafo.quantidadeVertices());
        assertThrows(IllegalArgumentException.class, () -> grafo.adicionarVertice(0));
        assertThrows(IllegalArgumentException.class, () -> grafo.adicionarVertice(-3));
    }

    @Test
    void verticesSaemEmOrdemCrescente() {
        assertEquals(List.of(10, 20, 30), grafoCom(30, 10, 20).vertices());
    }

    @Test
    void grafoVazioNaoTemNada() {
        Grafo grafo = new Grafo();
        assertEquals(0, grafo.quantidadeVertices());
        assertEquals(0, grafo.quantidadeArestas());
        assertTrue(grafo.vertices().isEmpty());
        assertTrue(grafo.arestas().isEmpty());
        assertTrue(grafo.verticesIsolados().isEmpty());
        assertFalse(grafo.contemVertice(1));
    }

    // ---------------- arestas: criação ----------------

    @Test
    void arestaDeMaoUnicaCriaSoUmaDirecao() {
        Grafo grafo = grafoCom(1, 2);
        grafo.adicionarAresta(new Aresta(1, 2, 5.5, 10));

        assertTrue(grafo.existeAresta(1, 2));
        assertFalse(grafo.existeAresta(2, 1)); // adjacência é direcional
        assertEquals(1, grafo.quantidadeArestas());
    }

    @Test
    void listaDeSaidaFicaOrdenadaPorIdDeDestino() {
        Grafo grafo = grafoCom(1, 2, 3, 4, 5);
        grafo.adicionarAresta(new Aresta(1, 5, 1, 1));
        grafo.adicionarAresta(new Aresta(1, 3, 1, 1));
        grafo.adicionarAresta(new Aresta(1, 2, 1, 1));
        grafo.adicionarAresta(new Aresta(1, 4, 1, 1));

        assertEquals(List.of(2, 3, 4, 5), grafo.vizinhos(1));
    }

    @Test
    void rejeitaArestaComVerticeInexistente() {
        Grafo grafo = grafoCom(1);
        assertThrows(IllegalArgumentException.class, () -> grafo.adicionarAresta(new Aresta(1, 2, 1, 1)));
        assertThrows(IllegalArgumentException.class, () -> grafo.adicionarAresta(new Aresta(2, 1, 1, 1)));
        assertEquals(0, grafo.quantidadeArestas());
    }

    @Test
    void rejeitaArestaDuplicadaMesmoComPesosDiferentes() {
        Grafo grafo = grafoCom(1, 2);
        grafo.adicionarAresta(new Aresta(1, 2, 5, 10));

        assertThrows(IllegalArgumentException.class, () -> grafo.adicionarAresta(new Aresta(1, 2, 99, 99)));
        assertEquals(1, grafo.quantidadeArestas());
        assertEquals(5.0, grafo.obterAresta(1, 2).get().distanciaKm()); // a original ficou
    }

    @Test
    void conexaoBidirecionalCriaDuasArestasComPesosIguais() {
        Grafo grafo = grafoCom(1, 2);
        grafo.adicionarConexaoBidirecional(1, 2, 7.5, 12);

        assertEquals(List.of(new Aresta(1, 2, 7.5, 12), new Aresta(2, 1, 7.5, 12)), grafo.arestas());
    }

    @Test
    void conexaoBidirecionalNaoDeixaMetadeCriadaSeAVoltaJaExiste() {
        Grafo grafo = grafoCom(1, 2);
        grafo.adicionarAresta(new Aresta(2, 1, 3, 3)); // só a volta existe

        assertThrows(IllegalArgumentException.class, () -> grafo.adicionarConexaoBidirecional(1, 2, 5, 5));
        assertEquals(1, grafo.quantidadeArestas());
        assertFalse(grafo.existeAresta(1, 2));
    }

    @Test
    void conexaoBidirecionalInvalidaNaoCriaNada() {
        Grafo grafo = grafoCom(1, 2);
        assertThrows(IllegalArgumentException.class, () -> grafo.adicionarConexaoBidirecional(1, 2, -5, 5)); // peso
        assertThrows(IllegalArgumentException.class, () -> grafo.adicionarConexaoBidirecional(1, 1, 5, 5));   // laço
        assertThrows(IllegalArgumentException.class, () -> grafo.adicionarConexaoBidirecional(1, 9, 5, 5));   // inexistente
        assertEquals(0, grafo.quantidadeArestas());
    }

    // ---------------- arestas: remoção e alteração ----------------

    @Test
    void removerArestaRemoveSoUmaDirecao() {
        Grafo grafo = grafoCom(1, 2);
        grafo.adicionarConexaoBidirecional(1, 2, 4, 6);

        assertTrue(grafo.removerAresta(1, 2));
        assertFalse(grafo.existeAresta(1, 2));
        assertTrue(grafo.existeAresta(2, 1)); // a volta fica
        assertFalse(grafo.removerAresta(1, 2));  // já tinha sido removida
        assertFalse(grafo.removerAresta(9, 1));  // origem inexistente
    }

    @Test
    void alterarPesosTrocaOsValoresDaAresta() {
        Grafo grafo = grafoCom(1, 2);
        grafo.adicionarAresta(new Aresta(1, 2, 5, 10));

        assertTrue(grafo.alterarPesos(1, 2, 8.5, 14));
        assertEquals(new Aresta(1, 2, 8.5, 14), grafo.obterAresta(1, 2).get());
        assertFalse(grafo.alterarPesos(2, 1, 1, 1)); // não existe a volta
        assertFalse(grafo.alterarPesos(9, 1, 1, 1));
    }

    @Test
    void alterarPesosInvalidosLancaErroENaoMudaNada() {
        Grafo grafo = grafoCom(1, 2);
        grafo.adicionarAresta(new Aresta(1, 2, 5, 10));

        assertThrows(IllegalArgumentException.class, () -> grafo.alterarPesos(1, 2, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> grafo.alterarPesos(1, 2, 5, -1));
        assertEquals(new Aresta(1, 2, 5, 10), grafo.obterAresta(1, 2).get());
    }

    // ---------------- remoção de vértice ----------------

    @Test
    void removerVerticeApagaAsArestasQueSaemEAsQueChegam() {
        Grafo grafo = grafoCom(1, 2, 3);
        grafo.adicionarConexaoBidirecional(1, 2, 1, 1); // 1<->2
        grafo.adicionarAresta(new Aresta(3, 2, 1, 1));  // 3->2 (chega no 2)
        grafo.adicionarAresta(new Aresta(2, 3, 1, 1));  // 2->3 (sai do 2)
        grafo.adicionarAresta(new Aresta(1, 3, 1, 1));  // não toca o 2

        assertTrue(grafo.removerVertice(2));

        assertFalse(grafo.contemVertice(2));
        assertEquals(List.of(new Aresta(1, 3, 1, 1)), grafo.arestas());
        assertEquals(List.of(1, 3), grafo.vertices());
        assertFalse(grafo.removerVertice(2)); // já não existe
    }

    // ---------------- vizinhança e graus ----------------

    @Test
    void grausDeEntradaESaida() {
        Grafo grafo = grafoCom(1, 2, 3);
        grafo.adicionarAresta(new Aresta(1, 2, 1, 1));
        grafo.adicionarAresta(new Aresta(1, 3, 1, 1));
        grafo.adicionarAresta(new Aresta(3, 2, 1, 1));

        assertEquals(2, grafo.grauSaida(1));
        assertEquals(0, grafo.grauEntrada(1));
        assertEquals(0, grafo.grauSaida(2));
        assertEquals(2, grafo.grauEntrada(2));
        assertEquals(1, grafo.grauSaida(3));
        assertEquals(1, grafo.grauEntrada(3));
    }

    @Test
    void verticesIsoladosNaoTemArestaNemDeEntradaNemDeSaida() {
        Grafo grafo = grafoCom(1, 2, 3, 4);
        grafo.adicionarAresta(new Aresta(1, 2, 1, 1)); // 2 só recebe, 1 só envia: nenhum é isolado

        assertEquals(List.of(3, 4), grafo.verticesIsolados());
    }

    @Test
    void arestasSaemOrdenadasPorOrigemEDestino() {
        Grafo grafo = grafoCom(1, 2, 3);
        grafo.adicionarAresta(new Aresta(3, 1, 1, 1));
        grafo.adicionarAresta(new Aresta(1, 3, 1, 1));
        grafo.adicionarAresta(new Aresta(1, 2, 1, 1));
        grafo.adicionarAresta(new Aresta(2, 1, 1, 1));

        assertEquals(List.of(
                new Aresta(1, 2, 1, 1), new Aresta(1, 3, 1, 1),
                new Aresta(2, 1, 1, 1), new Aresta(3, 1, 1, 1)), grafo.arestas());
    }

    @Test
    void arestasDeSaidaDevolveUmaCopia() {
        Grafo grafo = grafoCom(1, 2);
        grafo.adicionarAresta(new Aresta(1, 2, 1, 1));

        grafo.arestasDeSaida(1).clear(); // mexer na cópia não pode afetar o grafo

        assertEquals(1, grafo.quantidadeArestas());
    }

    @Test
    void consultasDeVerticeInexistenteLancamErro() {
        Grafo grafo = grafoCom(1);
        assertThrows(IllegalArgumentException.class, () -> grafo.vizinhos(9));
        assertThrows(IllegalArgumentException.class, () -> grafo.arestasDeSaida(9));
        assertThrows(IllegalArgumentException.class, () -> grafo.grauEntrada(9));
        assertThrows(IllegalArgumentException.class, () -> grafo.grauSaida(9));
        assertFalse(grafo.existeAresta(9, 1)); // perguntar "existe?" nunca lança
    }
}