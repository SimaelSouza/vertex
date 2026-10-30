package br.ifg.vertex.modelo;

/**
 * Tipos de localidade que o sistema gerencia (cidades, bairros e instituições).
 */
public enum TipoLocalidade {
    CIDADE("Cidade"),
    BAIRRO("Bairro"),
    INSTITUICAO("Instituição");

    private final String rotulo;

    TipoLocalidade(String rotulo) {
        this.rotulo = rotulo;
    }

    /** Texto amigável para mostrar ao usuário. */
    public String getRotulo() {
        return rotulo;
    }

    @Override
    public String toString() {
        return rotulo;
    }
}