package br.ifg.vertex.servico;

import java.util.Objects;

/**
 * Verificações mínimas para os testes automáticos do menu (opção 15). Se algo não for como
 * esperado, lança {@link AssertionError} com uma mensagem em português; o executor mostra
 * isso como [FALHA]. (Os testes JUnit continuam existindo; estes rodam dentro do programa.)
 */
public final class Verifica {

    private Verifica() {
    }

    public static void verdadeiro(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }

    public static void igual(Object esperado, Object atual, String mensagem) {
        if (!Objects.equals(esperado, atual)) {
            throw new AssertionError(mensagem + " (esperado: " + esperado + "; obtido: " + atual + ")");
        }
    }

    /** Confere que o trecho lança exatamente o tipo de exceção esperado. */
    public static void lanca(Class<? extends Throwable> tipo, Runnable trecho, String mensagem) {
        try {
            trecho.run();
        } catch (Throwable e) {
            if (tipo.isInstance(e)) {
                return;
            }
            throw new AssertionError(mensagem + " (lançou " + e.getClass().getSimpleName()
                    + " em vez de " + tipo.getSimpleName() + ")");
        }
        throw new AssertionError(mensagem + " (não lançou nada; esperava " + tipo.getSimpleName() + ")");
    }

    /** Confere as regras de integração (slide 19) no sistema dado. */
    public static void consistente(SistemaCidades sistema) {
        var problemas = sistema.verificarConsistencia();
        if (!problemas.isEmpty()) {
            throw new AssertionError("Sistema inconsistente: " + problemas);
        }
    }
}