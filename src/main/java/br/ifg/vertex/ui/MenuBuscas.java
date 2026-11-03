package br.ifg.vertex.ui;

import br.ifg.vertex.grafo.ResultadoBusca;
import br.ifg.vertex.servico.SistemaCidades;
import java.util.List;

/** Opções 9 (BFS) e 10 (DFS) do menu principal. */
class MenuBuscas extends MenuBase {

    MenuBuscas(Entrada entrada, SistemaCidades sistema) {
        super(entrada, sistema);
    }

    void largura() {
        Console.subtitulo("Busca em largura (BFS)");
        int origem = lerOrigem();
        ResultadoBusca resultado = sistema.buscaEmLargura(origem);
        mostrar(resultado, "Nível", true);
    }

    void profundidade() {
        Console.subtitulo("Busca em profundidade (DFS)");
        int origem = lerOrigem();
        ResultadoBusca resultado = sistema.buscaEmProfundidade(origem);
        mostrar(resultado, "Profundidade", false);
    }

    private int lerOrigem() {
        int origem = lerId("ID da localidade de origem: ");
        sistema.exigirLocalidade(origem);
        return origem;
    }

    private String nome(int id) {
        return Formatacao.localidade(sistema, id);
    }

    private void mostrar(ResultadoBusca resultado, String colunaNivel, boolean largura) {
        int origem = resultado.origem();
        Console.ok("Busca a partir de " + nome(origem) + " (seguindo o sentido das vias).");
        Console.linha("Ordem de visita: " + juntar(resultado.ordemVisita()));
        Console.linhaEmBranco();
        Console.linha(String.format("%-7s %-45s %-13s %s", "Ordem", "Localidade", colunaNivel, "Veio de"));
        int posicao = 1;
        for (int vertice : resultado.ordemVisita()) {
            Integer pai = resultado.pai().get(vertice);
            Console.linha(String.format("%-7d %-45s %-13d %s", posicao++, nome(vertice),
                    resultado.profundidade().get(vertice), pai == null ? "(origem)" : String.valueOf(pai)));
        }
        Console.linhaEmBranco();
        int total = resultado.quantidadeAlcancados() + resultado.naoAlcancados().size();
        Console.linha("Alcançou " + resultado.quantidadeAlcancados() + " de " + total + " localidades.");
        if (!resultado.naoAlcancados().isEmpty()) {
            Console.aviso("Não alcançadas a partir daqui: " + juntar(resultado.naoAlcancados()));
        }

        if (entrada.confirmar("Deseja ver o caminho até um destino?")) {
            int destino = lerId("ID da localidade de destino: ");
            sistema.exigirLocalidade(destino);
            mostrarCaminho(resultado, destino, largura);
        }
    }

    private void mostrarCaminho(ResultadoBusca resultado, int destino, boolean largura) {
        List<Integer> caminho = resultado.caminhoAte(destino);
        if (caminho.isEmpty()) {
            Console.aviso("Não há caminho de " + nome(resultado.origem()) + " até " + nome(destino) + ".");
            return;
        }
        StringBuilder texto = new StringBuilder();
        for (int vertice : caminho) {
            if (texto.length() > 0) {
                texto.append(" -> ");
            }
            texto.append(nome(vertice));
        }
        Console.ok("Caminho encontrado (" + (caminho.size() - 1) + " trecho(s)): " + texto);
        Console.linha(largura
                ? "A BFS garante o menor número de trechos, mas não a menor distância."
                : "A DFS mostra um caminho possível, não necessariamente o mais curto.");
    }

    private static String juntar(List<Integer> ids) {
        StringBuilder texto = new StringBuilder();
        for (int id : ids) {
            if (texto.length() > 0) {
                texto.append(", ");
            }
            texto.append(id);
        }
        return texto.toString();
    }
}