package br.ifg.vertex.servico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.ifg.vertex.arvore.ArvoreBinariaBusca;
import br.ifg.vertex.grafo.Grafo;
import br.ifg.vertex.grafo.ResultadoBusca;
import br.ifg.vertex.modelo.Aresta;
import br.ifg.vertex.modelo.Localidade;
import br.ifg.vertex.modelo.TipoLocalidade;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Testes da integração árvore + grafo: as regras de consistência do slide 19. */
class SistemaCidadesTest {

    private static SistemaCidades sistemaCom(int... ids) {
        SistemaCidades sistema = new SistemaCidades();
        for (int id : ids) {
            sistema.cadastrarLocalidade(id, "Local " + id, TipoLocalidade.CIDADE, 1000 + id);
        }
        return sistema;
    }

    // ---------------- cadastro ----------------

    @Test
    void cadastrarCriaALocalidadeNaArvoreEOVerticeNoGrafo() {
        SistemaCidades sistema = sistemaCom();
        Localidade nova = sistema.cadastrarLocalidade(15, "Centro", TipoLocalidade.BAIRRO, 28000);

        assertEquals(15, nova.getId());
        assertTrue(sistema.getArvore().contem(15));
        assertTrue(sistema.getGrafo().contemVertice(15));
        assertTrue(sistema.estaConsistente());
    }

    @Test
    void cadastrarIdRepetidoLancaErroENaoMudaNada() {
        SistemaCidades sistema = sistemaCom(10, 20);

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> sistema.cadastrarLocalidade(10, "Outra", TipoLocalidade.BAIRRO, 5));

        assertEquals("Já existe uma localidade com o ID 10.", erro.getMessage());
        assertEquals(2, sistema.getArvore().quantidadeNos());
        assertEquals("Local 10", sistema.nomeDe(10)); // a original ficou
    }

    @Test
    void cadastrarDadosInvalidosLancaErroComMensagemEmPortugues() {
        SistemaCidades sistema = sistemaCom();

        assertEquals("O nome não pode ficar vazio.", assertThrows(RegraNegocioException.class,
                () -> sistema.cadastrarLocalidade(1, "  ", TipoLocalidade.CIDADE, 1)).getMessage());
        assertEquals("O ID deve ser um número positivo.", assertThrows(RegraNegocioException.class,
                () -> sistema.cadastrarLocalidade(0, "X", TipoLocalidade.CIDADE, 1)).getMessage());
        assertEquals("A população não pode ser negativa.", assertThrows(RegraNegocioException.class,
                () -> sistema.cadastrarLocalidade(1, "X", TipoLocalidade.CIDADE, -1)).getMessage());
        assertTrue(sistema.getArvore().estaVazia());
        assertEquals(0, sistema.getGrafo().quantidadeVertices());
    }

    @Test
    void cadastrarDesfazAArvoreSeOGrafoFalhar() {
        // grafo "contaminado": já tem o vértice 7, mas a árvore não tem a localidade 7
        Grafo grafo = new Grafo();
        grafo.adicionarVertice(7);
        ArvoreBinariaBusca arvore = new ArvoreBinariaBusca();
        SistemaCidades sistema = new SistemaCidades(arvore, grafo);

        assertThrows(RegraNegocioException.class,
                () -> sistema.cadastrarLocalidade(7, "X", TipoLocalidade.CIDADE, 1));

        assertFalse(arvore.contem(7)); // o passo 1 foi desfeito: nada de localidade "meio cadastrada"
    }

    // ---------------- pesquisa, consulta e atualização ----------------

    @Test
    void pesquisarTrazNivelEComparacoes() {
        SistemaCidades sistema = new SistemaCidades();
        DadosTeste.carregar(sistema);

        ResultadoPesquisa r = sistema.pesquisarLocalidade(3).get();
        assertEquals(3, r.localidade().getId());
        assertEquals(5, r.nivel());
        assertEquals(6, r.comparacoes());
        assertFalse(sistema.pesquisarLocalidade(99).isPresent());
    }

    @Test
    void exigirLocalidadeLancaErroSeNaoExistir() {
        SistemaCidades sistema = sistemaCom(1);
        assertEquals("Não existe localidade cadastrada com o ID 99.", assertThrows(
                RegraNegocioException.class, () -> sistema.exigirLocalidade(99)).getMessage());
        assertEquals(1, sistema.exigirLocalidade(1).getId());
    }

    @Test
    void atualizacaoApareceNasConsultasEPreservaAsConexoes() {
        SistemaCidades sistema = sistemaCom(1, 2);
        sistema.conectar(1, 2, 10, 15, true);

        sistema.atualizarLocalidade(2, "Nome Novo", TipoLocalidade.BAIRRO, 42);

        assertEquals("Nome Novo", sistema.nomeDe(2)); // as rotas mostram o nome buscado na árvore
        assertEquals(2, sistema.getArvore().pesquisar(2).get().getId());
        assertEquals(2, sistema.getGrafo().quantidadeArestas());
        assertTrue(sistema.estaConsistente());
    }

    @Test
    void atualizarComDadoInvalidoOuIdInexistenteLancaErro() {
        SistemaCidades sistema = sistemaCom(1);

        assertThrows(RegraNegocioException.class,
                () -> sistema.atualizarLocalidade(1, "Novo", TipoLocalidade.CIDADE, -5));
        assertEquals("Local 1", sistema.nomeDe(1)); // nada mudou
        assertThrows(RegraNegocioException.class,
                () -> sistema.atualizarLocalidade(99, "X", TipoLocalidade.CIDADE, 1));
    }

    // ---------------- conectar ----------------

    @Test
    void conectarExigeAsDuasLocalidadesNaArvore() {
        SistemaCidades sistema = sistemaCom(1, 2);

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> sistema.conectar(1, 99, 5, 5, false));
        assertEquals("Não existe localidade cadastrada com o ID 99.", erro.getMessage());
        assertThrows(RegraNegocioException.class, () -> sistema.conectar(99, 1, 5, 5, true));
        assertEquals(0, sistema.getGrafo().quantidadeArestas());
    }

    @Test
    void conectarRejeitaLacoPesosInvalidosEDuplicata() {
        SistemaCidades sistema = sistemaCom(1, 2);

        assertThrows(RegraNegocioException.class, () -> sistema.conectar(1, 1, 5, 5, false));
        assertThrows(RegraNegocioException.class, () -> sistema.conectar(1, 2, 0, 5, false));
        assertThrows(RegraNegocioException.class, () -> sistema.conectar(1, 2, 5, 0, true));
        assertEquals(0, sistema.getGrafo().quantidadeArestas());

        sistema.conectar(1, 2, 5, 5, false);
        RegraNegocioException duplicada = assertThrows(RegraNegocioException.class,
                () -> sistema.conectar(1, 2, 9, 9, false));
        assertEquals("Já existe uma conexão de 1 (Local 1) para 2 (Local 2).", duplicada.getMessage());
        assertEquals(1, sistema.getGrafo().quantidadeArestas());
    }

    @Test
    void conectarMaoUnicaCriaUmaArestaEMaoDuplaCriaDuas() {
        SistemaCidades sistema = sistemaCom(1, 2, 3);

        sistema.conectar(1, 2, 5, 10, false);
        assertTrue(sistema.existeConexao(1, 2));
        assertFalse(sistema.existeConexao(2, 1));

        sistema.conectar(2, 3, 7.5, 12, true);
        assertEquals(new Aresta(2, 3, 7.5, 12), sistema.getGrafo().obterAresta(2, 3).get());
        assertEquals(new Aresta(3, 2, 7.5, 12), sistema.getGrafo().obterAresta(3, 2).get());
        assertEquals(3, sistema.getGrafo().quantidadeArestas());
    }

    @Test
    void conectarMaoDuplaRejeitaSeAVoltaJaExisteSemCriarMetade() {
        SistemaCidades sistema = sistemaCom(1, 2);
        sistema.conectar(2, 1, 5, 5, false); // só a volta existe

        assertThrows(RegraNegocioException.class, () -> sistema.conectar(1, 2, 5, 5, true));

        assertEquals(1, sistema.getGrafo().quantidadeArestas());
        assertFalse(sistema.existeConexao(1, 2));
    }

    // ---------------- alterar e desconectar ----------------

    @Test
    void alterarConexaoTrocaUmSentidoOuOsDois() {
        SistemaCidades sistema = sistemaCom(1, 2);
        sistema.conectar(1, 2, 5, 10, true);

        sistema.alterarConexao(1, 2, 8, 20, false);
        assertEquals(8.0, sistema.getGrafo().obterAresta(1, 2).get().distanciaKm());
        assertEquals(5.0, sistema.getGrafo().obterAresta(2, 1).get().distanciaKm()); // volta intacta

        sistema.alterarConexao(1, 2, 9, 25, true);
        assertEquals(new Aresta(1, 2, 9, 25), sistema.getGrafo().obterAresta(1, 2).get());
        assertEquals(new Aresta(2, 1, 9, 25), sistema.getGrafo().obterAresta(2, 1).get());
    }

    @Test
    void alterarConexaoQueNaoExisteOuComVoltaInexistenteLancaErro() {
        SistemaCidades sistema = sistemaCom(1, 2);
        assertThrows(RegraNegocioException.class, () -> sistema.alterarConexao(1, 2, 5, 5, false));

        sistema.conectar(1, 2, 5, 10, false); // mão única
        assertThrows(RegraNegocioException.class, () -> sistema.alterarConexao(1, 2, 9, 9, true));
        assertEquals(new Aresta(1, 2, 5, 10), sistema.getGrafo().obterAresta(1, 2).get()); // nada mudou
    }

    @Test
    void alterarConexaoComPesoInvalidoNaoMudaNenhumSentido() {
        SistemaCidades sistema = sistemaCom(1, 2);
        sistema.conectar(1, 2, 5, 10, true);

        assertThrows(RegraNegocioException.class, () -> sistema.alterarConexao(1, 2, -1, 10, true));

        assertEquals(new Aresta(1, 2, 5, 10), sistema.getGrafo().obterAresta(1, 2).get());
        assertEquals(new Aresta(2, 1, 5, 10), sistema.getGrafo().obterAresta(2, 1).get());
    }

    @Test
    void desconectarRemoveUmSentidoOuOsDois() {
        SistemaCidades sistema = sistemaCom(1, 2);
        sistema.conectar(1, 2, 5, 10, true);

        sistema.desconectar(1, 2, false);
        assertFalse(sistema.existeConexao(1, 2));
        assertTrue(sistema.existeConexao(2, 1));

        sistema.conectar(1, 2, 5, 10, false);
        sistema.desconectar(1, 2, true);
        assertEquals(0, sistema.getGrafo().quantidadeArestas());
    }

    @Test
    void desconectarConexaoInexistenteLancaErro() {
        SistemaCidades sistema = sistemaCom(1, 2);
        assertThrows(RegraNegocioException.class, () -> sistema.desconectar(1, 2, false));
        sistema.conectar(1, 2, 5, 10, false);
        assertThrows(RegraNegocioException.class, () -> sistema.desconectar(1, 2, true)); // sem volta
        assertTrue(sistema.existeConexao(1, 2)); // nada foi removido
    }

    // ---------------- remoção de localidade ----------------

    @Test
    void removerLocalidadeRemoveTambemAsConexoesDeEntradaESaida() {
        SistemaCidades sistema = sistemaCom(1, 2, 3);
        sistema.conectar(1, 2, 5, 5, true);   // 1<->2 (2 arestas tocam o 2)
        sistema.conectar(2, 3, 5, 5, false);  // 2->3 (sai do 2)
        sistema.conectar(3, 2, 5, 5, false);  // 3->2 (chega no 2)  -> 4 arestas tocam o 2
        sistema.conectar(1, 3, 5, 5, false);  // não toca o 2

        assertEquals(5, sistema.getGrafo().quantidadeArestas());
        assertEquals(4, sistema.quantidadeConexoes(2));

        int removidas = sistema.removerLocalidade(2);

        assertEquals(4, removidas);
        assertFalse(sistema.getArvore().contem(2));
        assertFalse(sistema.getGrafo().contemVertice(2));
        assertEquals(List.of(new Aresta(1, 3, 5, 5)), sistema.getGrafo().arestas());
        assertTrue(sistema.verificarConsistencia().isEmpty());
    }

    @Test
    void removerLocalidadeInexistenteLancaErro() {
        SistemaCidades sistema = sistemaCom(1);
        assertThrows(RegraNegocioException.class, () -> sistema.removerLocalidade(99));
        assertThrows(RegraNegocioException.class, () -> sistema.quantidadeConexoes(99));
        assertEquals(1, sistema.getArvore().quantidadeNos());
    }

    @Test
    void depoisDeRemoverPodeCadastrarOMesmoIdDeNovoSemConexoesAntigas() {
        SistemaCidades sistema = sistemaCom(1, 2);
        sistema.conectar(1, 2, 5, 5, true);
        sistema.removerLocalidade(2);

        sistema.cadastrarLocalidade(2, "Nova", TipoLocalidade.BAIRRO, 1);

        assertEquals(0, sistema.getGrafo().quantidadeArestas()); // a nova não herda as conexões
        assertTrue(sistema.estaConsistente());
    }

    // ---------------- balanceamento ----------------

    @Test
    void balancearArvoreNaoAlteraOGrafo() {
        SistemaCidades sistema = new SistemaCidades();
        DadosTeste.carregar(sistema);
        sistema.conectar(50, 30, 12.5, 20, true);
        sistema.conectar(70, 80, 40, 35, true);
        sistema.conectar(20, 10, 8, 12, false);
        List<Aresta> arestasAntes = sistema.getGrafo().arestas();
        List<Integer> verticesAntes = sistema.getGrafo().vertices();

        ResultadoBalanceamento resultado = sistema.balancearArvore();

        assertFalse(resultado.estavaBalanceada());
        assertEquals(5, resultado.alturaAntes());
        assertEquals(3, resultado.alturaDepois());
        assertTrue(sistema.getArvore().estaBalanceada());
        assertEquals(arestasAntes, sistema.getGrafo().arestas());
        assertEquals(verticesAntes, sistema.getGrafo().vertices());
        assertTrue(sistema.estaConsistente());
        // e as consultas continuam funcionando depois de reconstruir
        assertEquals("Formosa", sistema.nomeDe(80));
        assertTrue(sistema.existeConexao(50, 30));
    }

    // ---------------- buscas ----------------

    @Test
    void buscasExigemQueAOrigemSejaUmaLocalidadeCadastrada() {
        SistemaCidades sistema = sistemaCom(1, 2);
        assertEquals("Não existe localidade cadastrada com o ID 99.", assertThrows(
                RegraNegocioException.class, () -> sistema.buscaEmLargura(99)).getMessage());
        assertThrows(RegraNegocioException.class, () -> sistema.buscaEmProfundidade(99));
    }

    @Test
    void buscasSeguemAsConexoesDoSistema() {
        SistemaCidades sistema = sistemaCom(1, 2, 3, 4);
        sistema.conectar(1, 2, 5, 5, true);
        sistema.conectar(2, 3, 5, 5, false);

        ResultadoBusca largura = sistema.buscaEmLargura(1);
        ResultadoBusca profundidade = sistema.buscaEmProfundidade(1);

        assertEquals(List.of(1, 2, 3), largura.ordemVisita());
        assertEquals(List.of(1, 2, 3), profundidade.ordemVisita());
        assertEquals(List.of(4), largura.naoAlcancados());
        assertEquals(List.of(3), sistema.buscaEmLargura(3).ordemVisita()); // 3 não tem saída
    }

    @Test
    void depoisDeRemoverUmaLocalidadeAsBuscasNaoPassamMaisPorEla() {
        SistemaCidades sistema = sistemaCom(1, 2, 3);
        sistema.conectar(1, 2, 5, 5, true);
        sistema.conectar(2, 3, 5, 5, true);
        sistema.removerLocalidade(2);

        ResultadoBusca r = sistema.buscaEmLargura(1);

        assertEquals(List.of(1), r.ordemVisita());
        assertEquals(List.of(3), r.naoAlcancados());
    }

    // ---------------- consistência ----------------

    @Test
    void depoisDeUmaSequenciaDeOperacoesTudoContinuaConsistente() {
        SistemaCidades sistema = new SistemaCidades();
        DadosTeste.carregar(sistema);
        sistema.conectar(50, 30, 12.5, 20, true);
        sistema.conectar(30, 20, 9, 15, false);
        sistema.atualizarLocalidade(30, "Valparaíso", TipoLocalidade.CIDADE, 200000);
        sistema.removerLocalidade(20);
        sistema.cadastrarLocalidade(21, "Nova Cidade", TipoLocalidade.CIDADE, 5000);
        sistema.conectar(21, 50, 30, 40, true);
        sistema.balancearArvore();
        sistema.desconectar(50, 30, true);

        assertTrue(sistema.verificarConsistencia().isEmpty());
        assertEquals(13, sistema.listarLocalidades().size());
    }

    @Test
    void verificarConsistenciaDetectaProblemasNoSistemaMontadoAMao() {
        ArvoreBinariaBusca arvore = new ArvoreBinariaBusca();
        arvore.inserir(new Localidade(1, "A", TipoLocalidade.CIDADE, 1));
        arvore.inserir(new Localidade(2, "B", TipoLocalidade.CIDADE, 1));
        Grafo grafo = new Grafo();
        grafo.adicionarVertice(2);
        grafo.adicionarVertice(3); // vértice 3 não tem localidade
        grafo.adicionarAresta(new Aresta(2, 3, 1, 1)); // aresta para localidade inexistente
        SistemaCidades sistema = new SistemaCidades(arvore, grafo);

        List<String> problemas = sistema.verificarConsistencia();

        assertEquals(3, problemas.size());
        assertTrue(problemas.contains("A localidade 1 está na árvore, mas não tem vértice no grafo."));
        assertTrue(problemas.contains("O vértice 3 não tem localidade cadastrada na árvore."));
        assertTrue(problemas.contains("A conexão 2 -> 3 usa uma localidade inexistente."));
        assertFalse(sistema.estaConsistente());
    }
}