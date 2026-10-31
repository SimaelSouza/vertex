package br.ifg.vertex.arvore;

import static br.ifg.vertex.arvore.ArvoresParaTeste.arvoreDeTeste;
import static br.ifg.vertex.arvore.ArvoresParaTeste.ids;
import static br.ifg.vertex.arvore.ArvoresParaTeste.loc;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** Testes dos quatro percursos, usando a árvore de 13 nós (veja ArvoresParaTeste). */
class ArvoreBinariaBuscaPercursosTest {

    @Test
    void preOrdemVisitaONoAntesDosFilhos() {
        assertEquals(List.of(50, 30, 20, 10, 5, 3, 15, 40, 70, 60, 80, 90, 95),
                ids(arvoreDeTeste().preOrdem()));
    }

    @Test
    void emOrdemSaiEmOrdemCrescente() {
        assertEquals(List.of(3, 5, 10, 15, 20, 30, 40, 50, 60, 70, 80, 90, 95),
                ids(arvoreDeTeste().emOrdem()));
    }

    @Test
    void posOrdemVisitaONoDepoisDosFilhos() {
        assertEquals(List.of(3, 5, 15, 10, 20, 40, 30, 60, 95, 90, 80, 70, 50),
                ids(arvoreDeTeste().posOrdem()));
    }

    @Test
    void porNiveisVisitaNivelPorNivelDaEsquerdaParaADireita() {
        assertEquals(List.of(50, 30, 70, 20, 40, 60, 80, 10, 90, 5, 15, 95, 3),
                ids(arvoreDeTeste().porNiveis()));
    }

    @Test
    void percursosDeArvoreVaziaDevolvemListaVazia() {
        ArvoreBinariaBusca vazia = new ArvoreBinariaBusca();
        assertTrue(vazia.preOrdem().isEmpty());
        assertTrue(vazia.emOrdem().isEmpty());
        assertTrue(vazia.posOrdem().isEmpty());
        assertTrue(vazia.porNiveis().isEmpty());
    }

    @Test
    void percursosDeArvoreComUmNoTemSoEleMesmo() {
        ArvoreBinariaBusca arvore = new ArvoreBinariaBusca();
        arvore.inserir(loc(7));
        assertEquals(List.of(7), ids(arvore.preOrdem()));
        assertEquals(List.of(7), ids(arvore.emOrdem()));
        assertEquals(List.of(7), ids(arvore.posOrdem()));
        assertEquals(List.of(7), ids(arvore.porNiveis()));
    }

    @Test
    void todosOsPercursosVisitamOsMesmosNosUmaVezCada() {
        ArvoreBinariaBusca arvore = arvoreDeTeste();
        List<Integer> esperado = ids(arvore.emOrdem());
        for (List<Integer> percurso : List.of(
                ids(arvore.preOrdem()), ids(arvore.posOrdem()), ids(arvore.porNiveis()))) {
            List<Integer> ordenado = new ArrayList<>(percurso);
            Collections.sort(ordenado);
            assertEquals(esperado, ordenado);
        }
    }

    @Test
    void emOrdemSaiCrescenteEmArvoreAleatoriaGrande() {
        List<Integer> sorteados = new ArrayList<>();
        for (int i = 1; i <= 300; i++) {
            sorteados.add(i * 3); // IDs distintos
        }
        Collections.shuffle(sorteados, new Random(42));

        ArvoreBinariaBusca arvore = new ArvoreBinariaBusca();
        for (int id : sorteados) {
            arvore.inserir(loc(id));
        }

        List<Integer> resultado = ids(arvore.emOrdem());
        assertEquals(300, resultado.size());
        for (int i = 1; i < resultado.size(); i++) {
            assertTrue(resultado.get(i - 1) < resultado.get(i), "fora de ordem na posição " + i);
        }
    }
}