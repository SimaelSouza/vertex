package br.ifg.vertex.ui;

public class EntradaEncerradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EntradaEncerradaException() {
        super("A entrada de dados foi encerrada.");
    }
}