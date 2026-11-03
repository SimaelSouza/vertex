package br.ifg.vertex.ui;

import br.ifg.vertex.grafo.Grafo;
import br.ifg.vertex.modelo.Aresta;
import br.ifg.vertex.servico.SistemaCidades;
import java.util.List;
import java.util.Optional;

/** Opção 8 do menu principal: gerenciar as conexões (arestas) entre as localidades. */
class MenuConexoes extends MenuBase {

    MenuConexoes(Entrada entrada, SistemaCidades sistema) {
        super(entrada, sistema);
    }

    void executar() {
        executarSubmenu("Gerenciar conexões", List.of(
                "Adicionar conexão",
                "Remover conexão",
                "Alterar distância e tempo",
                "Verificar adjacência entre duas localidades",
                "Consultar vizinhos de uma localidade",
                "Listar vértices",
                "Listar arestas (lista de adjacência)",
                "Graus de entrada e saída",
                "Vértices isolados"), opcao -> {
            switch (opcao) {
                case 1 -> adicionar();
                case 2 -> remover();
                case 3 -> alterar();
                case 4 -> verificarAdjacencia();
                case 5 -> consultarVizinhos();
                case 6 -> listarVertices();
                case 7 -> listarArestas();
                case 8 -> graus();
                case 9 -> isolados();
            }
        });
    }

    private String nome(int id) {
        return Formatacao.localidade(sistema, id);
    }

    /** Lê um ID e já confere na árvore se a localidade existe (se não, lança erro de regra). */
    private int lerIdExistente(String pergunta) {
        int id = lerId(pergunta);
        sistema.exigirLocalidade(id);
        return id;
    }

    private void adicionar() {
        int origem = lerIdExistente("ID da localidade de origem: ");
        int destino = lerIdExistente("ID da localidade de destino: ");
        double km = entrada.lerDecimal("Distância em km: ", 0.01, 100_000);
        int minutos = entrada.lerInteiro("Tempo estimado em minutos: ", 1, 100_000);
        boolean maoDupla = entrada.confirmar("A via é de mão dupla?");

        sistema.conectar(origem, destino, km, minutos, maoDupla);
        Console.ok((maoDupla ? "Via de mão dupla criada: " : "Via de mão única criada: ")
                + nome(origem) + (maoDupla ? " <-> " : " -> ") + nome(destino)
                + " [" + Formatacao.km(km) + ", " + minutos + " min].");
    }

    private void remover() {
        int origem = lerIdExistente("ID da localidade de origem: ");
        int destino = lerIdExistente("ID da localidade de destino: ");
        if (!sistema.existeConexao(origem, destino)) {
            Console.erro("Não existe conexão de " + nome(origem) + " para " + nome(destino) + ".");
            return;
        }
        boolean tambemVolta = sistema.existeConexao(destino, origem)
                && entrada.confirmar("Existe também a conexão de volta. Remover as duas?");

        sistema.desconectar(origem, destino, tambemVolta);
        Console.ok("Conexão removida: " + nome(origem) + (tambemVolta ? " <-> " : " -> ") + nome(destino) + ".");
    }

    private void alterar() {
        int origem = lerIdExistente("ID da localidade de origem: ");
        int destino = lerIdExistente("ID da localidade de destino: ");
        if (!sistema.existeConexao(origem, destino)) {
            Console.erro("Não existe conexão de " + nome(origem) + " para " + nome(destino) + ".");
            return;
        }
        boolean tambemVolta = sistema.existeConexao(destino, origem)
                && entrada.confirmar("Existe também a conexão de volta. Alterar as duas (mantém os pesos iguais)?");
        double km = entrada.lerDecimal("Nova distância em km: ", 0.01, 100_000);
        int minutos = entrada.lerInteiro("Novo tempo estimado em minutos: ", 1, 100_000);

        sistema.alterarConexao(origem, destino, km, minutos, tambemVolta);
        Console.ok("Conexão alterada: " + nome(origem) + (tambemVolta ? " <-> " : " -> ") + nome(destino)
                + " [" + Formatacao.km(km) + ", " + minutos + " min].");
    }

    private void verificarAdjacencia() {
        int a = lerIdExistente("ID da primeira localidade: ");
        int b = lerIdExistente("ID da segunda localidade: ");
        mostrarSentido(a, b);
        mostrarSentido(b, a);
        Console.linha("(a adjacência é direcional: A -> B existir não garante que B -> A exista)");
    }

    private void mostrarSentido(int de, int para) {
        Optional<Aresta> aresta = sistema.getGrafo().obterAresta(de, para);
        String trecho = "  " + nome(de) + " -> " + nome(para) + ": ";
        if (aresta.isPresent()) {
            Console.linha(trecho + "SIM [" + Formatacao.km(aresta.get().distanciaKm()) + ", "
                    + aresta.get().tempoMin() + " min]");
        } else {
            Console.linha(trecho + "não");
        }
    }

    private void consultarVizinhos() {
        int id = lerIdExistente("ID da localidade: ");
        List<Aresta> saidas = sistema.conexoesDe(id);
        if (saidas.isEmpty()) {
            Console.aviso(nome(id) + " não tem conexões de saída.");
            return;
        }
        for (Aresta aresta : saidas) {
            Console.linha("  -> " + Formatacao.conexao(sistema, aresta));
        }
        Console.linha(saidas.size() + " vizinho(s) de " + nome(id) + ".");
    }

    private void listarVertices() {
        List<Integer> vertices = sistema.getGrafo().vertices();
        if (vertices.isEmpty()) {
            Console.aviso("O grafo não tem vértices.");
            return;
        }
        for (int id : vertices) {
            Console.linha("  " + nome(id));
        }
        Console.linha("Total: " + vertices.size() + " vértices.");
    }

    private void listarArestas() {
        Grafo grafo = sistema.getGrafo();
        if (grafo.quantidadeVertices() == 0) {
            Console.aviso("O grafo não tem vértices.");
            return;
        }
        for (int id : grafo.vertices()) {
            StringBuilder linha = new StringBuilder(nome(id)).append(" -> ");
            List<Aresta> saidas = grafo.arestasDeSaida(id);
            if (saidas.isEmpty()) {
                linha.append("(sem conexões de saída)");
            }
            for (int i = 0; i < saidas.size(); i++) {
                if (i > 0) {
                    linha.append(", ");
                }
                linha.append(Formatacao.conexao(sistema, saidas.get(i)));
            }
            Console.linha(linha.toString());
        }
        Console.linha("Total: " + grafo.quantidadeVertices() + " vértices e " + grafo.quantidadeArestas()
                + " arestas (direcionadas; uma via de mão dupla conta 2).");
    }

    private void graus() {
        Grafo grafo = sistema.getGrafo();
        if (grafo.quantidadeVertices() == 0) {
            Console.aviso("O grafo não tem vértices.");
            return;
        }
        Console.linha(String.format("%-36s %8s %8s", "Localidade", "Entrada", "Saída"));
        for (int id : grafo.vertices()) {
            Console.linha(String.format("%-36s %8d %8d", nome(id), grafo.grauEntrada(id), grafo.grauSaida(id)));
        }
    }

    private void isolados() {
        List<Integer> isolados = sistema.getGrafo().verticesIsolados();
        if (isolados.isEmpty()) {
            Console.ok("Nenhum vértice isolado.");
            return;
        }
        Console.linha("Vértices isolados (sem nenhuma conexão):");
        for (int id : isolados) {
            Console.linha("  " + nome(id));
        }
        Console.linha("Total: " + isolados.size() + ".");
    }
}