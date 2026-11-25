package br.ifg.vertex.app;

import br.ifg.vertex.servico.DadosTeste;
import br.ifg.vertex.servico.SistemaCidades;
import br.ifg.vertex.ui.Console;
import br.ifg.vertex.ui.Entrada;
import br.ifg.vertex.ui.EntradaEncerradaException;
import br.ifg.vertex.ui.MenuPrincipal;


 // -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8

public class Main {

    public static void main(String[] args) {
        Entrada entrada = new Entrada(System.in);
        try {
            SistemaCidades sistema = new SistemaCidades();
            DadosTeste.carregarTudo(sistema);
            Console.ok(sistema.listarLocalidades().size() + " localidades e "
                    + sistema.getGrafo().quantidadeArestas() + " conexões de teste carregadas.");

            new MenuPrincipal(entrada, sistema).executar();
        } catch (EntradaEncerradaException e) {
            Console.linhaEmBranco();
            Console.aviso("Entrada encerrada. Até logo!");
        }
    }
}