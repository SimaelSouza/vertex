package br.ifg.vertex.grafo;

import br.ifg.vertex.modelo.Aresta;

/** Qual peso da aresta o Dijkstra deve minimizar: a distância (km) ou o tempo (minutos). */
public enum CriterioPeso {

    DISTANCIA("distância", "km"),
    TEMPO("tempo", "min");

    private final String nome;
    private final String unidade;

    CriterioPeso(String nome, String unidade) {
        this.nome = nome;
        this.unidade = unidade;
    }

    /** O valor da aresta que este critério considera. */
    public double pesoDe(Aresta aresta) {
        return this == DISTANCIA ? aresta.distanciaKm() : aresta.tempoMin();
    }

    public String getNome() {
        return nome;
    }

    public String getUnidade() {
        return unidade;
    }
}