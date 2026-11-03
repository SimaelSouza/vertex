package br.ifg.vertex.modelo;

/**
 * Uma conexão DIRECIONADA entre duas localidades, com os dois pesos do enunciado.
 *
 * <p>A direção não é um campo: uma via de mão dupla é representada por duas
 * arestas (A para B e B para A) com os mesmos pesos. A aresta guarda só os IDs,
 * nunca o objeto {@link Localidade}; assim, atualizar o nome de uma localidade
 * aparece automaticamente nas rotas.</p>
 *
 * <p>É um {@code record}: imutável. Para "alterar" pesos, o grafo troca a aresta
 * por outra nova ({@link #comPesos}).</p>
 *
 * @param origem      ID da localidade de origem
 * @param destino     ID da localidade de destino
 * @param distanciaKm distância em quilômetros (maior que zero)
 * @param tempoMin    tempo estimado em minutos (maior que zero)
 */
public record Aresta(int origem, int destino, double distanciaKm, int tempoMin) {

    /** Validações feitas ao criar: qualquer violação lança {@link IllegalArgumentException}. */
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

    /** A aresta no sentido contrário, com os mesmos pesos (a "volta" de uma via de mão dupla). */
    public Aresta inversa() {
        return new Aresta(destino, origem, distanciaKm, tempoMin);
    }

    /** Mesma origem e destino, com novos pesos (também validados). */
    public Aresta comPesos(double novaDistanciaKm, int novoTempoMin) {
        return new Aresta(origem, destino, novaDistanciaKm, novoTempoMin);
    }
}