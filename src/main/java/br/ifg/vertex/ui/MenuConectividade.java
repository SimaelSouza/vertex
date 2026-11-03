package br.ifg.vertex.ui;

import br.ifg.vertex.servico.SistemaCidades;
import java.util.List;

/** Opção 12 do menu principal: analisar a conectividade da rede. */
class MenuConectividade extends MenuBase {

    private static final int MAXIMO_DE_CICLOS_MOSTRADOS = 10;

    MenuConectividade(Entrada entrada, SistemaCidades sistema) {
        super(entrada, sistema);
    }

    void executar() {
        executarSubmenu("Analisar conectividade", List.of(
                "Vértices isolados",
                "Alcançabilidade entre duas localidades",
                "Componentes conexas",
                "A rede é conexa?",
                "Detectar ciclos"), opcao -> {
            switch (opcao) {
                case 1 -> isolados();
                case 2 -> alcancabilidade();
                case 3 -> componentes();
                case 4 -> redeConexa();
                case 5 -> ciclos();
            }
        });
    }

    private String nome(int id) {
        return Formatacao.localidade(sistema, id);
    }

    private void isolados() {
        List<Integer> isolados = sistema.getGrafo().verticesIsolados();
        if (isolados.isEmpty()) {
            Console.ok("Nenhuma localidade isolada: todas têm pelo menos uma via.");
            return;
        }
        Console.linha("Localidades isoladas (sem nenhuma via de entrada ou saída):");
        for (int id : isolados) {
            Console.linha("  " + nome(id));
        }
        Console.linha("Total: " + isolados.size() + ".");
    }

    private void alcancabilidade() {
        int a = lerIdExistente("ID da primeira localidade (A): ");
        int b = lerIdExistente("ID da segunda localidade (B): ");
        boolean aChegaEmB = sistema.alcanca(a, b);
        boolean bChegaEmA = sistema.alcanca(b, a);
        Console.linha(nome(a) + " chega em " + nome(b) + ": " + (aChegaEmB ? "SIM" : "NÃO"));
        Console.linha(nome(b) + " chega em " + nome(a) + ": " + (bChegaEmA ? "SIM" : "NÃO"));
        if (aChegaEmB != bChegaEmA) {
            Console.aviso("A alcançabilidade é assimétrica: só um dos sentidos tem caminho "
                    + "(A -> B não implica B -> A).");
        } else if (!aChegaEmB) {
            Console.aviso("Nenhuma das duas chega na outra seguindo o sentido das vias.");
        } else {
            Console.ok("As duas se alcançam nos dois sentidos.");
        }
    }

    private void componentes() {
        List<List<Integer>> componentes = sistema.componentesConexas();
        if (componentes.isEmpty()) {
            Console.aviso("A rede está vazia.");
            return;
        }
        Console.linha("Componentes conexas na projeção NÃO DIRECIONADA (cada via vale nos dois sentidos):");
        for (int i = 0; i < componentes.size(); i++) {
            Console.linha("  " + (i + 1) + ") " + Formatacao.idsInteiros(componentes.get(i))
                    + "  (" + componentes.get(i).size() + " localidade(s))");
        }
        Console.ok("Total: " + componentes.size() + " componente(s).");
    }

    private void redeConexa() {
        if (sistema.getGrafo().quantidadeVertices() == 0) {
            Console.aviso("A rede está vazia.");
        } else if (sistema.redeConexa()) {
            Console.ok("A rede é conexa: todas as localidades estão ligadas, ignorando o sentido das vias.");
        } else {
            Console.aviso("A rede NÃO é conexa: ela tem " + sistema.componentesConexas().size()
                    + " componentes separadas (veja \"Componentes conexas\").");
        }
    }

    private void ciclos() {
        List<List<Integer>> ciclos = sistema.ciclos();
        if (ciclos.isEmpty()) {
            Console.ok("Nenhum ciclo com 3 ou mais localidades. (Ir e voltar pela mesma via de mão dupla não conta.)");
            return;
        }
        Console.aviso("A rede tem ciclos (não conta ir e voltar pela mesma via de mão dupla). Exemplos:");
        int mostrados = Math.min(ciclos.size(), MAXIMO_DE_CICLOS_MOSTRADOS);
        for (int i = 0; i < mostrados; i++) {
            Console.linha("  " + Formatacao.ciclo(ciclos.get(i)));
        }
        Console.linha("Ciclos distintos encontrados pela DFS: " + ciclos.size()
                + (mostrados < ciclos.size() ? " (mostrando " + mostrados + ")" : "") + ".");
    }

}