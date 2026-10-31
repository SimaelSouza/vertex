package br.ifg.vertex.arvore;

import static br.ifg.vertex.arvore.ArvoresParaTeste.IDS_DE_TESTE;
import static br.ifg.vertex.arvore.ArvoresParaTeste.arvoreCom;
import static br.ifg.vertex.arvore.ArvoresParaTeste.arvoreDeTeste;
import static br.ifg.vertex.arvore.ArvoresParaTeste.ids;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.ifg.vertex.modelo.Localidade;
import br.ifg.vertex.modelo.TipoLocalidade;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Testes de remoção (3 casos + raiz) e atualização. Para conferir o FORMATO da árvore
 * depois de cada remoção usamos a pré-ordem, que "denuncia" a estrutura.
 */
class ArvoreBinariaBuscaRemocaoAtualizacaoTest {

    // ---------------- remoção: caso 1 (folha) ----------------

    @Test
    void removerFolhaApenasSomeComOGalho() {
        ArvoreBinariaBusca arvore = arvoreDeTeste();
        assertTrue(arvore.remover(3)); // folha, filha esquerda de 5

        assertEquals(12, arvore.quantidadeNos());
        assertFalse(arvore.contem(3));
        assertEquals(List.of(50, 30, 20, 10, 5, 15, 40, 70, 60, 80, 90, 95), ids(arvore.preOrdem()));
        assertEquals(4, arvore.altura()); // antes era 5: o 3 era o nó mais fundo
    }

    // ---------------- remoção: caso 2 (um filho) ----------------

    @Test
    void removerNoComSoFilhoEsquerdoSobeOFilho() {
        ArvoreBinariaBusca arvore = arvoreDeTeste();
        assertTrue(arvore.remover(5)); // só tem o filho esquerdo (3)

        assertEquals(List.of(50, 30, 20, 10, 3, 15, 40, 70, 60, 80, 90, 95), ids(arvore.preOrdem()));
        assertEquals(4, arvore.nivel(3)); // o 3 subiu um nível (era 5)
    }

    @Test
    void removerNoComSoFilhoDireitoSobeOFilho() {
        ArvoreBinariaBusca arvore = arvoreDeTeste();
        assertTrue(arvore.remover(80)); // só tem o filho direito (90)

        assertEquals(List.of(50, 30, 20, 10, 5, 3, 15, 40, 70, 60, 90, 95), ids(arvore.preOrdem()));
        assertEquals(2, arvore.nivel(90));
    }

    // ---------------- remoção: caso 3 (dois filhos) ----------------

    @Test
    void removerNoComDoisFilhosUsaOSucessorEmOrdem() {
        ArvoreBinariaBusca arvore = arvoreDeTeste();
        assertTrue(arvore.remover(10)); // filhos 5 e 15; sucessor = 15

        assertEquals(List.of(50, 30, 20, 15, 5, 3, 40, 70, 60, 80, 90, 95), ids(arvore.preOrdem()));
        assertFalse(arvore.contem(10));
        assertTrue(arvore.contem(15));
    }

    @Test
    void sucessorQueEFilhoDiretoDaDireita() {
        // 70 não tem filho esquerdo: ele mesmo é o sucessor de 50
        ArvoreBinariaBusca arvore = arvoreCom(50, 30, 70, 80);
        assertTrue(arvore.remover(50));
        assertEquals(List.of(70, 30, 80), ids(arvore.preOrdem()));
    }

    @Test
    void sucessorComFilhoDireitoTemOFilhoReligadoAoPai() {
        // sucessor de 50 é 60, que tem o filho direito 65: o 65 deve ocupar o lugar do 60
        ArvoreBinariaBusca arvore = arvoreCom(50, 30, 70, 60, 65);
        assertTrue(arvore.remover(50));
        assertEquals(List.of(60, 30, 70, 65), ids(arvore.preOrdem()));
        assertEquals(List.of(30, 60, 65, 70), ids(arvore.emOrdem()));
    }

    // ---------------- remoção da raiz ----------------

    @Test
    void removerRaizComDoisFilhos() {
        ArvoreBinariaBusca arvore = arvoreDeTeste();
        assertTrue(arvore.remover(50)); // sucessor = 60 (menor da subárvore direita)

        assertEquals(60, arvore.raiz().get().getId());
        assertEquals(List.of(60, 30, 20, 10, 5, 3, 15, 40, 70, 80, 90, 95), ids(arvore.preOrdem()));
    }

    @Test
    void removerRaizComUmSoFilhoOFilhoViraRaiz() {
        ArvoreBinariaBusca arvore = arvoreCom(50, 70, 80);
        assertTrue(arvore.remover(50));
        assertEquals(70, arvore.raiz().get().getId());
        assertEquals(List.of(70, 80), ids(arvore.preOrdem()));
    }

    @Test
    void removerOUnicoNoDeixaAArvoreVazia() {
        ArvoreBinariaBusca arvore = arvoreCom(42);
        assertTrue(arvore.remover(42));
        assertTrue(arvore.estaVazia());
        assertEquals(0, arvore.quantidadeNos());
        assertEquals(-1, arvore.altura());
    }

    // ---------------- remoção: situações de borda ----------------

    @Test
    void removerIdInexistenteDevolveFalseENaoMudaNada() {
        ArvoreBinariaBusca arvore = arvoreDeTeste();
        List<Integer> antes = ids(arvore.preOrdem());

        assertFalse(arvore.remover(99));
        assertFalse(arvore.remover(-5));
        assertEquals(antes, ids(arvore.preOrdem()));
    }

    @Test
    void removerDeArvoreVaziaDevolveFalse() {
        assertFalse(new ArvoreBinariaBusca().remover(1));
    }

    @Test
    void removerTodosUmPorUmMantemAArvoreValidaEmCadaPasso() {
        ArvoreBinariaBusca arvore = arvoreDeTeste();
        List<Integer> restantes = new ArrayList<>();
        for (int id : IDS_DE_TESTE) {
            restantes.add(id);
        }

        // remove na mesma ordem de inserção: raiz primeiro, depois todos os tipos de nó
        for (int id : IDS_DE_TESTE) {
            assertTrue(arvore.remover(id), "deveria remover " + id);
            restantes.remove(Integer.valueOf(id));

            assertEquals(restantes.size(), arvore.quantidadeNos());
            assertFalse(arvore.contem(id));

            // em ordem continua crescente e com exatamente os IDs que sobraram
            List<Integer> emOrdem = ids(arvore.emOrdem());
            List<Integer> esperado = new ArrayList<>(restantes);
            esperado.sort(null);
            assertEquals(esperado, emOrdem);
            for (int sobrou : restantes) {
                assertTrue(arvore.contem(sobrou), "o " + sobrou + " sumiu depois de remover " + id);
            }
        }
        assertTrue(arvore.estaVazia());
    }

    @Test
    void removerTodosEmOrdemInversaTambemFunciona() {
        ArvoreBinariaBusca arvore = arvoreDeTeste();
        for (int i = IDS_DE_TESTE.length - 1; i >= 0; i--) {
            assertTrue(arvore.remover(IDS_DE_TESTE[i]));
        }
        assertTrue(arvore.estaVazia());
    }

    @Test
    void removerEDepoisInserirDeNovoFunciona() {
        ArvoreBinariaBusca arvore = arvoreDeTeste();
        arvore.remover(30);
        assertTrue(arvore.inserir(new Localidade(30, "Nova", TipoLocalidade.BAIRRO, 5)));
        assertEquals("Nova", arvore.pesquisar(30).get().getNome());
        assertEquals(13, arvore.quantidadeNos());
    }

    // ---------------- atualização ----------------

    @Test
    void atualizarTrocaOsCamposEPreservaIdEPosicao() {
        ArvoreBinariaBusca arvore = arvoreDeTeste();
        List<Integer> estruturaAntes = ids(arvore.preOrdem());

        assertTrue(arvore.atualizar(40, "Santo Antônio", TipoLocalidade.BAIRRO, 777));

        Localidade l = arvore.pesquisar(40).get();
        assertEquals(40, l.getId());
        assertEquals("Santo Antônio", l.getNome());
        assertEquals(TipoLocalidade.BAIRRO, l.getTipo());
        assertEquals(777, l.getPopulacao());
        assertEquals(estruturaAntes, ids(arvore.preOrdem())); // nada mudou de lugar
    }

    @Test
    void atualizarIdInexistenteDevolveFalse() {
        assertFalse(arvoreDeTeste().atualizar(99, "X", TipoLocalidade.CIDADE, 1));
        assertFalse(new ArvoreBinariaBusca().atualizar(1, "X", TipoLocalidade.CIDADE, 1));
    }

    @Test
    void atualizarComDadoInvalidoLancaErroENaoMudaNada() {
        ArvoreBinariaBusca arvore = arvoreDeTeste();

        // o nome é válido, mas a população não: o nome NÃO pode ter sido trocado
        assertThrows(IllegalArgumentException.class,
                () -> arvore.atualizar(30, "Nome novo", TipoLocalidade.BAIRRO, -1));
        assertThrows(IllegalArgumentException.class,
                () -> arvore.atualizar(30, "   ", TipoLocalidade.BAIRRO, 1));
        assertThrows(IllegalArgumentException.class,
                () -> arvore.atualizar(30, "Nome novo", null, 1));

        Localidade l = arvore.pesquisar(30).get();
        assertEquals("Local 30", l.getNome());
        assertEquals(TipoLocalidade.CIDADE, l.getTipo());
        assertEquals(1030, l.getPopulacao());
    }
}