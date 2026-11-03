package br.ifg.vertex.servico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.ifg.vertex.modelo.Localidade;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DadosTesteTest {

    private static SistemaCidades carregado() {
        SistemaCidades sistema = new SistemaCidades();
        DadosTeste.carregar(sistema);
        return sistema;
    }

    @Test
    void carregaTrezeLocalidadesConsistentes() {
        SistemaCidades sistema = carregado();
        assertEquals(13, sistema.listarLocalidades().size());
        assertEquals(13, sistema.getGrafo().quantidadeVertices());
        assertTrue(sistema.estaConsistente());
    }

    @Test
    void aArvoreDeTesteNasceDesbalanceadaComAlturaCinco() {
        SistemaCidades sistema = carregado();
        assertEquals(5, sistema.getArvore().altura());
        assertFalse(sistema.getArvore().estaBalanceada());
        assertEquals(50, sistema.getArvore().raiz().get().getId());
    }

    @Test
    void naoHaConexoesAindaEntaoTodosOsVerticesSaoIsolados() {
        SistemaCidades sistema = carregado();
        assertEquals(0, sistema.getGrafo().quantidadeArestas());
        assertEquals(13, sistema.getGrafo().verticesIsolados().size());
    }

    @Test
    void populacoesSaoTodasDiferentesParaMaiorEMenorSeremUnicos() {
        Set<Integer> populacoes = new HashSet<>();
        for (Localidade l : carregado().listarLocalidades()) {
            populacoes.add(l.getPopulacao());
        }
        assertEquals(13, populacoes.size());
    }

    @Test
    void carregarDuasVezesViolaARegraDeIdRepetido() {
        SistemaCidades sistema = carregado();
        assertThrows(RegraNegocioException.class, () -> DadosTeste.carregar(sistema));
        assertEquals(13, sistema.listarLocalidades().size());
    }

    @Test
    void listaEmOrdemDeIdComOsExtremosEsperados() {
        List<Localidade> todas = carregado().listarLocalidades();
        assertEquals(3, todas.get(0).getId());
        assertEquals(95, todas.get(12).getId());
        assertEquals("Luziânia", carregado().nomeDe(50));
    }
}