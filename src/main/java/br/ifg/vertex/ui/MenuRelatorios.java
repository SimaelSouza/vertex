package br.ifg.vertex.ui;

import br.ifg.vertex.servico.SistemaCidades;
import java.util.List;

/** Opção 14 do menu principal: os relatórios A, B, C e D. */
class MenuRelatorios extends MenuBase {

    MenuRelatorios(Entrada entrada, SistemaCidades sistema) {
        super(entrada, sistema);
    }

    void executar() {
        executarSubmenu("Gerar relatórios", List.of(
                "Relatório A - Cadastro de localidades",
                "Relatório B - Estrutura da árvore",
                "Relatório C - Rede de transportes",
                "Relatório D - Rotas entre duas localidades",
                "Todos os relatórios"), opcao -> {
            switch (opcao) {
                case 1 -> Console.linha(Relatorios.cadastro(sistema));
                case 2 -> Console.linha(Relatorios.arvore(sistema));
                case 3 -> Console.linha(Relatorios.rede(sistema));
                case 4 -> rotas();
                case 5 -> todos();
            }
        });
    }

    private void rotas() {
        int origem = lerIdExistente("ID da localidade de origem: ");
        int destino = lerIdExistente("ID da localidade de destino: ");
        Console.linha(Relatorios.rotas(sistema, origem, destino));
    }

    private void todos() {
        Console.linha(Relatorios.cadastro(sistema));
        Console.linhaEmBranco();
        Console.linha(Relatorios.arvore(sistema));
        Console.linhaEmBranco();
        Console.linha(Relatorios.rede(sistema));
        Console.linhaEmBranco();
        Console.linha("O relatório D precisa de duas localidades:");
        rotas();
    }
}