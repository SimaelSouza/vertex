package br.ifg.vertex.modelo;

public enum TipoLocalidade {
    CIDADE("Cidade"),
    BAIRRO("Bairro"),
    INSTITUICAO("Instituição");

    private final String rotulo;

    TipoLocalidade(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    @Override
    public String toString() {
        return rotulo;
    }
}