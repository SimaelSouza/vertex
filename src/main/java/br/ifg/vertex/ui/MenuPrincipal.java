package br.ifg.vertex.ui;

import br.ifg.vertex.servico.RegraNegocioException;
import br.ifg.vertex.servico.SistemaCidades;

/**
 * Menu principal (as 15 opções do slide 21). As opções 1 a 11 e 13 já funcionam; as demais
 * avisam em qual etapa do roadmap serão implementadas.
 */
public class MenuPrincipal {

    private final Entrada entrada;
    private final MenuLocalidades localidades;
    private final MenuArvore arvore;
    private final MenuConexoes conexoes;
    private final MenuBuscas buscas;
    private final MenuRotas rotas;

    public MenuPrincipal(Entrada entrada, SistemaCidades sistema) {
        this.entrada = entrada;
        this.localidades = new MenuLocalidades(entrada, sistema);
        this.arvore = new MenuArvore(entrada, sistema);
        this.conexoes = new MenuConexoes(entrada, sistema);
        this.buscas = new MenuBuscas(entrada, sistema);
        this.rotas = new MenuRotas(entrada, sistema);
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
            } catch (RegraNegocioException e) {
                Console.erro(e.getMessage()); // regra violada: mensagem pronta, volta ao menu
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
            case 1 -> localidades.cadastrar();
            case 2 -> localidades.pesquisar();
            case 3 -> localidades.atualizar();
            case 4 -> localidades.remover();
            case 5 -> arvore.visualizar();
            case 6 -> arvore.percursos();
            case 7 -> arvore.balancear();
            case 8 -> conexoes.executar();
            case 9 -> buscas.largura();
            case 10 -> buscas.profundidade();
            case 11 -> rotas.menorRota();
            case 13 -> rotas.comparar();
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