package br.ifg.vertex.modelo;

public record Aresta(int origem, int destino, double distanciaKm, int tempoMin) {

    public Aresta {
        if (origem <= 0 || destino <= 0) {
            throw new IllegalArgumentException("Os IDs das localidades devem ser números positivos.");
        }
        if (origem == destino) {
            throw new IllegalArgumentException("Uma localidade não pode ser conectada a ela mesma.");
        }
        if (Double.isNaN(distanciaKm) || Double.isInfinite(distanciaKm) || distanciaKm <= 0) {
            throw new IllegalArgumentException("A distância deve ser maior que zero.");
        }
        if (tempoMin <= 0) {
            throw new IllegalArgumentException("O tempo deve ser maior que zero.");
        }
    }

    public Aresta inversa() {
        return new Aresta(destino, origem, distanciaKm, tempoMin);
    }

    public Aresta comPesos(double novaDistanciaKm, int novoTempoMin) {
        return new Aresta(origem, destino, novaDistanciaKm, novoTempoMin);
    }
}