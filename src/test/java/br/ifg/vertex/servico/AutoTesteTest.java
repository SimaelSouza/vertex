package br.ifg.vertex.servico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AutoTesteTest {

    @Test
    void todosOsCasosDeDominioPassam() {
        List<ResultadoTeste> resultados = AutoTeste.executar(AutoTeste.casos());

        assertEquals(28, resultados.size());
        for (ResultadoTeste r : resultados) {
            assertTrue(r.passou(), r.grupo() + " / " + r.nome() + ": " + r.detalhe());
        }
    }

    @Test
    void cobreTodasAsAreasDoPlanoDeTestes() {
        Set<String> grupos = new HashSet<>();
        for (CasoDeTeste caso : AutoTeste.casos()) {
            grupos.add(caso.grupo());
        }
        assertEquals(Set.of("Árvore", "Balanceamento", "Vetor", "Grafo", "Algoritmos", "Integração"), grupos);
    }

    @Test
    void casoQueFalhaViraFalhaComMotivoENaoImpedeOsOutros() {
        List<CasoDeTeste> casos = List.of(
                new CasoDeTeste("G", "passa", () -> Verifica.verdadeiro(true, "x")),
                new CasoDeTeste("G", "falha por verificação", () -> Verifica.igual(1, 2, "valor")),
                new CasoDeTeste("G", "falha por exceção", () -> {
                    throw new IllegalStateException("boom");
                }),
                new CasoDeTeste("G", "passa também", () -> { }));

        List<ResultadoTeste> r = AutoTeste.executar(casos);

        assertTrue(r.get(0).passou());
        assertFalse(r.get(1).passou());
        assertEquals("valor (esperado: 1; obtido: 2)", r.get(1).detalhe());
        assertFalse(r.get(2).passou());
        assertTrue(r.get(2).detalhe().contains("IllegalStateException"));
        assertTrue(r.get(3).passou());
    }

    @Test
    void autotesteNaoMexeEmDadosExternos() {
        SistemaCidades sistema = new SistemaCidades();
        DadosTeste.carregarTudo(sistema);
        AutoTeste.executar(AutoTeste.casos());
        assertEquals(13, sistema.listarLocalidades().size());
        assertEquals(29, sistema.getGrafo().quantidadeArestas());
    }

    @Test
    void verificaLancaQuandoDeve() {
        assertThrows(AssertionError.class, () -> Verifica.verdadeiro(false, "m"));
        assertThrows(AssertionError.class, () -> Verifica.igual("a", "b", "m"));
        assertThrows(AssertionError.class, () -> Verifica.lanca(IllegalArgumentException.class, () -> { }, "m"));
        assertThrows(AssertionError.class, () -> Verifica.lanca(IllegalArgumentException.class,
                () -> {
                    throw new IllegalStateException();
                }, "m"));
        Verifica.lanca(IllegalArgumentException.class, () -> {
            throw new IllegalArgumentException();
        }, "m");
    }

    @Test
    void recarregarDadosDeixaSistemaIgualAoInicial() {
        SistemaCidades sistema = new SistemaCidades();
        DadosTeste.carregarTudo(sistema);
        sistema.removerLocalidade(30);
        sistema.conectar(50, 60, 1, 1, true);

        DadosTeste.recarregar(sistema);

        assertEquals(13, sistema.listarLocalidades().size());
        assertEquals(29, sistema.getGrafo().quantidadeArestas());
        assertFalse(sistema.existeConexao(50, 60));
        assertTrue(sistema.estaConsistente());
    }
}