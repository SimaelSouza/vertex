package br.ifg.vertex.ui;

import br.ifg.vertex.arvore.ArvoreBinariaBusca;
import br.ifg.vertex.modelo.Localidade;
import br.ifg.vertex.modelo.TipoLocalidade;
import br.ifg.vertex.servico.ComparacaoRotas;
import br.ifg.vertex.servico.SistemaCidades;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Os quatro relatórios do enunciado (A cadastro, B árvore, C rede, D rotas). Só leitura:
 * cada método consulta o {@link SistemaCidades} e devolve o texto pronto para imprimir.
 */
final class Relatorios {

    private static final int MAXIMO_DE_CICLOS_MOSTRADOS = 5;

    private Relatorios() {
    }

    // ---------------- A: cadastro ----------------

    static String cadastro(SistemaCidades sistema) {
        List<Localidade> localidades = sistema.listarLocalidades(); // percurso em ordem: IDs crescentes
        StringBuilder texto = new StringBuilder();
        texto.append("RELATÓRIO A - CADASTRO DE LOCALIDADES\n");
        if (localidades.isEmpty()) {
            return texto.append("Nenhuma localidade cadastrada.").toString();
        }
        texto.append("Total de localidades: ").append(localidades.size()).append("\n\n");
        texto.append(String.format("%-6s %-32s %-13s %12s%n", "ID", "Nome", "Tipo", "População"));

        long populacaoTotal = 0;
        Localidade maior = localidades.get(0);
        Localidade menor = localidades.get(0);
        int[] porTipo = new int[TipoLocalidade.values().length];
        for (Localidade localidade : localidades) { // um único percurso acumula tudo
            texto.append(String.format("%-6d %-32s %-13s %12s%n", localidade.getId(), localidade.getNome(),
                    localidade.getTipo().getRotulo(), Formatacao.numero(localidade.getPopulacao())));
            populacaoTotal += localidade.getPopulacao();
            porTipo[localidade.getTipo().ordinal()]++;
            if (localidade.getPopulacao() > maior.getPopulacao()) {
                maior = localidade;
            }
            if (localidade.getPopulacao() < menor.getPopulacao()) {
                menor = localidade;
            }
        }
        texto.append("\nPopulação total: ").append(Formatacao.numero(populacaoTotal)).append("\n");
        texto.append("Maior população: ").append(descricaoComPopulacao(maior)).append("\n");
        texto.append("Menor população: ").append(descricaoComPopulacao(menor)).append("\n");
        texto.append("Por tipo:");
        for (TipoLocalidade tipo : TipoLocalidade.values()) {
            texto.append(" ").append(tipo.getRotulo()).append(" = ").append(porTipo[tipo.ordinal()]).append(";");
        }
        return texto.toString();
    }

    private static String descricaoComPopulacao(Localidade l) {
        return l.getId() + " (" + l.getNome() + ") - " + Formatacao.numero(l.getPopulacao()) + " habitantes";
    }

    // ---------------- B: estrutura da árvore ----------------

    /**
     * Mostra a árvore atual e, se ela estiver desbalanceada, também como ficaria balanceada
     * (numa cópia: a árvore real não é alterada), para evidenciar a diferença nas comparações.
     */
    static String arvore(SistemaCidades sistema) {
        ArvoreBinariaBusca atual = sistema.getArvore();
        StringBuilder texto = new StringBuilder("RELATÓRIO B - ESTRUTURA DA ÁRVORE\n");
        if (atual.estaVazia()) {
            return texto.append("A árvore está vazia.").toString();
        }
        texto.append(secaoDaArvore("Árvore atual", atual));
        if (!atual.estaBalanceada()) {
            texto.append("\n\n").append(secaoDaArvore(
                    "Como ficaria balanceada (cópia; a árvore real não foi alterada)", atual.copiaBalanceada()));
        }
        return texto.toString();
    }

    private static String secaoDaArvore(String titulo, ArvoreBinariaBusca arvore) {
        StringBuilder texto = new StringBuilder("> " + titulo + "\n");
        Localidade raiz = arvore.raiz().orElseThrow();
        int desbalanceados = arvore.nosDesbalanceados().size();
        texto.append("Raiz: ").append(raiz.getId()).append(" (").append(raiz.getNome()).append(")\n");
        texto.append("Altura: ").append(arvore.altura())
                .append(" | Folhas: ").append(arvore.quantidadeFolhas())
                .append(" | Nós internos: ").append(arvore.quantidadeInternos()).append("\n");
        texto.append("Balanceada: ").append(arvore.estaBalanceada() ? "sim"
                : "não (" + desbalanceados + " nó(s) com diferença maior que 1)").append("\n\n");

        Localidade menor = arvore.menor().orElseThrow();
        Localidade maior = arvore.maior().orElseThrow();
        List<Localidade> porNiveis = arvore.porNiveis();
        Localidade folhaFunda = porNiveis.get(porNiveis.size() - 1); // último por níveis = nível mais fundo
        int inexistente = maior.getId() < Integer.MAX_VALUE ? maior.getId() + 1 : menor.getId() - 1;

        texto.append("Comparações em buscas selecionadas:\n");
        texto.append(String.format("  %-18s %-34s %-6s %s%n", "Busca", "Localidade", "Nível", "Comparações"));
        linhaDeBusca(texto, "Menor ID", arvore, menor.getId());
        linhaDeBusca(texto, "Maior ID", arvore, maior.getId());
        linhaDeBusca(texto, "Raiz", arvore, raiz.getId());
        linhaDeBusca(texto, "Folha mais funda", arvore, folhaFunda.getId());
        linhaDeBusca(texto, "ID inexistente", arvore, inexistente);
        return texto.toString().stripTrailing();
    }

    private static void linhaDeBusca(StringBuilder texto, String busca, ArvoreBinariaBusca arvore, int id) {
        Optional<Localidade> encontrada = arvore.pesquisar(id);
        String quem = encontrada.map(l -> l.getId() + " (" + l.getNome() + ")").orElse(id + " (não existe)");
        String nivel = encontrada.isPresent() ? String.valueOf(arvore.nivel(id)) : "-";
        texto.append(String.format("  %-18s %-34s %-6s %d%n", busca, quem, nivel, arvore.comparacoesNaBusca(id)));
    }

    // ---------------- C: rede de transportes ----------------

    static String rede(SistemaCidades sistema) {
        StringBuilder texto = new StringBuilder("RELATÓRIO C - REDE DE TRANSPORTES\n");
        List<Integer> vertices = sistema.getGrafo().vertices();
        if (vertices.isEmpty()) {
            return texto.append("A rede está vazia (nenhuma localidade cadastrada).").toString();
        }
        texto.append("Vértices (localidades): ").append(vertices.size()).append("\n");
        texto.append("Arestas (vias de um sentido): ").append(sistema.getGrafo().quantidadeArestas()).append("\n\n");

        List<Integer> isolados = sistema.getGrafo().verticesIsolados();
        texto.append("Vértices isolados: ").append(isolados.size()).append("\n");
        for (int id : isolados) {
            texto.append("  ").append(Formatacao.localidade(sistema, id)).append("\n");
        }

        List<List<Integer>> componentes = sistema.componentesConexas();
        texto.append("\nComponentes conexas (projeção não direcionada, cada via vale nos dois sentidos): ")
                .append(componentes.size()).append("\n");
        for (int i = 0; i < componentes.size(); i++) {
            List<Integer> componente = componentes.get(i);
            texto.append("  ").append(i + 1).append(") ").append(Formatacao.idsInteiros(componente))
                    .append("  (").append(componente.size()).append(" localidade(s))\n");
        }
        texto.append("A rede é conexa? ").append(sistema.redeConexa() ? "sim" : "não").append("\n");

        List<List<Integer>> ciclos = sistema.ciclos();
        texto.append("\nCiclos com 3 ou mais localidades: ").append(ciclos.isEmpty() ? "nenhum" : "existem")
                .append("\n");
        for (int i = 0; i < Math.min(ciclos.size(), MAXIMO_DE_CICLOS_MOSTRADOS); i++) {
            texto.append("  exemplo: ").append(Formatacao.ciclo(ciclos.get(i))).append("\n");
        }
        if (ciclos.size() > MAXIMO_DE_CICLOS_MOSTRADOS) {
            texto.append("  (a busca achou ").append(ciclos.size()).append(" ciclos distintos; mostrando os primeiros ")
                    .append(MAXIMO_DE_CICLOS_MOSTRADOS).append(")\n");
        }

        // localidades com mais conexões (grau de entrada + saída); em caso de empate, todas
        int maiorGrau = -1;
        List<Integer> campeas = new ArrayList<>();
        for (int id : vertices) {
            int grau = sistema.quantidadeConexoes(id);
            if (grau > maiorGrau) {
                maiorGrau = grau;
                campeas.clear();
            }
            if (grau == maiorGrau) {
                campeas.add(id);
            }
        }
        texto.append("\nLocalidades com mais conexões (entrada + saída = ").append(maiorGrau).append("):\n");
        for (int id : campeas) {
            texto.append("  ").append(Formatacao.localidade(sistema, id)).append("\n");
        }
        return texto.toString().stripTrailing();
    }

    // ---------------- D: rotas ----------------

    static String rotas(SistemaCidades sistema, int origem, int destino) {
        ComparacaoRotas comparacao = sistema.compararRotas(origem, destino);
        StringBuilder texto = new StringBuilder("RELATÓRIO D - ROTAS\n");
        texto.append("De ").append(Formatacao.localidade(sistema, origem)).append(" para ")
                .append(Formatacao.localidade(sistema, destino)).append("\n\n");
        if (!comparacao.existe()) {
            return texto.append("Não existe rota seguindo o sentido das vias.").toString();
        }
        texto.append(Formatacao.cabecalhoDeRotas()).append("\n");
        texto.append(Formatacao.linhaDeRota("Menos trechos (BFS)", comparacao.menosTrechos())).append("\n");
        texto.append(Formatacao.linhaDeRota("Menor distância", comparacao.menorDistancia())).append("\n");
        texto.append(Formatacao.linhaDeRota("Menor tempo", comparacao.menorTempo())).append("\n\n");
        texto.append("Menos trechos (BFS): ").append(Formatacao.caminho(sistema, comparacao.menosTrechos())).append("\n");
        texto.append("Menor distância: ").append(Formatacao.caminho(sistema, comparacao.menorDistancia())).append("\n");
        texto.append("Menor tempo: ").append(Formatacao.caminho(sistema, comparacao.menorTempo())).append("\n\n");
        texto.append(Formatacao.conclusaoDaComparacao(comparacao));
        return texto.toString();
    }
}