package br.ifg.vertex.ui;

import br.ifg.vertex.servico.RegraNegocioException;
import br.ifg.vertex.servico.SistemaCidades;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * Base dos menus: guarda a entrada e o sistema e sabe rodar um submenu numerado.
 */
abstract class MenuBase {

    protected final Entrada entrada;
    protected final SistemaCidades sistema;

    MenuBase(Entrada entrada, SistemaCidades sistema) {
        this.entrada = entrada;
        this.sistema = sistema;
    }

    /**
     * Mostra o submenu e executa a ação escolhida até o usuário voltar (opção 0).
     * Se a ação violar uma regra de negócio, mostra o erro e continua no submenu.
     *
     * @param opcoes os textos das opções 1, 2, 3... (a opção 0, "Voltar", é automática)
     * @param acao   recebe o número da opção escolhida
     */
    protected void executarSubmenu(String titulo, List<String> opcoes, IntConsumer acao) {
        while (true) {
            Console.subtitulo(titulo);
            for (int i = 0; i < opcoes.size(); i++) {
                Console.linha((i + 1) + " - " + opcoes.get(i));
            }
            Console.linha("0 - Voltar");
            int opcao = entrada.lerInteiro("Escolha uma opção: ", 0, opcoes.size());
            if (opcao == 0) {
                return;
            }
            try {
                acao.accept(opcao);
            } catch (RegraNegocioException e) {
                Console.erro(e.getMessage());
            }
        }
    }

    /** Lê um ID positivo (não confere se existe). */
    protected int lerId(String pergunta) {
        return entrada.lerInteiro(pergunta, 1, Integer.MAX_VALUE);
    }
}