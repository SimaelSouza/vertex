package br.ifg.vertex.ui;

/**
 * Menu principal (as 15 opções do slide 21). Nesta etapa só o esqueleto funciona:
 * cada opção avisa em qual etapa do roadmap ela será implementada.
 */
public class MenuPrincipal {

    private final Entrada entrada;

    public MenuPrincipal(Entrada entrada) {
        this.entrada = entrada;
    }

    /** Laço principal: mostra o menu, lê a opção e executa até o usuário sair. */
    public void executar() {
        boolean continuar = true;
        while (continuar) {
            try {
                exibir();
                int opcao = entrada.lerInteiro("Escolha uma opção: ", 0, 15);
                continuar = tratarOpcao(opcao);
            } catch (EntradaEncerradaException e) {
                throw e; // quem trata é o Main
            } catch (RuntimeException e) {
                // Última rede de proteção: nenhum erro inesperado derruba o programa.
                Console.erro("Ocorreu um erro inesperado (" + e.getMessage() + "). Voltando ao menu.");
            }
        }
    }

    private void exibir() {
        Console.titulo("Gerenciador de localidades");
        Console.linhaEmBranco();
        Console.linha(" 1 - Cadastrar localidade");
        Console.linha(" 2 - Pesquisar localidade");
        Console.linha(" 3 - Atualizar localidade");
        Console.linha(" 4 - Remover localidade");
        Console.linha(" 5 - Visualizar árvore");
        Console.linha(" 6 - Percursos da árvore");
        Console.linha(" 7 - Balancear árvore");
        Console.linha(" 8 - Gerenciar conexões");
        Console.linha(" 9 - Executar BFS");
        Console.linha("10 - Executar DFS");
        Console.linha("11 - Calcular menor rota");
        Console.linha("12 - Analisar conectividade");
        Console.linha("13 - Comparar rotas");
        Console.linha("14 - Gerar relatórios");
        Console.linha("15 - Executar testes");
        Console.linha(" 0 - Sair");
        Console.linhaEmBranco();
    }

    /** @return {@code false} quando o usuário confirmou a saída */
    private boolean tratarOpcao(int opcao) {
        switch (opcao) {
            case 0 -> {
                if (entrada.confirmar("Deseja realmente sair?")) {
                    Console.ok("Programa encerrado. Até logo!");
                    return false;
                }
            }
            case 1, 2, 3, 4 -> emBreve("Gerenciamento de localidades", "5");
            case 5, 6, 7 -> emBreve("Operações da árvore", "2 a 4");
            case 8 -> emBreve("Gerenciamento de conexões", "5");
            case 9, 10 -> emBreve("Buscas BFS e DFS", "6");
            case 11, 13 -> emBreve("Rotas (Dijkstra)", "7");
            case 12, 14 -> emBreve("Conectividade e relatórios", "8");
            case 15 -> emBreve("Testes automáticos", "9");
            default -> Console.erro("Opção inexistente.");
        }
        return true;
    }

    private void emBreve(String nome, String etapa) {
        Console.aviso("A funcionalidade \"" + nome + "\" ainda não foi implementada (etapa "
                + etapa + " do roadmap).");
    }
}