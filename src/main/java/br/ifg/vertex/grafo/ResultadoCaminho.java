package br.ifg.vertex.grafo;

import br.ifg.vertex.modelo.Aresta;
import java.util.ArrayList;
import java.util.List;

/**
 * Um caminho entre duas localidades (ou a informação de que ele não existe).
 *
 * <p>Guarda sempre os DOIS totais (km e minutos), seja qual for o critério que escolheu o
 * caminho; assim dá para comparar a rota da BFS, a de menor distância e a de menor tempo
 * na mesma tabela.</p>
 *
 * @param origem      onde o caminho começa
 * @param destino     onde o caminho termina
 * @param existe      {@code false} se o destino não pode ser alcançado seguindo as vias
 * @param caminho     IDs dos vértices, da origem ao destino (vazio se não existe)
 * @param trechos     as arestas percorridas, na ordem (vazio se não existe ou origem = destino)
 * @param distanciaKm soma das distâncias dos trechos
 * @param tempoMin    soma dos tempos dos trechos
 */
public record ResultadoCaminho(
        int origem,
        int destino,
        boolean existe,
        List<Integer> caminho,
        List<Aresta> trechos,
        double distanciaKm,
        int tempoMin) {

    public ResultadoCaminho {
        caminho = List.copyOf(caminho);
        trechos = List.copyOf(trechos);
    }

    /** Resultado "não há caminho". */
    public static ResultadoCaminho inexistente(int origem, int destino) {
        return new ResultadoCaminho(origem, destino, false, List.of(), List.of(), 0, 0);
    }

    /** Monta o resultado a partir das arestas percorridas (os totais são somados aqui). */
    public static ResultadoCaminho deTrechos(int origem, int destino, List<Aresta> trechos) {
        List<Integer> vertices = new ArrayList<>();
        vertices.add(origem);
        double km = 0;
        int minutos = 0;
        for (Aresta trecho : trechos) {
            vertices.add(trecho.destino());
            km += trecho.distanciaKm();
            minutos += trecho.tempoMin();
        }
        return new ResultadoCaminho(origem, destino, true, vertices, trechos, km, minutos);
    }

    /** Quantidade de arestas percorridas (0 se a origem é o próprio destino). */
    public int quantidadeTrechos() {
        return trechos.size();
    }

    /** Custo do caminho segundo o critério dado (km ou minutos). */
    public double custo(CriterioPeso criterio) {
        return criterio == CriterioPeso.DISTANCIA ? distanciaKm : tempoMin;
    }
}