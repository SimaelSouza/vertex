package br.ifg.vertex.ui;

/**
 * Padroniza tudo que o programa escreve na tela: títulos e as mensagens
 * [OK], [ERRO], [AVISO] e [CONFIRMAR].
 */
public final class Console {

    private static final String LINHA = "=".repeat(45);

    private Console() {
        // classe utilitária: não se cria objeto
    }

    public static void titulo(String texto) {
        System.out.println();
        System.out.println("    " + texto.toUpperCase());
        System.out.println(LINHA);
    }

    public static void subtitulo(String texto) {
        System.out.println();
        System.out.println("--- " + texto + " ---");
    }

    public static void ok(String mensagem) {
        System.out.println("[OK] " + mensagem);
    }

    public static void erro(String mensagem) {
        System.out.println("[ERRO] " + mensagem);
    }

    public static void aviso(String mensagem) {
        System.out.println("[AVISO] " + mensagem);
    }

    /** Escreve a pergunta de confirmação sem pular linha (a resposta é digitada em seguida). */
    public static void confirmar(String pergunta) {
        System.out.print("[CONFIRMAR] " + pergunta + " (s/n): ");
    }

    public static void linha(String texto) {
        System.out.println(texto);
    }

    public static void linhaEmBranco() {
        System.out.println();
    }
}