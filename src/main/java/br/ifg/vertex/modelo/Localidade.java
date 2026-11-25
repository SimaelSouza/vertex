package br.ifg.vertex.modelo;

public final class Localidade {

    private final int id;
    private String nome;
    private TipoLocalidade tipo;
    private int populacao;

    public Localidade(int id, String nome, TipoLocalidade tipo, int populacao) {
        if (id <= 0) {
            throw new IllegalArgumentException("O ID deve ser um número positivo.");
        }
        this.id = id;
        setNome(nome);
        setTipo(tipo);
        setPopulacao(populacao);
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public TipoLocalidade getTipo() {
        return tipo;
    }

    public int getPopulacao() {
        return populacao;
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome não pode ficar vazio.");
        }
        this.nome = nome.trim();
    }

    public void setTipo(TipoLocalidade tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("O tipo da localidade é obrigatório.");
        }
        this.tipo = tipo;
    }

    public void setPopulacao(int populacao) {
        if (populacao < 0) {
            throw new IllegalArgumentException("A população não pode ser negativa.");
        }
        this.populacao = populacao;
    }

    @Override
    public boolean equals(Object outro) {
        return outro instanceof Localidade l && l.id == this.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return id + " - " + nome + " (" + tipo + ", pop. " + populacao + ")";
    }
}