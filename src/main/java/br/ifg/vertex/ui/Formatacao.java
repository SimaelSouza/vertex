package br.ifg.vertex.ui;

import br.ifg.vertex.grafo.ResultadoCaminho;
import br.ifg.vertex.modelo.Aresta;
import br.ifg.vertex.modelo.Localidade;
import br.ifg.vertex.servico.ComparacaoRotas;
import br.ifg.vertex.servico.SistemaCidades;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/** Funções de apoio para mostrar números, localidades e conexões no padrão brasileiro. */
final class Formatacao {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private Formatacao() {
    }

    /** 12.5 vira "12,5"; 12.0 vira "12" (até duas casas decimais). */
    static String decimal(double valor) {
        return new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(PT_BR)).format(valor);
    }

    static String km(double valor) {
        return decimal(valor) + " km";
    }

    /** Inteiro com separador de milhar: 210000 vira "210.000". */
    static String numero(long valor) {
        return NumberFormat.getIntegerInstance(PT_BR).format(valor);
    }

    static String percentual(double valor) {
        return new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(PT_BR)).format(valor) + "%";
    }

    /**
     * Quantidade de posições do vetor. Quando o valor "estourou" o tamanho de um long
     * ({@code saturado}), mostra uma descrição em vez de um número enganoso.
     */
    static String posicoes(long valor, boolean saturado) {
        return saturado ? "mais de 4,6 x 10^18" : numero(valor);
    }

    /** "50 (Luziânia)": o nome vem sempre da árvore, então reflete atualizações. */
    static String localidade(SistemaCidades sistema, int id) {
        return id + " (" + sistema.nomeDe(id) + ")";
    }

    /** "30 (Valparaíso de Goiás) [12,5 km, 20 min]": o destino da aresta com os pesos. */
    static String conexao(SistemaCidades sistema, Aresta aresta) {
        return localidade(sistema, aresta.destino())
                + " [" + km(aresta.distanciaKm()) + ", " + aresta.tempoMin() + " min]";
    }

    /** "50, 30, 70": só os IDs, na ordem da lista. */
    static String ids(List<Localidade> localidades) {
        StringBuilder texto = new StringBuilder();
        for (Localidade localidade : localidades) {
            if (texto.length() > 0) {
                texto.append(", ");
            }
            texto.append(localidade.getId());
        }
        return texto.toString();
    }

    /** "3, 5, 10": IDs inteiros separados por vírgula. */
    static String idsInteiros(List<Integer> ids) {
        StringBuilder texto = new StringBuilder();
        for (int id : ids) {
            if (texto.length() > 0) {
                texto.append(", ");
            }
            texto.append(id);
        }
        return texto.toString();
    }

    /** "3 -> 15 -> 5 -> 3": um ciclo, voltando ao primeiro vértice no fim. */
    static String ciclo(List<Integer> ciclo) {
        StringBuilder texto = new StringBuilder();
        for (int id : ciclo) {
            texto.append(id).append(" -> ");
        }
        return texto.append(ciclo.get(0)).toString();
    }

    /** "50 (Luziânia) -> 30 (Valparaíso de Goiás)": os vértices do caminho com nome. */
    static String caminho(SistemaCidades sistema, ResultadoCaminho rota) {
        StringBuilder texto = new StringBuilder();
        for (int vertice : rota.caminho()) {
            if (texto.length() > 0) {
                texto.append(" -> ");
            }
            texto.append(localidade(sistema, vertice));
        }
        return texto.toString();
    }

    static String cabecalhoDeRotas() {
        return String.format("%-24s %-8s %-12s %s", "Critério", "Trechos", "Distância", "Tempo");
    }

    static String linhaDeRota(String criterio, ResultadoCaminho rota) {
        return String.format("%-24s %-8d %-12s %d min", criterio, rota.quantidadeTrechos(),
                km(rota.distanciaKm()), rota.tempoMin());
    }

    /** Frase que resume se os três critérios escolheram caminhos iguais ou diferentes. */
    static String conclusaoDaComparacao(ComparacaoRotas c) {
        switch (c.caminhosDistintos()) {
            case 1:
                return "Os três critérios escolheram o mesmo caminho neste par.";
            case 2:
                String quem = c.bfsIgualDistancia() ? "BFS = menor distância"
                        : c.bfsIgualTempo() ? "BFS = menor tempo" : "menor distância = menor tempo";
                return "Dois critérios coincidem e o outro escolheu um caminho diferente (" + quem + ").";
            default:
                return "Os três critérios escolheram caminhos diferentes: distância e tempo "
                        + "não são proporcionais, e a BFS só conta trechos.";
        }
    }
}