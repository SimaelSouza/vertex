package br.ifg.vertex.arvore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.ifg.vertex.modelo.Localidade;
import br.ifg.vertex.modelo.TipoLocalidade;
import org.junit.jupiter.api.Test;

/**
 * Testes da etapa 2. A árvore de referência usa os 13 IDs de teste do planejamento,
 * inseridos nesta ordem (que deixa a árvore desbalanceada de propósito):
 *
 * <pre>
 *                 50
 *            /          \
 *          30            70
 *         /  \          /  \
 *       20    40      60    80
 *      /                      \
 *    10                        90
 *   /  \                         \
 *  5    15                        95
 * /
 * 3
 * </pre>
 */
class ArvoreBinariaBuscaTest {

    private static final int[] IDS_DE_TESTE = {50, 30, 70, 20, 40, 60, 80, 10, 5, 3, 15, 90, 95};

    private static Localidade loc(int id) {
        return new Localidade(id, "Local " + id, TipoLocalidade.CIDADE, 1000 + id);
    }

    /** Monta a árvore de 13 nós da figura acima. */
    private static ArvoreBinariaBusca arvoreDeTeste() {
        ArvoreBinariaBusca arvore = new ArvoreBinariaBusca();
        for (int id : IDS_DE_TESTE) {
            assertTrue(arvore.inserir(loc(id)));
        }
        return arvore;
    }

    // ---------------- árvore vazia ----------------

    @Test
    void arvoreVaziaTemValoresNeutros() {
        ArvoreBinariaBusca arvore = new ArvoreBinariaBusca();
        assertTrue(arvore.estaVazia());
        assertEquals(0, arvore.quantidadeNos());
        assertEquals(0, arvore.quantidadeFolhas());
        assertEquals(0, arvore.quantidadeInternos());
        assertEquals(-1, arvore.altura());
        assertEquals(-1, arvore.nivel(10));
        assertEquals(0, arvore.comparacoesNaBusca(10));
        assertFalse(arvore.pesquisar(10).isPresent());
        assertFalse(arvore.contem(10));
        assertFalse(arvore.menor().isPresent());
        assertFalse(arvore.maior().isPresent());
        assertFalse(arvore.raiz().isPresent());
    }

    @Test
    void arvoreComUmNoTemAlturaZeroEUmaFolha() {
        ArvoreBinariaBusca arvore = new ArvoreBinariaBusca();
        arvore.inserir(loc(42));
        assertFalse(arvore.estaVazia());
        assertEquals(1, arvore.quantidadeNos());
        assertEquals(1, arvore.quantidadeFolhas());
        assertEquals(0, arvore.quantidadeInternos());
        assertEquals(0, arvore.altura());
        assertEquals(0, arvore.nivel(42));
        assertEquals(42, arvore.menor().get().getId());
        assertEquals(42, arvore.maior().get().getId());
    }

    // ---------------- inserção ----------------

    @Test
    void insercaoRejeitaIdRepetidoEMantemOOriginal() {
        ArvoreBinariaBusca arvore = arvoreDeTeste();
        Localidade impostora = new Localidade(30, "Impostora", TipoLocalidade.BAIRRO, 1);

        assertFalse(arvore.inserir(impostora));
        assertEquals(13, arvore.quantidadeNos());
        assertEquals("Local 30", arvore.pesquisar(30).get().getNome());
    }

    @Test
    void insercaoRejeitaLocalidadeNula() {
        assertThrows(IllegalArgumentException.class, () -> new ArvoreBinariaBusca().inserir(null));
    }

    @Test
    void raizEAPrimeiraLocalidadeInserida() {
        assertEquals(50, arvoreDeTeste().raiz().get().getId());
    }

    // ---------------- pesquisa ----------------

    @Test
    void pesquisaEncontraTodosOsIdsInseridos() {
        ArvoreBinariaBusca arvore = arvoreDeTeste();
        for (int id : IDS_DE_TESTE) {
            assertTrue(arvore.contem(id), "deveria achar " + id);
            assertEquals("Local " + id, arvore.pesquisar(id).get().getNome());
        }
    }

    @Test
    void pesquisaDeIdInexistenteDevolveVazio() {
        ArvoreBinariaBusca arvore = arvoreDeTeste();
        for (int id : new int[] {1, 4, 35, 55, 99, 1000}) {
            assertFalse(arvore.pesquisar(id).isPresent(), "não deveria achar " + id);
        }
    }

    @Test
    void comparacoesNaBuscaContamOsNosDoCaminho() {
        ArvoreBinariaBusca arvore = arvoreDeTeste();
        assertEquals(1, arvore.comparacoesNaBusca(50)); // a raiz
        assertEquals(2, arvore.comparacoesNaBusca(70)); // 50, 70
        assertEquals(6, arvore.comparacoesNaBusca(3));  // 50, 30, 20, 10, 5, 3
        assertEquals(5, arvore.comparacoesNaBusca(95)); // 50, 70, 80, 90, 95
        assertEquals(5, arvore.comparacoesNaBusca(99)); // mesmo caminho do 95, e cai fora
        assertEquals(6, arvore.comparacoesNaBusca(1));  // mesmo caminho do 3, e cai fora
    }

    // ---------------- menor e maior ----------------

    @Test
    void menorEMaiorIdDaArvoreDeTeste() {
        ArvoreBinariaBusca arvore = arvoreDeTeste();
        assertEquals(3, arvore.menor().get().getId());
        assertEquals(95, arvore.maior().get().getId());
    }

    // ---------------- contagens e altura ----------------

    @Test
    void contagensDaArvoreDeTeste() {
        ArvoreBinariaBusca arvore = arvoreDeTeste();
        assertEquals(13, arvore.quantidadeNos());
        assertEquals(5, arvore.quantidadeFolhas());   // 3, 15, 40, 60, 95
        assertEquals(8, arvore.quantidadeInternos()); // 13 - 5
    }

    @Test
    void alturaDaArvoreDeTesteEFicaCincoEmArestas() {
        assertEquals(5, arvoreDeTeste().altura()); // 50-30-20-10-5-3
    }

    @Test
    void niveisDaArvoreDeTeste() {
        ArvoreBinariaBusca arvore = arvoreDeTeste();
        assertEquals(0, arvore.nivel(50));
        assertEquals(1, arvore.nivel(30));
        assertEquals(1, arvore.nivel(70));
        assertEquals(2, arvore.nivel(20));
        assertEquals(2, arvore.nivel(40));
        assertEquals(2, arvore.nivel(60));
        assertEquals(2, arvore.nivel(80));
        assertEquals(3, arvore.nivel(10));
        assertEquals(3, arvore.nivel(90));
        assertEquals(4, arvore.nivel(5));
        assertEquals(4, arvore.nivel(15));
        assertEquals(4, arvore.nivel(95));
        assertEquals(5, arvore.nivel(3));
    }

    @Test
    void nivelDeIdInexistenteEMenosUm() {
        assertEquals(-1, arvoreDeTeste().nivel(77));
    }

    // ---------------- árvores em "escada" ----------------

    @Test
    void idsCrescentesFormamEscadaParaADireita() {
        ArvoreBinariaBusca arvore = new ArvoreBinariaBusca();
        for (int id = 1; id <= 10; id++) {
            arvore.inserir(loc(id));
        }
        assertEquals(9, arvore.altura());
        assertEquals(1, arvore.quantidadeFolhas());
        assertEquals(9, arvore.quantidadeInternos());
        assertEquals(10, arvore.comparacoesNaBusca(10)); // pior caso: percorre tudo
        assertEquals(1, arvore.menor().get().getId());
        assertEquals(10, arvore.maior().get().getId());
    }

    @Test
    void idsDecrescentesFormamEscadaParaAEsquerda() {
        ArvoreBinariaBusca arvore = new ArvoreBinariaBusca();
        for (int id = 10; id >= 1; id--) {
            arvore.inserir(loc(id));
        }
        assertEquals(9, arvore.altura());
        assertEquals(9, arvore.nivel(1));
        assertEquals(1, arvore.menor().get().getId());
    }

    @Test
    void ordemDeInsercaoNaoMudaOConjuntoDeIds() {
        // inserindo na ordem inversa o formato muda, mas todos continuam acháveis
        ArvoreBinariaBusca arvore = new ArvoreBinariaBusca();
        for (int i = IDS_DE_TESTE.length - 1; i >= 0; i--) {
            arvore.inserir(loc(IDS_DE_TESTE[i]));
        }
        assertEquals(13, arvore.quantidadeNos());
        for (int id : IDS_DE_TESTE) {
            assertTrue(arvore.contem(id));
        }
        assertEquals(3, arvore.menor().get().getId());
        assertEquals(95, arvore.maior().get().getId());
    }
}