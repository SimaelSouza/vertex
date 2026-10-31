package br.ifg.vertex.arvore;

import br.ifg.vertex.modelo.Localidade;

/**
 * Um nó da árvore binária de busca: guarda uma {@link Localidade} e as
 * referências para os filhos esquerdo e direito (que podem ser {@code null}).
 *
 * <p>A chave de ordenação é o ID da localidade: IDs menores ficam na subárvore
 * esquerda e IDs maiores na direita.</p>
 */
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

    /** Atalho para o ID da localidade guardada (a chave da árvore). */
    public int getId() {
        return localidade.getId();
    }

    /** Folha = nó sem nenhum filho. */
    public boolean ehFolha() {
        return esquerdo == null && direito == null;
    }
}