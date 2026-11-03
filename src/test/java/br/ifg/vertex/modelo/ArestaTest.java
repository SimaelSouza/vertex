package br.ifg.vertex.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ArestaTest {

    @Test
    void criaArestaValida() {
        Aresta a = new Aresta(50, 30, 12.5, 20);
        assertEquals(50, a.origem());
        assertEquals(30, a.destino());
        assertEquals(12.5, a.distanciaKm());
        assertEquals(20, a.tempoMin());
    }

    @Test
    void rejeitaLacoEIdsNaoPositivos() {
        assertThrows(IllegalArgumentException.class, () -> new Aresta(5, 5, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new Aresta(0, 5, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new Aresta(5, -1, 1, 1));
    }

    @Test
    void rejeitaDistanciaInvalida() {
        assertThrows(IllegalArgumentException.class, () -> new Aresta(1, 2, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new Aresta(1, 2, -3.5, 1));
        assertThrows(IllegalArgumentException.class, () -> new Aresta(1, 2, Double.NaN, 1));
        assertThrows(IllegalArgumentException.class, () -> new Aresta(1, 2, Double.POSITIVE_INFINITY, 1));
    }

    @Test
    void rejeitaTempoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new Aresta(1, 2, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new Aresta(1, 2, 1, -10));
    }

    @Test
    void inversaTrocaOrigemEDestinoEMantemOsPesos() {
        Aresta volta = new Aresta(1, 2, 7.5, 12).inversa();
        assertEquals(new Aresta(2, 1, 7.5, 12), volta);
    }

    @Test
    void comPesosTrocaSoOsPesosEValidaOsNovos() {
        Aresta a = new Aresta(1, 2, 7.5, 12);
        assertEquals(new Aresta(1, 2, 9, 15), a.comPesos(9, 15));
        assertThrows(IllegalArgumentException.class, () -> a.comPesos(-1, 15));
        assertEquals(new Aresta(1, 2, 7.5, 12), a); // a original não muda (é imutável)
    }

    @Test
    void igualdadeConsideraTodosOsCampos() {
        assertEquals(new Aresta(1, 2, 3, 4), new Aresta(1, 2, 3, 4));
        assertNotEquals(new Aresta(1, 2, 3, 4), new Aresta(1, 2, 3, 5));
    }
}