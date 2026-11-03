package br.ifg.vertex.servico;

import br.ifg.vertex.grafo.ResultadoCaminho;

/**
 * As três rotas entre o mesmo par de localidades: a com menos trechos (BFS), a de menor
 * distância e a de menor tempo (Dijkstra). Distância e tempo não são proporcionais, então
 * as três podem ser diferentes.
 */
public record ComparacaoRotas(
        ResultadoCaminho menosTrechos,
        ResultadoCaminho menorDistancia,
        ResultadoCaminho menorTempo) {

    /** Se há caminho entre as duas localidades, os três critérios o encontram. */
    public boolean existe() {
        return menosTrechos.existe();
    }

    public boolean bfsIgualDistancia() {
        return menosTrechos.caminho().equals(menorDistancia.caminho());
    }

    public boolean bfsIgualTempo() {
        return menosTrechos.caminho().equals(menorTempo.caminho());
    }

    public boolean distanciaIgualTempo() {
        return menorDistancia.caminho().equals(menorTempo.caminho());
    }

    /** Quantos caminhos diferentes os três critérios escolheram (1, 2 ou 3; 0 se não há caminho). */
    public int caminhosDistintos() {
        if (!existe()) {
            return 0;
        }
        if (bfsIgualDistancia() && bfsIgualTempo()) {
            return 1;
        }
        if (bfsIgualDistancia() || bfsIgualTempo() || distanciaIgualTempo()) {
            return 2;
        }
        return 3;
    }
}