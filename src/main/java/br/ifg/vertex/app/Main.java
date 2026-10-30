package br.ifg.vertex.app;

import br.ifg.vertex.ui.Console;
import br.ifg.vertex.ui.Entrada;
import br.ifg.vertex.ui.EntradaEncerradaException;
import br.ifg.vertex.ui.MenuPrincipal;

/**
 * Ponto de entrada do VERTEX.
 *
 * <p>Dica de execução no IntelliJ (para os acentos saírem certos): em
 * Run &gt; Edit Configurations &gt; VM options, use
 * {@code -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8}.</p>
 */
public class Main {

    public static void main(String[] args) {
        Entrada entrada = new Entrada(System.in);
        try {
            // Nas próximas etapas: criar o SistemaCidades e carregar os dados de teste aqui.
            new MenuPrincipal(entrada).executar();
        } catch (EntradaEncerradaException e) {
            Console.linhaEmBranco();
            Console.aviso("Entrada encerrada. Até logo!");
        }
    }
}