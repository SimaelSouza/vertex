package br.ifg.vertex.arvore;

import br.ifg.vertex.modelo.Localidade;

public class NoArvore {

    private Localidade localidade;
    private NoArvore esquerdo;
    private NoArvore direito;

    public NoArvore(Localidade localidade) {
        this.localidade = localidade;
    }

    public Localidade getLocalidade() {
        return localidade;
    }

    public void setLocalidade(Localidade localidade) {
        this.localidade = localidade;
    }

    public NoArvore getEsquerdo() {
        return esquerdo;
    }

    public void setEsquerdo(NoArvore esquerdo) {
        this.esquerdo = esquerdo;
    }

    public NoArvore getDireito() {
        return direito;
    }

    public void setDireito(NoArvore direito) {
        this.direito = direito;
    }

    public int getId() {
        return localidade.getId();
    }

    public boolean ehFolha() {
        return esquerdo == null && direito == null;
    }
}