package br.ifg.vertex.arvore;

import static br.ifg.vertex.arvore.ArvoresParaTeste.arvoreCom;
import static br.ifg.vertex.arvore.ArvoresParaTeste.arvoreDeTeste;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Testes do desenho textual ("deitado") da árvore. */
class ArvoreBinariaBuscaDesenhoTest {

    @Test
    void arvoreVaziaTemDesenhoVazio() {
        assertEquals("", new ArvoreBinariaBusca().desenhoLateral());
    }

    @Test
    void umNoSoDesenhaSoARaiz() {
        assertEquals("7 (Local 7)\n", arvoreCom(7).desenhoLateral());
    }

    @Test
    void filhoDireitoFicaAcimaEEsquerdoAbaixo() {
        assertEquals(
                "    /-- 3 (Local 3)\n"
                        + "2 (Local 2)\n"
                        + "    \\-- 1 (Local 1)\n",
                arvoreCom(2, 1, 3).desenhoLateral());
    }

    @Test
    void aArvoreDeTesteTemUmaLinhaPorNoEIndentacaoPorNivel() {
        String[] linhas = arvoreDeTeste().desenhoLateral().split("\n");

        assertEquals(13, linhas.length);
        // primeira linha: o nó mais à direita (95, nível 4); última: o mais à esquerda (3, nível 5)
        assertEquals("                /-- 95 (Local 95)", linhas[0]);
        assertEquals("                    \\-- 3 (Local 3)", linhas[12]);
        assertEquals("50 (Local 50)", linhas[5]); // a raiz, sem indentação, fica no meio
    }
}