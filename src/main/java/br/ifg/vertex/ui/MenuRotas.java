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
        Console.linha(Formatacao.cabecalhoDeRotas());
        Console.linha(Formatacao.linhaDeRota("Menos trechos (BFS)", comparacao.menosTrechos()));
        Console.linha(Formatacao.linhaDeRota("Menor distância", comparacao.menorDistancia()));
        Console.linha(Formatacao.linhaDeRota("Menor tempo", comparacao.menorTempo()));
        Console.linhaEmBranco();
        Console.linha("Menos trechos (BFS): " + Formatacao.caminho(sistema, comparacao.menosTrechos()));
        Console.linha("Menor distância: " + Formatacao.caminho(sistema, comparacao.menorDistancia()));
        Console.linha("Menor tempo: " + Formatacao.caminho(sistema, comparacao.menorTempo()));
        Console.linhaEmBranco();
        Console.ok(Formatacao.conclusaoDaComparacao(comparacao));
    }

    private void mostrarRota(ResultadoCaminho rota) {
        Console.linha("Caminho: " + Formatacao.caminho(sistema, rota));
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

    private void avisarSemRota(int origem, int destino) {
        Console.aviso("Não existe rota de " + nome(origem) + " até " + nome(destino)
                + " seguindo o sentido das vias.");
    }

    private String nome(int id) {
        return Formatacao.localidade(sistema, id);
    }

}