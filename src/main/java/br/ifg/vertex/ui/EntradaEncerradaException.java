package br.ifg.vertex.ui;

/**
 * Lançada quando o teclado não tem mais nada para ler (por exemplo, o usuário
 * apertou Ctrl+D / Ctrl+Z). O {@code Main} captura e encerra o programa de forma limpa.
 */
public class EntradaEncerradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EntradaEncerradaException() {
        super("A entrada de dados foi encerrada.");
    }
}