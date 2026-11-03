package br.ifg.vertex.servico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.ifg.vertex.grafo.CriterioPeso;
import br.ifg.vertex.grafo.ResultadoCaminho;
import br.ifg.vertex.modelo.Aresta;
import br.ifg.vertex.modelo.TipoLocalidade;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Testa as conexões de teste e as rotas calculadas sobre elas (os cenários da demonstração). */
class RotasDeTesteTest {

    private static SistemaCidades completo() {
        SistemaCidades sistema = new SistemaCidades();
        DadosTeste.carregarTudo(sistema);
        return sistema;
    }

    // ---------------- as conexões de teste ----------------

    @Test
    void conexoesDeTesteSaoConsistentesETemOsCenariosPedidos() {
        SistemaCidades s = completo();

        assertEquals(13, s.getGrafo().quantidadeVertices());
        assertEquals(29, s.getGrafo().quantidadeArestas());
        assertTrue(s.estaConsistente());
        assertEquals(List.of(95), s.getGrafo().verticesIsolados());
    }

    @Test
    void temViasDeMaoUnicaEIdaSemVolta() {
        SistemaCidades s = completo();
        assertTrue(s.existeConexao(90, 15));
        assertFalse(s.existeConexao(15, 90));
        assertTrue(s.existeConexao(40, 50));
        assertFalse(s.existeConexao(50, 40));
        assertTrue(s.menorRota(90, 15, CriterioPeso.TEMPO).existe());
        assertFalse(s.menorRota(15, 90, CriterioPeso.TEMPO).existe()); // alcançabilidade assimétrica
    }

    @Test
    void temUmCicloSoDeMaoUnicaComTresVertices() {
        SistemaCidades s = completo();
        assertTrue(s.existeConexao(3, 15) && s.existeConexao(15, 5) && s.existeConexao(5, 3));
        assertFalse(s.existeConexao(15, 3) || s.existeConexao(5, 15) || s.existeConexao(3, 5));
    }

    @Test
    void oHubTemMuitoMaisConexoesQueOsDemais() {
        SistemaCidades s = completo();
        int hub = s.quantidadeConexoes(50);
        assertEquals(11, hub);
        for (int id : s.getGrafo().vertices()) {
            if (id != 50) {
                assertTrue(s.quantidadeConexoes(id) < hub);
            }
        }
    }

    @Test
    void tempoNaoEProporcionalADistancia() {
        Set<Double> razoes = new HashSet<>();
        for (Aresta a : completo().getGrafo().arestas()) {
            razoes.add(Math.round(a.distanciaKm() / a.tempoMin() * 100) / 100.0);
        }
        assertTrue(razoes.size() > 10);
    }

    @Test
    void carregarSoAsLocalidadesNaoCriaConexoes() {
        SistemaCidades s = new SistemaCidades();
        DadosTeste.carregar(s);
        assertEquals(0, s.getGrafo().quantidadeArestas());
    }

    // ---------------- cenários de rota ----------------

    @Test
    void paraVinteSetentaOsTresCriteriosDaoCaminhosDiferentes() {
        ComparacaoRotas c = completo().compararRotas(20, 70);

        assertEquals(3, c.caminhosDistintos());
        assertEquals(List.of(20, 40, 70), c.menosTrechos().caminho());
        assertEquals(72.5, c.menosTrechos().distanciaKm(), 1e-9);
        assertEquals(105, c.menosTrechos().tempoMin());
        assertEquals(List.of(20, 10, 50, 70), c.menorDistancia().caminho());
        assertEquals(39.8, c.menorDistancia().distanciaKm(), 1e-9);
        assertEquals(69, c.menorDistancia().tempoMin());
        assertEquals(List.of(20, 10, 15, 70), c.menorTempo().caminho());
        assertEquals(52.0, c.menorTempo().distanciaKm(), 1e-9);
        assertEquals(38, c.menorTempo().tempoMin());
    }

    @Test
    void paraTresSetentaDoisCriteriosCoincidem() {
        ComparacaoRotas c = completo().compararRotas(3, 70);

        assertEquals(2, c.caminhosDistintos());
        assertTrue(c.bfsIgualTempo());
        assertFalse(c.bfsIgualDistancia());
        assertEquals(List.of(3, 15, 70), c.menosTrechos().caminho());
        assertEquals(List.of(3, 50, 70), c.menorDistancia().caminho());
    }

    @Test
    void paraCinquentaTrintaOsTresCoincidem() {
        ComparacaoRotas c = completo().compararRotas(50, 30);
        assertEquals(1, c.caminhosDistintos());
        assertEquals(List.of(50, 30), c.menorTempo().caminho());
    }

    @Test
    void destinosInalcancaveisNaoTemRotaEmNenhumCriterio() {
        SistemaCidades s = completo();
        for (int destino : new int[] {90, 60, 80, 95}) {
            ComparacaoRotas c = s.compararRotas(50, destino);
            assertFalse(c.existe());
            assertEquals(0, c.caminhosDistintos());
            assertFalse(c.menorDistancia().existe());
            assertFalse(c.menorTempo().existe());
        }
    }

    @Test
    void ogrupoDeCristalinaEFormosaSoAlcancaASiMesmo() {
        SistemaCidades s = completo();
        assertTrue(s.menorRota(60, 80, CriterioPeso.DISTANCIA).existe());
        assertEquals(95.5, s.menorRota(80, 60, CriterioPeso.DISTANCIA).distanciaKm(), 1e-9);
        assertFalse(s.menorRota(60, 20, CriterioPeso.DISTANCIA).existe());
    }

    // ---------------- verificação independente (força bruta) ----------------

    /** Enumera todos os caminhos simples e guarda o menor km e o menor tempo. */
    private static void forcaBruta(SistemaCidades s, int atual, int destino, Set<Integer> usados,
                                   double km, int min, double[] melhor) {
        if (atual == destino) {
            melhor[0] = Math.min(melhor[0], km);
            melhor[1] = Math.min(melhor[1], min);
            return;
        }
        for (Aresta a : s.getGrafo().arestasDeSaida(atual)) {
            if (usados.add(a.destino())) {
                forcaBruta(s, a.destino(), destino, usados, km + a.distanciaKm(), min + a.tempoMin(), melhor);
                usados.remove(a.destino());
            }
        }
    }

    @Test
    void dijkstraConcordaComForcaBrutaEmTodosOsParesDosDadosDeTeste() {
        SistemaCidades s = completo();
        List<Integer> vertices = s.getGrafo().vertices();
        int pares = 0;
        for (int origem : vertices) {
            for (int destino : vertices) {
                if (origem == destino) {
                    continue;
                }
                double[] melhor = {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
                Set<Integer> usados = new HashSet<>(List.of(origem));
                forcaBruta(s, origem, destino, usados, 0, 0, melhor);

                ResultadoCaminho porKm = s.menorRota(origem, destino, CriterioPeso.DISTANCIA);
                ResultadoCaminho porMin = s.menorRota(origem, destino, CriterioPeso.TEMPO);
                if (Double.isInfinite(melhor[0])) {
                    assertFalse(porKm.existe());
                    assertFalse(porMin.existe());
                } else {
                    assertEquals(melhor[0], porKm.distanciaKm(), 1e-9, "km " + origem + "->" + destino);
                    assertEquals(melhor[1], porMin.tempoMin(), 1e-9, "min " + origem + "->" + destino);
                }
                pares++;
            }
        }
        assertEquals(156, pares);
    }

    // ---------------- integração com as regras do sistema ----------------

    @Test
    void rotasExigemLocalidadesCadastradas() {
        SistemaCidades s = completo();
        assertEquals("Não existe localidade cadastrada com o ID 999.", assertThrows(RegraNegocioException.class,
                () -> s.menorRota(50, 999, CriterioPeso.TEMPO)).getMessage());
        assertThrows(RegraNegocioException.class, () -> s.menorRota(999, 50, CriterioPeso.DISTANCIA));
        assertThrows(RegraNegocioException.class, () -> s.rotaComMenosTrechos(999, 50));
        assertThrows(RegraNegocioException.class, () -> s.compararRotas(50, 999));
    }

    @Test
    void alterarUmaConexaoMudaAMelhorRota() {
        SistemaCidades s = completo();
        assertEquals(List.of(20, 10, 15, 70), s.menorRota(20, 70, CriterioPeso.TEMPO).caminho());

        s.alterarConexao(10, 15, 16.0, 300, true); // a via ficou lentíssima

        assertEquals(List.of(20, 10, 50, 70), s.menorRota(20, 70, CriterioPeso.TEMPO).caminho().subList(0, 4));
    }

    @Test
    void removerLocalidadeTiraElaDasRotas() {
        SistemaCidades s = completo();
        s.removerLocalidade(10);

        List<Integer> caminho = s.menorRota(20, 70, CriterioPeso.DISTANCIA).caminho();

        assertFalse(caminho.contains(10));
        assertTrue(s.estaConsistente());
    }

    @Test
    void novaLocalidadeConectadaJaApareceNasRotas() {
        SistemaCidades s = completo();
        s.cadastrarLocalidade(99, "Nova", TipoLocalidade.BAIRRO, 10);
        assertFalse(s.menorRota(50, 99, CriterioPeso.TEMPO).existe());

        s.conectar(50, 99, 2.0, 4, true);

        assertEquals(List.of(50, 99), s.menorRota(50, 99, CriterioPeso.TEMPO).caminho());
        List<Integer> todos = new ArrayList<>(s.getGrafo().vertices());
        assertTrue(todos.contains(99));
    }
}