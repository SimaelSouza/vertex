package br.ifg.vertex.grafo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.ifg.vertex.modelo.Aresta;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ConectividadeTest {

    private static Grafo grafoCom(int quantidade) {
        Grafo grafo = new Grafo();
        for (int id = 1; id <= quantidade; id++) {
            grafo.adicionarVertice(id);
        }
        return grafo;
    }

    private static void via(Grafo grafo, int a, int b) { // mão dupla
        grafo.adicionarConexaoBidirecional(a, b, 1, 1);
    }

    private static void unica(Grafo grafo, int a, int b) { // mão única
        grafo.adicionarAresta(new Aresta(a, b, 1, 1));
    }

    // ---------------- componentes ----------------

    @Test
    void redeVaziaNaoTemComponentesNemEConexa() {
        Grafo vazio = new Grafo();
        assertTrue(Conectividade.componentes(vazio).isEmpty());
        assertFalse(Conectividade.redeConexa(vazio));
    }

    @Test
    void cadaVerticeSemViasEUmaComponente() {
        assertEquals(List.of(List.of(1), List.of(2), List.of(3)), Conectividade.componentes(grafoCom(3)));
    }

    @Test
    void componentesSaemOrdenadasPorIdEPeloMenorId() {
        Grafo grafo = grafoCom(6);
        via(grafo, 6, 2);
        via(grafo, 4, 1);
        unica(grafo, 4, 5);

        assertEquals(List.of(List.of(1, 4, 5), List.of(2, 6), List.of(3)), Conectividade.componentes(grafo));
    }

    @Test
    void projecaoNaoDirecionadaIgnoraOSentidoDasVias() {
        Grafo grafo = grafoCom(3);
        unica(grafo, 1, 2); // 1 chega em 2, mas 2 não chega em 1
        unica(grafo, 3, 2);

        assertEquals(List.of(List.of(1, 2, 3)), Conectividade.componentes(grafo));
        assertTrue(Conectividade.redeConexa(grafo));
    }

    @Test
    void redeComDuasPartesNaoEConexa() {
        Grafo grafo = grafoCom(4);
        via(grafo, 1, 2);
        via(grafo, 3, 4);
        assertFalse(Conectividade.redeConexa(grafo));
        assertEquals(2, Conectividade.componentes(grafo).size());
    }

    @Test
    void umUnicoVerticeJaEConexo() {
        assertTrue(Conectividade.redeConexa(grafoCom(1)));
    }

    // ---------------- ciclos ----------------

    @Test
    void semArestasOuSoParesDeMaoDuplaNaoHaCiclo() {
        assertTrue(Conectividade.ciclos(grafoCom(3)).isEmpty());

        Grafo grafo = grafoCom(3);
        via(grafo, 1, 2); // ir e voltar pela mesma via não conta
        assertTrue(Conectividade.ciclos(grafo).isEmpty());
        via(grafo, 2, 3); // cadeia 1=2=3 continua sem ciclo de 3 vértices
        assertTrue(Conectividade.ciclos(grafo).isEmpty());
    }

    @Test
    void cicloDeMaoUnicaComTresVertices() {
        Grafo grafo = grafoCom(3);
        unica(grafo, 3, 1);
        unica(grafo, 1, 2);
        unica(grafo, 2, 3);

        assertEquals(List.of(List.of(1, 2, 3)), Conectividade.ciclos(grafo));
    }

    @Test
    void trianguloDeViasDeMaoDuplaTemCiclo() {
        Grafo grafo = grafoCom(3);
        via(grafo, 1, 2);
        via(grafo, 2, 3);
        via(grafo, 1, 3);

        assertFalse(Conectividade.ciclos(grafo).isEmpty());
    }

    @Test
    void caminhoSemVoltaNaoTemCiclo() {
        Grafo grafo = grafoCom(4);
        unica(grafo, 1, 2);
        unica(grafo, 2, 3);
        unica(grafo, 1, 3);
        unica(grafo, 3, 4);
        assertTrue(Conectividade.ciclos(grafo).isEmpty());
    }

    @Test
    void cicloQuePassaPorViaDeMaoDuplaNaoEscapaMesmoDependendoDaOrdemDosIds() {
        // 1=3 (mão dupla), 1->2, 2->3: o ciclo 1 -> 2 -> 3 -> 1 existe
        Grafo grafo = grafoCom(3);
        via(grafo, 1, 3);
        unica(grafo, 1, 2);
        unica(grafo, 2, 3);
        assertEquals(List.of(List.of(1, 2, 3)), Conectividade.ciclos(grafo));

        // o mesmo desenho com os IDs trocados (3 como início) também precisa achar
        Grafo outro = grafoCom(3);
        via(outro, 3, 1);
        unica(outro, 3, 2);
        unica(outro, 2, 1);
        assertEquals(1, Conectividade.ciclos(outro).size());
    }

    @Test
    void cicloNaoSeRepeteEComecaPeloMenorId() {
        Grafo grafo = grafoCom(3);
        via(grafo, 1, 2);
        via(grafo, 2, 3);
        via(grafo, 1, 3);

        for (List<Integer> ciclo : Conectividade.ciclos(grafo)) {
            assertEquals(Integer.valueOf(1), ciclo.get(0));
        }
        assertEquals(new HashSet<>(Conectividade.ciclos(grafo)).size(), Conectividade.ciclos(grafo).size());
    }

    @Test
    void naoTravaEmCadeiaMuitoLonga() {
        Grafo grafo = grafoCom(20_000);
        for (int id = 1; id < 20_000; id++) {
            unica(grafo, id, id + 1);
        }
        unica(grafo, 20_000, 1); // fecha um ciclo gigante
        assertEquals(1, Conectividade.ciclos(grafo).size());
    }

    // ---------------- verificação independente (força bruta) ----------------

    private static boolean existeCicloDeTresOuMais(Grafo grafo) {
        for (int inicio : grafo.vertices()) {
            if (procurar(grafo, inicio, inicio, new HashSet<>(Set.of(inicio)), 1)) {
                return true;
            }
        }
        return false;
    }

    private static boolean procurar(Grafo grafo, int inicio, int atual, Set<Integer> usados, int tamanho) {
        for (int vizinho : grafo.vizinhos(atual)) {
            if (vizinho == inicio && tamanho >= 3) {
                return true;
            }
            if (usados.add(vizinho)) {
                boolean achou = procurar(grafo, inicio, vizinho, usados, tamanho + 1);
                usados.remove(vizinho);
                if (achou) {
                    return true;
                }
            }
        }
        return false;
    }

    @Test
    void concordaComForcaBrutaEmCentenasDeGrafosAleatorios() {
        Random sorteio = new Random(2026);
        int comCiclo = 0;
        for (int tentativa = 0; tentativa < 500; tentativa++) {
            int n = 3 + sorteio.nextInt(4); // 3 a 6 vértices
            Grafo grafo = grafoCom(n);
            for (int a = 1; a <= n; a++) {
                for (int b = 1; b <= n; b++) {
                    if (a != b && sorteio.nextDouble() < 0.3) {
                        unica(grafo, a, b); // pares de mão dupla surgem por acaso
                    }
                }
            }

            List<List<Integer>> ciclos = Conectividade.ciclos(grafo);

            assertEquals(existeCicloDeTresOuMais(grafo), !ciclos.isEmpty(), "grafo " + tentativa);
            for (List<Integer> ciclo : ciclos) {
                assertTrue(ciclo.size() >= 3, "tamanho");
                assertEquals(ciclo.size(), new HashSet<>(ciclo).size(), "vértices distintos");
                for (int i = 0; i < ciclo.size(); i++) {
                    int de = ciclo.get(i);
                    int para = ciclo.get((i + 1) % ciclo.size());
                    assertTrue(grafo.existeAresta(de, para), "aresta " + de + "->" + para);
                }
            }
            if (!ciclos.isEmpty()) {
                comCiclo++;
            }
        }
        assertTrue(comCiclo > 50 && comCiclo < 500); // o sorteio exercitou os dois casos
    }
}