package br.ifg.vertex.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.ifg.vertex.modelo.Localidade;
import br.ifg.vertex.modelo.TipoLocalidade;
import br.ifg.vertex.servico.DadosTeste;
import br.ifg.vertex.servico.SistemaCidades;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * Testes de ponta a ponta do menu: um "teclado" simulado digita as linhas e o teste
 * confere o estado do sistema e o que apareceu na tela.
 */
class MenuPrincipalTest {

    private static SistemaCidades sistemaDeTeste() {
        SistemaCidades sistema = new SistemaCidades();
        DadosTeste.carregar(sistema);
        return sistema;
    }

    /** Roda o menu digitando as linhas dadas e devolve tudo o que foi escrito na tela. */
    private static String rodar(SistemaCidades sistema, String... linhas) {
        PrintStream original = System.out;
        ByteArrayOutputStream capturado = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturado, true, StandardCharsets.UTF_8));
        try {
            String texto = String.join("\n", linhas) + "\n";
            Entrada entrada = new Entrada(new ByteArrayInputStream(texto.getBytes(StandardCharsets.UTF_8)));
            new MenuPrincipal(entrada, sistema).executar();
        } finally {
            System.setOut(original);
        }
        return capturado.toString(StandardCharsets.UTF_8);
    }

    private static int contar(String texto, String trecho) {
        return texto.split(java.util.regex.Pattern.quote(trecho), -1).length - 1;
    }

    // ---------------- localidades ----------------

    @Test
    void cadastrarPeloMenuCriaNaArvoreENoGrafo() {
        SistemaCidades sistema = sistemaDeTeste();

        String tela = rodar(sistema, "1", "100", "Teste City", "1", "5000", "0", "s");

        assertTrue(tela.contains("[OK] Localidade cadastrada"));
        assertTrue(sistema.getArvore().contem(100));
        assertTrue(sistema.getGrafo().contemVertice(100));
        assertTrue(sistema.estaConsistente());
    }

    @Test
    void entradasInvalidasPedemDeNovoENuncaDerrubamOPrograma() {
        SistemaCidades sistema = sistemaDeTeste();

        // opção "abc" e "99" inválidas; id "-5" e "xyz" inválidos; tipo "9" e população "-1" inválidos
        String tela = rodar(sistema, "abc", "99", "1", "-5", "xyz", "101", "Bairro Novo", "9", "2", "-1", "300",
                "0", "s");

        assertEquals(6, contar(tela, "[ERRO]"));
        Localidade nova = sistema.getArvore().pesquisar(101).get();
        assertEquals("Bairro Novo", nova.getNome());
        assertEquals(TipoLocalidade.BAIRRO, nova.getTipo());
        assertEquals(300, nova.getPopulacao());
        assertTrue(tela.contains("[OK] Programa encerrado"));
    }

    @Test
    void cadastrarIdRepetidoMostraErroLogoDepoisDoId() {
        SistemaCidades sistema = sistemaDeTeste();

        String tela = rodar(sistema, "1", "50", "0", "s");

        assertTrue(tela.contains("[ERRO] Já existe uma localidade com o ID 50."));
        assertEquals(13, sistema.listarLocalidades().size());
    }

    @Test
    void pesquisarMostraNivelEComparacoes() {
        String tela = rodar(sistemaDeTeste(), "2", "3", "0", "s");

        assertTrue(tela.contains("[OK] Localidade encontrada."));
        assertTrue(tela.contains("5 (a raiz é o nível 0)"));
        assertTrue(tela.contains("Comparações na busca: 6"));
    }

    @Test
    void pesquisarIdInexistenteMostraAviso() {
        String tela = rodar(sistemaDeTeste(), "2", "999", "0", "s");
        assertTrue(tela.contains("[AVISO] Nenhuma localidade encontrada com o ID 999"));
    }

    @Test
    void atualizarComEnterVazioMantemOCampoAtual() {
        SistemaCidades sistema = sistemaDeTeste();

        // nome: Enter (mantém); tipo 2 (Bairro); população 999
        rodar(sistema, "3", "40", "", "2", "999", "0", "s");

        Localidade l = sistema.getArvore().pesquisar(40).get();
        assertEquals("Santo Antônio do Descoberto", l.getNome());
        assertEquals(TipoLocalidade.BAIRRO, l.getTipo());
        assertEquals(999, l.getPopulacao());
    }

    @Test
    void removerPeloMenuTiraALocalidadeEAsConexoes() {
        SistemaCidades sistema = sistemaDeTeste();
        sistema.conectar(50, 30, 12.5, 20, true);
        sistema.conectar(50, 70, 20, 25, false);

        String tela = rodar(sistema, "4", "50", "s", "0", "s");

        assertTrue(tela.contains("[CONFIRMAR]"));
        assertTrue(tela.contains("junto com 3 conexões"));
        assertFalse(sistema.getArvore().contem(50));
        assertFalse(sistema.getGrafo().contemVertice(50));
        assertEquals(0, sistema.getGrafo().quantidadeArestas());
        assertTrue(sistema.estaConsistente());
    }

    @Test
    void removerCancelandoNaoMudaNada() {
        SistemaCidades sistema = sistemaDeTeste();

        String tela = rodar(sistema, "4", "50", "n", "0", "s");

        assertTrue(tela.contains("[AVISO] Remoção cancelada."));
        assertTrue(sistema.getArvore().contem(50));
    }

    // ---------------- árvore ----------------

    @Test
    void visualizarMostraODesenhoEOResumo() {
        String tela = rodar(sistemaDeTeste(), "5", "1", "0", "0", "s");

        assertTrue(tela.contains("50 (Luziânia)"));
        assertTrue(tela.contains("Altura:           5"));
        assertTrue(tela.contains("Balanceada:       não"));
    }

    @Test
    void percursoEmOrdemSaiCrescente() {
        String tela = rodar(sistemaDeTeste(), "6", "2", "0", "0", "s");
        assertTrue(tela.contains("Em ordem (esquerda, nó, direita): 3, 5, 10, 15, 20, 30, 40, 50, 60, 70, 80, 90, 95"));
    }

    @Test
    void analisarBalanceamentoApontaOsNosRuins() {
        String tela = rodar(sistemaDeTeste(), "7", "1", "0", "0", "s");
        assertEquals(4, contar(tela, "DESBALANCEADO"));
        assertTrue(tela.contains("4 nó(s) com diferença maior que 1"));
    }

    @Test
    void reconstruirPeloMenuBalanceiaAArvore() {
        SistemaCidades sistema = sistemaDeTeste();

        String tela = rodar(sistema, "7", "2", "s", "0", "0", "s");

        assertTrue(tela.contains("altura 5 -> 3"));
        assertEquals(3, sistema.getArvore().altura());
        assertTrue(sistema.getArvore().estaBalanceada());
    }

    @Test
    void estudoDeArmazenamentoMostraAsQuatroLinhas() {
        String tela = rodar(sistemaDeTeste(), "7", "3", "10", "0", "0", "s");

        assertTrue(tela.contains("79,37%"));
        assertTrue(tela.contains("13,33%"));
        assertTrue(tela.contains("1.023"));
    }

    // ---------------- conexões ----------------

    @Test
    void adicionarConexaoDeMaoDuplaPeloMenu() {
        SistemaCidades sistema = sistemaDeTeste();

        String tela = rodar(sistema, "8", "1", "50", "30", "12,5", "20", "s", "0", "0", "s");

        assertTrue(tela.contains("[OK] Via de mão dupla criada"));
        assertEquals(12.5, sistema.getGrafo().obterAresta(50, 30).get().distanciaKm());
        assertEquals(20, sistema.getGrafo().obterAresta(30, 50).get().tempoMin());
    }

    @Test
    void conectarLocalidadeInexistenteMostraErroEFicaNoSubmenu() {
        SistemaCidades sistema = sistemaDeTeste();

        String tela = rodar(sistema, "8", "1", "50", "999", "0", "0", "s");

        assertTrue(tela.contains("[ERRO] Não existe localidade cadastrada com o ID 999."));
        assertTrue(tela.contains("[OK] Programa encerrado"));
        assertEquals(0, sistema.getGrafo().quantidadeArestas());
    }

    @Test
    void conexaoDuplicadaMostraErro() {
        SistemaCidades sistema = sistemaDeTeste();
        sistema.conectar(50, 30, 12.5, 20, false);

        String tela = rodar(sistema, "8", "1", "50", "30", "5", "5", "n", "0", "0", "s");

        assertTrue(tela.contains("[ERRO] Já existe uma conexão de 50 (Luziânia) para 30 (Valparaíso de Goiás)."));
        assertEquals(1, sistema.getGrafo().quantidadeArestas());
    }

    @Test
    void removerConexaoComVoltaPerguntaERemoveAsDuas() {
        SistemaCidades sistema = sistemaDeTeste();
        sistema.conectar(50, 30, 12.5, 20, true);

        rodar(sistema, "8", "2", "50", "30", "s", "0", "0", "s");

        assertEquals(0, sistema.getGrafo().quantidadeArestas());
    }

    @Test
    void alterarPesosDasDuasDirecoesPeloMenu() {
        SistemaCidades sistema = sistemaDeTeste();
        sistema.conectar(50, 30, 12.5, 20, true);

        rodar(sistema, "8", "3", "50", "30", "s", "15,5", "25", "0", "0", "s");

        assertEquals(15.5, sistema.getGrafo().obterAresta(50, 30).get().distanciaKm());
        assertEquals(15.5, sistema.getGrafo().obterAresta(30, 50).get().distanciaKm());
        assertEquals(25, sistema.getGrafo().obterAresta(30, 50).get().tempoMin());
    }

    @Test
    void listagensDoGrafoFuncionamComEMesmoSemConexoes() {
        SistemaCidades sistema = sistemaDeTeste();
        sistema.conectar(50, 30, 12.5, 20, true);

        String tela = rodar(sistema, "8", "6", "7", "8", "9", "5", "50", "4", "50", "30", "0", "0", "s");

        assertTrue(tela.contains("Total: 13 vértices."));
        assertTrue(tela.contains("Total: 13 vértices e 2 arestas"));
        assertTrue(tela.contains("Vértices isolados"));
        assertTrue(tela.contains("50 (Luziânia) -> 30 (Valparaíso de Goiás): SIM [12,5 km, 20 min]"));
        assertTrue(tela.contains("1 vizinho(s) de 50 (Luziânia)."));
    }

    // ---------------- buscas ----------------

    /** 50<->30, 30<->20, 50<->70 (as demais localidades ficam sem via). */
    private static SistemaCidades sistemaComVias() {
        SistemaCidades sistema = sistemaDeTeste();
        sistema.conectar(50, 30, 12.5, 20, true);
        sistema.conectar(30, 20, 9, 15, true);
        sistema.conectar(50, 70, 40, 35, true);
        return sistema;
    }

    @Test
    void bfsPeloMenuMostraOrdemNiveisECaminho() {
        String tela = rodar(sistemaComVias(), "9", "50", "s", "20", "0", "s");

        assertTrue(tela.contains("Ordem de visita: 50, 30, 70, 20"));
        assertTrue(tela.contains("Alcançou 4 de 13 localidades."));
        assertTrue(tela.contains("[AVISO] Não alcançadas a partir daqui: 3, 5, 10, 15, 40, 60, 80, 90, 95"));
        assertTrue(tela.contains("Caminho encontrado (2 trecho(s)): 50 (Luziânia) -> 30 (Valparaíso de Goiás) -> 20 (Novo Gama)"));
    }

    @Test
    void dfsPeloMenuVaiFundoPrimeiro() {
        String tela = rodar(sistemaComVias(), "10", "50", "n", "0", "s");

        assertTrue(tela.contains("Ordem de visita: 50, 30, 20, 70"));
        assertTrue(tela.contains("Profundidade"));
        assertFalse(tela.contains("Caminho encontrado"));
    }

    @Test
    void buscaComDestinoSemCaminhoMostraAviso() {
        String tela = rodar(sistemaComVias(), "9", "50", "s", "10", "0", "s");
        assertTrue(tela.contains("[AVISO] Não há caminho de 50 (Luziânia) até 10 (Hospital Regional)."));
    }

    @Test
    void buscaComOrigemOuDestinoInexistenteMostraErro() {
        String tela = rodar(sistemaComVias(), "9", "999", "10", "50", "s", "888", "0", "s");

        assertEquals(2, contar(tela, "[ERRO] Não existe localidade cadastrada com o ID"));
        assertTrue(tela.contains("[OK] Programa encerrado"));
    }

    @Test
    void buscaEmLocalidadeSemViasVisitaSoElaMesma() {
        String tela = rodar(sistemaDeTeste(), "9", "95", "n", "0", "s");
        assertTrue(tela.contains("Ordem de visita: 95"));
        assertTrue(tela.contains("Alcançou 1 de 13 localidades."));
    }

    // ---------------- rotas ----------------

    private static SistemaCidades sistemaCompleto() {
        SistemaCidades sistema = new SistemaCidades();
        DadosTeste.carregarTudo(sistema);
        return sistema;
    }

    @Test
    void menorRotaPorDistanciaMostraOsTrechosEOsTotais() {
        String tela = rodar(sistemaCompleto(), "11", "1", "20", "70", "0", "0", "s");

        assertTrue(tela.contains("[OK] Rota de menor distância encontrada."));
        assertTrue(tela.contains("Caminho: 20 (Novo Gama) -> 10 (Hospital Regional) -> 50 (Luziânia) -> 70 (Cidade Ocidental)"));
        assertTrue(tela.contains("20 (Novo Gama) -> 10 (Hospital Regional) [14 km, 12 min]"));
        assertTrue(tela.contains("Total: 39,8 km e 69 min em 3 trecho(s)."));
    }

    @Test
    void menorRotaPorTempoEscolheOutroCaminho() {
        String tela = rodar(sistemaCompleto(), "11", "2", "20", "70", "0", "0", "s");

        assertTrue(tela.contains("[OK] Rota de menor tempo encontrada."));
        assertTrue(tela.contains("Total: 52 km e 38 min em 3 trecho(s)."));
    }

    @Test
    void rotaParaDestinoInalcancavelMostraAviso() {
        String tela = rodar(sistemaCompleto(), "11", "1", "50", "90", "0", "0", "s");
        assertTrue(tela.contains("[AVISO] Não existe rota de 50 (Luziânia) até 90 (IFG (campus)) seguindo o sentido das vias."));
    }

    @Test
    void rotaComOrigemIgualAoDestinoAvisaEMostraCustoZero() {
        String tela = rodar(sistemaCompleto(), "11", "1", "50", "50", "0", "0", "s");
        assertTrue(tela.contains("[AVISO] A origem e o destino são a mesma localidade."));
    }

    @Test
    void rotaComIdInexistenteMostraErroEContinuaNoSubmenu() {
        String tela = rodar(sistemaCompleto(), "11", "1", "999", "1", "50", "888", "0", "0", "s");

        assertEquals(2, contar(tela, "[ERRO] Não existe localidade cadastrada com o ID"));
        assertTrue(tela.contains("[OK] Programa encerrado"));
    }

    @Test
    void compararRotasMostraATabelaComOsTresCaminhosDiferentes() {
        String tela = rodar(sistemaCompleto(), "13", "20", "70", "0", "s");

        assertTrue(tela.contains("Menos trechos (BFS)"));
        assertTrue(tela.contains("72,5 km"));
        assertTrue(tela.contains("105 min"));
        assertTrue(tela.contains("39,8 km"));
        assertTrue(tela.contains("38 min"));
        assertTrue(tela.contains("[OK] Os três critérios escolheram caminhos diferentes"));
    }

    @Test
    void compararRotasInformaQuandoDoisCriteriosCoincidemOuTodos() {
        assertTrue(rodar(sistemaCompleto(), "13", "3", "70", "0", "s").contains("BFS = menor tempo"));
        assertTrue(rodar(sistemaCompleto(), "13", "50", "30", "0", "s")
                .contains("[OK] Os três critérios escolheram o mesmo caminho neste par."));
    }

    @Test
    void compararRotasSemCaminhoMostraAviso() {
        String tela = rodar(sistemaCompleto(), "13", "50", "95", "0", "s");
        assertTrue(tela.contains("[AVISO] Não existe rota de 50 (Luziânia) até 95 (Bairro isolado (sem vias))"));
    }

    // ---------------- conectividade e relatórios ----------------

    @Test
    void conectividadeMostraComponentesERedeNaoConexa() {
        String tela = rodar(sistemaCompleto(), "12", "3", "4", "0", "0", "s");

        assertTrue(tela.contains("1) 3, 5, 10, 15, 20, 30, 40, 50, 70, 90  (10 localidade(s))"));
        assertTrue(tela.contains("[OK] Total: 3 componente(s)."));
        assertTrue(tela.contains("[AVISO] A rede NÃO é conexa: ela tem 3 componentes separadas"));
    }

    @Test
    void conectividadeMostraIsoladosEAlcancabilidadeAssimetrica() {
        String tela = rodar(sistemaCompleto(), "12", "1", "2", "90", "15", "0", "0", "s");

        assertTrue(tela.contains("95 (Bairro isolado (sem vias))"));
        assertTrue(tela.contains("90 (IFG (campus)) chega em 15 (Centro): SIM"));
        assertTrue(tela.contains("15 (Centro) chega em 90 (IFG (campus)): NÃO"));
        assertTrue(tela.contains("[AVISO] A alcançabilidade é assimétrica"));
    }

    @Test
    void conectividadeDetectaCiclosEInformaQuandoNaoHa() {
        assertTrue(rodar(sistemaCompleto(), "12", "5", "0", "0", "s").contains("3 -> 15 -> 5 -> 3"));
        assertTrue(rodar(sistemaDeTeste(), "12", "5", "0", "0", "s").contains("[OK] Nenhum ciclo"));
    }

    @Test
    void conectividadeComIdInexistenteMostraErro() {
        String tela = rodar(sistemaCompleto(), "12", "2", "999", "0", "0", "s");
        assertTrue(tela.contains("[ERRO] Não existe localidade cadastrada com o ID 999."));
    }

    @Test
    void dfsMostraOsCiclosAoAlcanceDaOrigem() {
        String tela = rodar(sistemaCompleto(), "10", "50", "n", "0", "s");
        assertTrue(tela.contains("[AVISO] Há ciclos ao alcance da origem"));
        assertTrue(tela.contains("3 -> 15 -> 5 -> 3"));

        String semCiclo = rodar(sistemaComVias(), "10", "50", "n", "0", "s");
        assertTrue(semCiclo.contains("Ciclos ao alcance da origem: nenhum"));
    }

    @Test
    void relatoriosAbCPeloMenu() {
        String tela = rodar(sistemaCompleto(), "14", "1", "2", "3", "0", "0", "s");

        assertTrue(tela.contains("RELATÓRIO A - CADASTRO DE LOCALIDADES"));
        assertTrue(tela.contains("RELATÓRIO B - ESTRUTURA DA ÁRVORE"));
        assertTrue(tela.contains("RELATÓRIO C - REDE DE TRANSPORTES"));
    }

    @Test
    void relatorioDPeloMenuPedeOsDoisIds() {
        String tela = rodar(sistemaCompleto(), "14", "4", "20", "70", "0", "0", "s");
        assertTrue(tela.contains("RELATÓRIO D - ROTAS"));
        assertTrue(tela.contains("Os três critérios escolheram caminhos diferentes"));
    }

    @Test
    void todosOsRelatoriosDeUmaVez() {
        String tela = rodar(sistemaCompleto(), "14", "5", "20", "70", "0", "0", "s");

        for (String titulo : new String[] {"RELATÓRIO A", "RELATÓRIO B", "RELATÓRIO C", "RELATÓRIO D"}) {
            assertTrue(tela.contains(titulo), titulo);
        }
    }

    @Test
    void relatoriosNaoQuebramComSistemaVazio() {
        String tela = rodar(new SistemaCidades(), "14", "1", "2", "3", "0", "0", "s");

        assertTrue(tela.contains("Nenhuma localidade cadastrada."));
        assertTrue(tela.contains("A árvore está vazia."));
        assertTrue(tela.contains("A rede está vazia"));
    }

    // ---------------- geral ----------------

    @Test
    void autotestePeloMenuMostraOkParaCadaCasoEResumo() {
        SistemaCidades sistema = sistemaCompleto();
        int localidadesAntes = sistema.listarLocalidades().size();

        String tela = rodar(sistema, "15", "1", "0", "0", "s");

        assertTrue(tela.contains("--- Árvore ---"));
        assertTrue(tela.contains("--- Entrada ---"));
        assertTrue(tela.contains("[OK] Todos os 36 testes passaram."));
        assertFalse(tela.contains("[FALHA]"));
        assertTrue(tela.contains("Consistência dos dados cadastrados agora: OK"));
        assertEquals(localidadesAntes, sistema.listarLocalidades().size()); // os dados do usuário não mudam
    }

    @Test
    void recarregarDadosDeExemploRestauraAsLocalidadesEConexoes() {
        SistemaCidades sistema = sistemaCompleto();
        sistema.removerLocalidade(50);
        sistema.cadastrarLocalidade(777, "Extra", TipoLocalidade.BAIRRO, 1);

        String tela = rodar(sistema, "15", "2", "s", "0", "0", "s");

        assertTrue(tela.contains("[OK] 13 localidades e 29 conexões de exemplo carregadas."));
        assertTrue(sistema.getArvore().contem(50));
        assertFalse(sistema.getArvore().contem(777));
        assertEquals(29, sistema.getGrafo().quantidadeArestas());
        assertTrue(sistema.estaConsistente());
    }

    @Test
    void recusarARecargaNaoMudaNada() {
        SistemaCidades sistema = sistemaCompleto();
        sistema.cadastrarLocalidade(777, "Extra", TipoLocalidade.BAIRRO, 1);

        String tela = rodar(sistema, "15", "2", "n", "0", "0", "s");

        assertTrue(tela.contains("[AVISO] Operação cancelada. Nada foi alterado."));
        assertTrue(sistema.getArvore().contem(777));
    }

    @Test
    void sairPedeConfirmacaoERecusarVoltaAoMenu() {
        String tela = rodar(sistemaDeTeste(), "0", "n", "0", "s");
        assertEquals(2, contar(tela, "Deseja realmente sair?"));
    }

    @Test
    void fimDaEntradaNoMeioDeUmaOperacaoEncerraSemTravar() {
        SistemaCidades sistema = sistemaDeTeste();
        assertThrows(EntradaEncerradaException.class, () -> rodar(sistema, "1", "100"));
        assertFalse(sistema.getArvore().contem(100)); // nada ficou pela metade
    }
}