package br.ifg.vertex.arvore;

import static br.ifg.vertex.arvore.ArvoresParaTeste.arvoreCom;
import static br.ifg.vertex.arvore.ArvoresParaTeste.arvoreDeTeste;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.ifg.vertex.modelo.Localidade;
import org.junit.jupiter.api.Test;

/** Testes do vetor por níveis (índice 1) e do estudo comparativo de armazenamento. */
class ArvoreBinariaBuscaVetorTest {

    private static int idEm(Localidade[] vetor, int indice) {
        return vetor[indice].getId();
    }

    // ---------------- fórmulas de índice ----------------

    @Test
    void formulasDeFilhosEPai() {
        assertEquals(2, ArvoreBinariaBusca.indiceFilhoEsquerdo(1));
        assertEquals(3, ArvoreBinariaBusca.indiceFilhoDireito(1));
        assertEquals(10, ArvoreBinariaBusca.indiceFilhoEsquerdo(5));
        assertEquals(11, ArvoreBinariaBusca.indiceFilhoDireito(5));
        assertEquals(1, ArvoreBinariaBusca.indicePai(2));
        assertEquals(1, ArvoreBinariaBusca.indicePai(3));
        assertEquals(3, ArvoreBinariaBusca.indicePai(7));
        assertEquals(5, ArvoreBinariaBusca.indicePai(11)); // divisão inteira
        assertEquals(0, ArvoreBinariaBusca.indicePai(1));  // a raiz não tem pai
    }

    @Test
    void paiEFilhosSaoInversos() {
        for (int i = 1; i <= 1000; i++) {
            assertEquals(i, ArvoreBinariaBusca.indicePai(ArvoreBinariaBusca.indiceFilhoEsquerdo(i)));
            assertEquals(i, ArvoreBinariaBusca.indicePai(ArvoreBinariaBusca.indiceFilhoDireito(i)));
        }
    }

    @Test
    void indicesComecamEmUm() {
        assertThrows(IllegalArgumentException.class, () -> ArvoreBinariaBusca.indiceFilhoEsquerdo(0));
        assertThrows(IllegalArgumentException.class, () -> ArvoreBinariaBusca.indiceFilhoDireito(-1));
        assertThrows(IllegalArgumentException.class, () -> ArvoreBinariaBusca.indicePai(0));
    }

    // ---------------- tamanho necessário ----------------

    @Test
    void posicoesNecessariasSaoPotenciaDeDoisMenosUm() {
        assertEquals(0L, ArvoreBinariaBusca.posicoesNecessarias(-1)); // árvore vazia
        assertEquals(1L, ArvoreBinariaBusca.posicoesNecessarias(0));
        assertEquals(3L, ArvoreBinariaBusca.posicoesNecessarias(1));
        assertEquals(15L, ArvoreBinariaBusca.posicoesNecessarias(3));
        assertEquals(63L, ArvoreBinariaBusca.posicoesNecessarias(5));
        assertEquals(1_073_741_823L, ArvoreBinariaBusca.posicoesNecessarias(29));
        assertEquals(Long.MAX_VALUE, ArvoreBinariaBusca.posicoesNecessarias(62));
        assertEquals(Long.MAX_VALUE, ArvoreBinariaBusca.posicoesNecessarias(500));
    }

    // ---------------- conversão para vetor ----------------

    @Test
    void vetorDeArvoreVaziaTemSoAPosicaoZeroSemUso() {
        Localidade[] vetor = new ArvoreBinariaBusca().paraVetor();
        assertEquals(1, vetor.length);
        assertTrue(vetor[0] == null);
    }

    @Test
    void vetorDeArvorePerfeitaFicaTodoPreenchido() {
        Localidade[] vetor = arvoreCom(2, 1, 3).paraVetor();
        assertEquals(4, vetor.length);
        assertTrue(vetor[0] == null); // posição 0 nunca é usada
        assertEquals(2, idEm(vetor, 1));
        assertEquals(1, idEm(vetor, 2));
        assertEquals(3, idEm(vetor, 3));
    }

    @Test
    void vetorDeixaNuloOndeNaoHaNo() {
        // 5 na raiz; 3 e 8 de filhos; 2 filho esquerdo do 3 (índice 4)
        Localidade[] vetor = arvoreCom(5, 3, 8, 2).paraVetor();
        assertEquals(8, vetor.length); // 2^(2+1) - 1 = 7 posições, mais a posição 0
        assertEquals(5, idEm(vetor, 1));
        assertEquals(3, idEm(vetor, 2));
        assertEquals(8, idEm(vetor, 3));
        assertEquals(2, idEm(vetor, 4));
        assertTrue(vetor[5] == null);
        assertTrue(vetor[6] == null);
        assertTrue(vetor[7] == null);
    }

    @Test
    void vetorDaArvoreDeTesteSegueAsFormulas() {
        Localidade[] vetor = arvoreDeTeste().paraVetor();
        assertEquals(64, vetor.length); // altura 5: 63 posições, mais a posição 0

        assertEquals(50, idEm(vetor, 1));
        assertEquals(30, idEm(vetor, 2));
        assertEquals(70, idEm(vetor, 3));
        assertEquals(20, idEm(vetor, 4));
        assertEquals(40, idEm(vetor, 5));
        assertEquals(60, idEm(vetor, 6));
        assertEquals(80, idEm(vetor, 7));
        assertEquals(10, idEm(vetor, 8));
        assertEquals(90, idEm(vetor, 15)); // filho direito do 80 (índice 7): 2*7+1
        assertEquals(5, idEm(vetor, 16));  // filho esquerdo do 10 (índice 8): 2*8
        assertEquals(15, idEm(vetor, 17)); // filho direito do 10: 2*8+1
        assertEquals(95, idEm(vetor, 31)); // filho direito do 90 (índice 15): 2*15+1
        assertEquals(3, idEm(vetor, 32));  // filho esquerdo do 5 (índice 16): 2*16
    }

    @Test
    void todoElementoDoVetorTemOPaiPreenchidoEComIdCoerente() {
        Localidade[] vetor = arvoreDeTeste().paraVetor();
        int preenchidos = 0;
        for (int i = 1; i < vetor.length; i++) {
            if (vetor[i] == null) {
                continue;
            }
            preenchidos++;
            if (i > 1) {
                int pai = ArvoreBinariaBusca.indicePai(i);
                assertTrue(vetor[pai] != null, "o índice " + i + " não tem pai");
                boolean ehFilhoEsquerdo = i % 2 == 0;
                if (ehFilhoEsquerdo) {
                    assertTrue(vetor[i].getId() < vetor[pai].getId());
                } else {
                    assertTrue(vetor[i].getId() > vetor[pai].getId());
                }
            }
        }
        assertEquals(13, preenchidos);
    }

    @Test
    void vetorGrandeDemaisLancaErroComMensagemClara() {
        // limite: 100000 posições. Altura 15 = 65535 cabe; altura 16 = 131071 não cabe.
        assertEquals(65_536, ArvoreBinariaBusca.escadaDeExemplo(16).paraVetor().length);
        assertThrows(IllegalStateException.class,
                () -> ArvoreBinariaBusca.escadaDeExemplo(17).paraVetor());
    }

    // ---------------- estudo de armazenamento ----------------

    @Test
    void estudoDaArvoreDeTesteDesperdicaQuaseOitentaPorCento() {
        EstudoArmazenamento estudo = arvoreDeTeste().estudarArmazenamento();
        assertEquals(13, estudo.nos());
        assertEquals(5, estudo.altura());
        assertEquals(63L, estudo.posicoesNecessarias());
        assertEquals(50L, estudo.posicoesVazias());
        assertEquals(79.365, estudo.percentualVazio(), 0.001);
        assertTrue(estudo.cabeNoVetor());
    }

    @Test
    void estudoDaVersaoBalanceadaDesperdicaMuitoMenos() {
        EstudoArmazenamento estudo = arvoreDeTeste().copiaBalanceada().estudarArmazenamento();
        assertEquals(13, estudo.nos());
        assertEquals(3, estudo.altura());
        assertEquals(15L, estudo.posicoesNecessarias());
        assertEquals(2L, estudo.posicoesVazias());
        assertEquals(13.333, estudo.percentualVazio(), 0.001);
    }

    @Test
    void estudoDaEscadaDeDezNosMostraQuaseTudoVazio() {
        EstudoArmazenamento estudo = ArvoreBinariaBusca.escadaDeExemplo(10).estudarArmazenamento();
        assertEquals(9, estudo.altura());
        assertEquals(1023L, estudo.posicoesNecessarias()); // 2^10 - 1
        assertEquals(1013L, estudo.posicoesVazias());
        assertEquals(99.02, estudo.percentualVazio(), 0.01);
    }

    @Test
    void estudoDaEscadaDeTrintaNosPassaDeUmBilhaoDePosicoes() {
        EstudoArmazenamento estudo = ArvoreBinariaBusca.escadaDeExemplo(30).estudarArmazenamento();
        assertEquals(29, estudo.altura());
        assertEquals(1_073_741_823L, estudo.posicoesNecessarias()); // 2^30 - 1
        assertTrue(estudo.percentualVazio() > 99.999);
        assertFalse(estudo.cabeNoVetor());
    }

    @Test
    void estudoDeArvoreVaziaTemTudoZerado() {
        EstudoArmazenamento estudo = new ArvoreBinariaBusca().estudarArmazenamento();
        assertEquals(0, estudo.nos());
        assertEquals(-1, estudo.altura());
        assertEquals(0L, estudo.posicoesNecessarias());
        assertEquals(0L, estudo.posicoesVazias());
        assertEquals(0.0, estudo.percentualVazio(), 0.0);
    }

    @Test
    void arvoreBalanceadaDeNNosUsaMenosDoQueODobroDeNPosicoes() {
        // balanceada: 2^(floor(log2 n)+1) - 1 < 2n, ou seja, o desperdício é limitado
        for (int n = 1; n <= 300; n++) {
            ArvoreBinariaBusca balanceada = ArvoreBinariaBusca.escadaDeExemplo(n).copiaBalanceada();
            EstudoArmazenamento estudo = balanceada.estudarArmazenamento();
            assertTrue(estudo.posicoesNecessarias() < 2L * n, "posições demais com n=" + n);
        }
    }
}