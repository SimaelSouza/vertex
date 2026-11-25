package br.ifg.vertex.arvore;

public record BalanceamentoNo(int id, int alturaEsquerda, int alturaDireita) {

    public int diferenca() {
        return alturaEsquerda - alturaDireita;
    }

    public boolean estaBalanceado() {
        return Math.abs(diferenca()) <= 1;
    }
}