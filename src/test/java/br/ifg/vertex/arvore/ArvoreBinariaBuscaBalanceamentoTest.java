package br.ifg.vertex.arvore;

import static br.ifg.vertex.arvore.ArvoresParaTeste.arvoreCom;
import static br.ifg.vertex.arvore.ArvoresParaTeste.arvoreDeTeste;
import static br.ifg.vertex.arvore.ArvoresParaTeste.ids;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.ifg.vertex.modelo.Localidade;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Testes da análise de balanceamento e da reconstrução balanceada. */
class ArvoreBinariaBuscaBalanceamentoTest {

    // ---------------- análise ----------------

    @Test
    void analiseDaArvoreDeTesteTrazAsAlturasDeCadaNoEmOrdemDeId() {
        List<BalanceamentoNo> analise = arvoreDeTeste().analisarBalanceamento();

        // {id, altura esquerda, altura direita}; subárvore vazia = -1
        int[][] esperado = {
                {3, -1, -1}, {5, 0, -1}, {10, 1, 0}, {15, -1, -1}, {20, 2, -1}, {30, 3, 0},
                {40, -1, -1}, {50, 4, 3}, {60, -1, -1}, {70, 0, 2}, {80, -1, 1}, {90, -1, 0},
                {95, -1, -1}
        };
        assertEquals(esperado.length, analise.size());
        for (int i = 0; i < esperado.length; i++) {
            BalanceamentoNo no = analise.get(i);
            assertEquals(esperado[i][0], no.id());
            assertEquals(esperado[i][1], no.alturaEsquerda(), "altura esquerda do " + no.id());
            assertEquals(esperado[i][2], no.alturaDireita(), "altura direita do " + no.id());
        }
    }

    @Test
    void diferencaEEsquerdaMenosDireita() {
        List<BalanceamentoNo> analise = arvoreDeTeste().analisarBalanceamento();
        BalanceamentoNo no20 = analise.get(4);
        BalanceamentoNo no70 = analise.get(9);
        assertEquals(20, no20.id());
        assertEquals(3, no20.diferenca());   // pesa 3 para a esquerda
        assertEquals(70, no70.id());
        assertEquals(-2, no70.diferenca());  // pesa 2 para a direita
    }

    @Test
    void nosDesbalanceadosDaArvoreDeTeste() {
        List<Integer> violadores = new ArrayList<>();
        for (BalanceamentoNo no : arvoreDeTeste().nosDesbalanceados()) {
            violadores.add(no.id());
        }
        assertEquals(List.of(20, 30, 70, 80), violadores);
    }

    @Test
    void arvoreComRaizOkMasNosInternosRuinsNaoEBalanceada() {
        ArvoreBinariaBusca arvore = arvoreDeTeste();
        BalanceamentoNo raiz = arvore.analisarBalanceamento().get(7);
        assertEquals(50, raiz.id());
        assertTrue(raiz.estaBalanceado()); // 4 contra 3: diferença 1
        assertFalse(arvore.estaBalanceada()); // mas há violadores mais abaixo
    }

    @Test
    void diferencaDeUmaUnidadeEAceita() {
        assertTrue(arvoreCom(2, 1).estaBalanceada());          // 0 contra -1
        assertTrue(arvoreCom(2, 1, 3).estaBalanceada());       // perfeita
        assertTrue(arvoreCom(5, 3, 8, 2).estaBalanceada());    // 1 contra 0 na raiz
    }

    @Test
    void diferencaDeDuasUnidadesNaoEAceita() {
        assertFalse(arvoreCom(1, 2, 3).estaBalanceada());      // escada de 3 nós
        assertFalse(arvoreCom(3, 2, 1).estaBalanceada());
    }

    @Test
    void arvoreVaziaEComUmNoSaoBalanceadas() {
        assertTrue(new ArvoreBinariaBusca().estaBalanceada());
        assertTrue(new ArvoreBinariaBusca().analisarBalanceamento().isEmpty());
        assertTrue(arvoreCom(7).estaBalanceada());
        assertEquals(1, arvoreCom(7).analisarBalanceamento().size());
    }

    // ---------------- reconstrução ----------------

    @Test
    void reconstruirArvoreDeTesteGeraArvoreBalanceadaDeAlturaTres() {
        ArvoreBinariaBusca arvore = arvoreDeTeste();
        assertFalse(arvore.estaBalanceada());

        arvore.reconstruirBalanceada();

        assertTrue(arvore.estaBalanceada());
        assertEquals(13, arvore.quantidadeNos());
        assertEquals(3, arvore.altura()); // floor(log2 13)
        assertEquals(List.of(3, 5, 10, 15, 20, 30, 40, 50, 60, 70, 80, 90, 95),
                ids(arvore.emOrdem()));
        // formato exato: o elemento do meio (o 7º, ID 40) vira a raiz, e assim por diante
        assertEquals(List.of(40, 10, 3, 5, 20, 15, 30, 70, 50, 60, 90, 80, 95),
                ids(arvore.preOrdem()));
    }

    @Test
    void reconstruirReaproveitaOsMesmosObjetosLocalidade() {
        ArvoreBinariaBusca arvore = arvoreDeTeste();
        Localidade antes = arvore.pesquisar(60).get();

        arvore.reconstruirBalanceada();

        assertTrue(antes == arvore.pesquisar(60).get(), "o objeto Localidade deve ser o mesmo");
    }

    @Test
    void reconstruirArvoreVaziaOuComUmNoNaoMudaNada() {
        ArvoreBinariaBusca vazia = new ArvoreBinariaBusca();
        vazia.reconstruirBalanceada();
        assertTrue(vazia.estaVazia());

        ArvoreBinariaBusca umNo = arvoreCom(9);
        umNo.reconstruirBalanceada();
        assertEquals(List.of(9), ids(umNo.preOrdem()));
    }

    @Test
    void reconstruirEscadasDeQualquerTamanhoGeraAlturaMinima() {
        for (int n = 1; n <= 200; n++) {
            ArvoreBinariaBusca arvore = ArvoreBinariaBusca.escadaDeExemplo(n);
            assertEquals(n - 1, arvore.altura()); // antes: escada

            arvore.reconstruirBalanceada();

            int alturaMinima = 31 - Integer.numberOfLeadingZeros(n); // floor(log2 n)
            assertEquals(alturaMinima, arvore.altura(), "altura com n=" + n);
            assertTrue(arvore.estaBalanceada(), "balanceada com n=" + n);
            assertEquals(n, arvore.quantidadeNos());

            List<Integer> esperado = new ArrayList<>();
            for (int id = 1; id <= n; id++) {
                esperado.add(id);
            }
            assertEquals(esperado, ids(arvore.emOrdem())); // mesmos IDs, ainda ordenados
        }
    }

    @Test
    void depoisDeReconstruirInsercaoERemocaoContinuamFuncionando() {
        ArvoreBinariaBusca arvore = arvoreDeTeste();
        arvore.reconstruirBalanceada();

        assertTrue(arvore.remover(40)); // a nova raiz
        assertTrue(arvore.inserir(ArvoresParaTeste.loc(41)));
        assertEquals(13, arvore.quantidadeNos());
        assertTrue(arvore.contem(41));
        assertFalse(arvore.contem(40));
    }

    // ---------------- cópia balanceada ----------------

    @Test
    void copiaBalanceadaNaoAlteraAOriginal() {
        ArvoreBinariaBusca original = arvoreDeTeste();
        List<Integer> estruturaAntes = ids(original.preOrdem());

        ArvoreBinariaBusca copia = original.copiaBalanceada();

        assertEquals(estruturaAntes, ids(original.preOrdem()));
        assertEquals(5, original.altura());
        assertEquals(3, copia.altura());
        assertTrue(copia.estaBalanceada());
        assertEquals(ids(original.emOrdem()), ids(copia.emOrdem()));
        assertTrue(original.pesquisar(15).get() == copia.pesquisar(15).get());
    }

    @Test
    void copiaBalanceadaDeArvoreVaziaEVazia() {
        assertTrue(new ArvoreBinariaBusca().copiaBalanceada().estaVazia());
    }

    // ---------------- escada de exemplo ----------------

    @Test
    void escadaDeExemploTemAlturaIgualAQuantidadeMenosUm() {
        ArvoreBinariaBusca escada = ArvoreBinariaBusca.escadaDeExemplo(10);
        assertEquals(10, escada.quantidadeNos());
        assertEquals(9, escada.altura());
        assertEquals(1, escada.quantidadeFolhas());
        assertFalse(escada.estaBalanceada());
        assertTrue(ArvoreBinariaBusca.escadaDeExemplo(0).estaVazia());
    }
}