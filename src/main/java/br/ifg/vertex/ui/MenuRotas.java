package br.ifg.vertex.ui;

import br.ifg.vertex.grafo.CriterioPeso;
import br.ifg.vertex.grafo.ResultadoCaminho;
import br.ifg.vertex.modelo.Aresta;
import br.ifg.vertex.servico.ComparacaoRotas;
import br.ifg.vertex.servico.SistemaCidades;
import java.util.List;

/** Opções 11 (menor rota) e 13 (comparar rotas) do menu principal. */
class MenuRotas extends MenuBase {

    MenuRotas(Entrada entrada, SistemaCidades sistema) {
        super(entrada, sistema);
    }

    /** Opção 11: submenu com os dois critérios do Dijkstra. */
    void menorRota() {
        executarSubmenu("Calcular menor rota", List.of(
                "Menor rota por distância (km)",
                "Menor rota por tempo (minutos)"), opcao -> {
            CriterioPeso criterio = opcao == 1 ? CriterioPeso.DISTANCIA : CriterioPeso.TEMPO;
            int origem = lerIdExistente("ID da localidade de origem: ");
            int destino = lerIdExistente("ID da localidade de destino: ");
            ResultadoCaminho rota = sistema.menorRota(origem, destino, criterio);
            if (!rota.existe()) {
                avisarSemRota(origem, destino);
                return;
            }
            Console.ok("Rota de menor " + criterio.getNome() + " encontrada.");
            mostrarRota(rota);
        });
    }

    /** Opção 13: BFS x menor km x menor tempo para o mesmo par. */
    void comparar() {
        Console.subtitulo("Comparar rotas");
        int origem = lerIdExistente("ID da localidade de origem: ");
        int destino = lerIdExistente("ID da localidade de destino: ");
        ComparacaoRotas comparacao = sistema.compararRotas(origem, destino);
        if (!comparacao.existe()) {
            avisarSemRota(origem, destino);
            return;
        }

        Console.linhaEmBranco();
        Console.linha(String.format("%-24s %-8s %-12s %s", "Critério", "Trechos", "Distância", "Tempo"));
        linhaDaTabela("Menos trechos (BFS)", comparacao.menosTrechos());
        linhaDaTabela("Menor distância", comparacao.menorDistancia());
        linhaDaTabela("Menor tempo", comparacao.menorTempo());
        Console.linhaEmBranco();
        caminho("Menos trechos (BFS)", comparacao.menosTrechos());
        caminho("Menor distância", comparacao.menorDistancia());
        caminho("Menor tempo", comparacao.menorTempo());
        Console.linhaEmBranco();

        switch (comparacao.caminhosDistintos()) {
            case 1 -> Console.ok("Os três critérios escolheram o mesmo caminho neste par.");
            case 2 -> Console.ok("Dois critérios coincidem e o outro escolheu um caminho diferente ("
                    + quemCoincide(comparacao) + ").");
            default -> Console.ok("Os três critérios escolheram caminhos diferentes: distância e tempo "
                    + "não são proporcionais, e a BFS só conta trechos.");
        }
    }

    private static String quemCoincide(ComparacaoRotas c) {
        if (c.bfsIgualDistancia()) {
            return "BFS = menor distância";
        }
        if (c.bfsIgualTempo()) {
            return "BFS = menor tempo";
        }
        return "menor distância = menor tempo";
    }

    private void linhaDaTabela(String criterio, ResultadoCaminho rota) {
        Console.linha(String.format("%-24s %-8d %-12s %d min", criterio, rota.quantidadeTrechos(),
                Formatacao.km(rota.distanciaKm()), rota.tempoMin()));
    }

    private void caminho(String criterio, ResultadoCaminho rota) {
        Console.linha(criterio + ": " + textoDoCaminho(rota));
    }

    private void mostrarRota(ResultadoCaminho rota) {
        Console.linha("Caminho: " + textoDoCaminho(rota));
        if (rota.quantidadeTrechos() == 0) {
            Console.aviso("A origem e o destino são a mesma localidade.");
            return;
        }
        for (Aresta trecho : rota.trechos()) {
            Console.linha("  " + nome(trecho.origem()) + " -> " + nome(trecho.destino())
                    + " [" + Formatacao.km(trecho.distanciaKm()) + ", " + trecho.tempoMin() + " min]");
        }
        Console.linha("Total: " + Formatacao.km(rota.distanciaKm()) + " e " + rota.tempoMin()
                + " min em " + rota.quantidadeTrechos() + " trecho(s).");
    }

    private String textoDoCaminho(ResultadoCaminho rota) {
        StringBuilder texto = new StringBuilder();
        for (int vertice : rota.caminho()) {
            if (texto.length() > 0) {
                texto.append(" -> ");
            }
            texto.append(nome(vertice));
        }
        return texto.toString();
    }

    private void avisarSemRota(int origem, int destino) {
        Console.aviso("Não existe rota de " + nome(origem) + " até " + nome(destino)
                + " seguindo o sentido das vias.");
    }

    private String nome(int id) {
        return Formatacao.localidade(sistema, id);
    }

    private int lerIdExistente(String pergunta) {
        int id = lerId(pergunta);
        sistema.exigirLocalidade(id);
        return id;
    }
}