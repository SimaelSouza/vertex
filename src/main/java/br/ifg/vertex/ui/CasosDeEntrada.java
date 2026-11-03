package br.ifg.vertex.ui;

import br.ifg.vertex.servico.CasoDeTeste;
import br.ifg.vertex.servico.Verifica;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Casos do autoteste sobre a leitura segura do teclado: simulam um usuário digitando entradas
 * inválidas e conferem que a {@link Entrada} insiste na pergunta em vez de quebrar. Como as
 * mensagens de erro da Entrada iriam para a tela, a saída é desviada durante cada caso.
 */
final class CasosDeEntrada {

    private static final String GRUPO = "Entrada";

    private CasosDeEntrada() {
    }

    static List<CasoDeTeste> casos() {
        List<CasoDeTeste> casos = new ArrayList<>();
        casos.add(caso("Letras, texto vazio e número fora da faixa são recusados", () -> {
            Verifica.igual(7, digitando("abc", "", "99", "7").lerInteiro("? ", 0, 15), "inteiro");
        }));
        casos.add(caso("Número negativo e número gigante são recusados", () -> {
            Verifica.igual(3, digitando("-5", "99999999999999999999", "3").lerInteiro("? ", 0, 10), "inteiro");
        }));
        casos.add(caso("Decimal aceita vírgula e ponto", () -> {
            Verifica.igual(12.5, digitando("12,5").lerDecimal("? ", 0, 1000), "com vírgula");
            Verifica.igual(12.5, digitando("12.5").lerDecimal("? ", 0, 1000), "com ponto");
        }));
        casos.add(caso("Decimal recusa texto, NaN, infinito e negativo", () -> {
            Verifica.igual(4.2, digitando("x", "NaN", "Infinity", "-3", "4,2").lerDecimal("? ", 0, 100), "decimal");
        }));
        casos.add(caso("Texto obrigatório não aceita vazio nem só espaços", () -> {
            Verifica.igual("Luziânia", digitando("", "   ", "Luziânia").lerTexto("? "), "texto");
        }));
        casos.add(caso("Confirmação só aceita s ou n e insiste no resto", () -> {
            Verifica.verdadeiro(digitando("talvez", "S").confirmar("ok?"), "S deveria confirmar");
            Verifica.verdadeiro(!digitando("nao", "N").confirmar("ok?"), "N deveria recusar");
        }));
        casos.add(caso("Opção inexistente do menu (16, -1, letras) é recusada", () -> {
            Verifica.igual(15, digitando("16", "-1", "x", "15").lerInteiro("? ", 0, 15), "opção do menu");
        }));
        casos.add(caso("Fim da entrada (Ctrl+D) encerra de forma controlada, sem travar", () -> {
            Verifica.lanca(EntradaEncerradaException.class,
                    () -> new Entrada(new ByteArrayInputStream(new byte[0])).lerInteiro("? ", 0, 1),
                    "fim da entrada");
        }));
        return casos;
    }

    private static CasoDeTeste caso(String nome, Runnable verificacao) {
        return new CasoDeTeste(GRUPO, nome, () -> silenciando(verificacao));
    }

    private static Entrada digitando(String... linhas) {
        String texto = String.join("\n", linhas) + "\n";
        return new Entrada(new ByteArrayInputStream(texto.getBytes(StandardCharsets.UTF_8)));
    }

    /** Roda o trecho com a saída desviada para o nada e restaura a saída original no fim. */
    private static void silenciando(Runnable trecho) {
        PrintStream original = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
        try {
            trecho.run();
        } finally {
            System.setOut(original);
        }
    }
}