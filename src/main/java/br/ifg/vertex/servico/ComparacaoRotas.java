package br.ifg.vertex.servico;

import br.ifg.vertex.grafo.ResultadoCaminho;

public record ComparacaoRotas(
        ResultadoCaminho menosTrechos,
        ResultadoCaminho menorDistancia,
        ResultadoCaminho menorTempo) {

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