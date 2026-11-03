package br.ifg.vertex.ui;

import br.ifg.vertex.arvore.ArvoreBinariaBusca;
import br.ifg.vertex.arvore.BalanceamentoNo;
import br.ifg.vertex.arvore.EstudoArmazenamento;
import br.ifg.vertex.modelo.Localidade;
import br.ifg.vertex.servico.ResultadoBalanceamento;
import br.ifg.vertex.servico.SistemaCidades;
import java.util.List;

/** Opções 5, 6 e 7 do menu principal: visualizar, percorrer e balancear a árvore. */
class MenuArvore extends MenuBase {

    MenuArvore(Entrada entrada, SistemaCidades sistema) {
        super(entrada, sistema);
    }

    private ArvoreBinariaBusca arvore() {
        return sistema.getArvore();
    }

    // ------------------------------------------------------------------
    // Opção 5: visualizar
    // ------------------------------------------------------------------

    void visualizar() {
        executarSubmenu("Visualizar árvore", List.of(
                "Desenho da árvore e resumo",
                "Listar todas as localidades (em ordem de ID)",
                "Consultar o nível de uma localidade"), opcao -> {
            switch (opcao) {
                case 1 -> desenhoEResumo();
                case 2 -> listarTodas();
                case 3 -> consultarNivel();
            }
        });
    }

    private void desenhoEResumo() {
        ArvoreBinariaBusca a = arvore();
        if (a.estaVazia()) {
            Console.aviso("A árvore está vazia.");
            return;
        }
        Console.linhaEmBranco();
        Console.linha(a.desenhoLateral().stripTrailing());
        Console.linha("(raiz à esquerda; em cada nó o filho direito fica acima e o esquerdo abaixo)");
        Console.linhaEmBranco();
        Console.linha("Raiz:             " + a.raiz().get().getId());
        Console.linha("Nós:              " + a.quantidadeNos());
        Console.linha("Altura:           " + a.altura() + " (em arestas)");
        Console.linha("Folhas:           " + a.quantidadeFolhas());
        Console.linha("Nós internos:     " + a.quantidadeInternos());
        Console.linha("Menor ID:         " + a.menor().get().getId());
        Console.linha("Maior ID:         " + a.maior().get().getId());
        Console.linha("Balanceada:       " + (a.estaBalanceada() ? "sim" : "não"));
    }

    private void listarTodas() {
        List<Localidade> todas = sistema.listarLocalidades();
        if (todas.isEmpty()) {
            Console.aviso("Nenhuma localidade cadastrada.");
            return;
        }
        for (Localidade localidade : todas) {
            Console.linha("  " + localidade);
        }
        Console.linha("Total: " + todas.size() + " localidades.");
    }

    private void consultarNivel() {
        int id = lerId("ID da localidade: ");
        int nivel = arvore().nivel(id);
        if (nivel < 0) {
            Console.aviso("Nenhuma localidade encontrada com o ID " + id + ".");
        } else {
            Console.ok("A localidade " + Formatacao.localidade(sistema, id) + " está no nível " + nivel
                    + " da árvore (a raiz é o nível 0).");
        }
    }

    // ------------------------------------------------------------------
    // Opção 6: percursos
    // ------------------------------------------------------------------

    void percursos() {
        executarSubmenu("Percursos da árvore", List.of(
                "Pré-ordem", "Em ordem", "Pós-ordem", "Por níveis", "Todos os percursos"), opcao -> {
            if (arvore().estaVazia()) {
                Console.aviso("A árvore está vazia.");
                return;
            }
            switch (opcao) {
                case 1 -> preOrdem();
                case 2 -> emOrdem();
                case 3 -> posOrdem();
                case 4 -> porNiveis();
                case 5 -> {
                    preOrdem();
                    emOrdem();
                    posOrdem();
                    porNiveis();
                }
            }
        });
    }

    private void preOrdem() {
        Console.linha("Pré-ordem (nó, esquerda, direita): " + Formatacao.ids(arvore().preOrdem()));
        Console.linha("  Visita cada nó ANTES dos filhos; a raiz é sempre a primeira.");
    }

    private void emOrdem() {
        Console.linha("Em ordem (esquerda, nó, direita): " + Formatacao.ids(arvore().emOrdem()));
        Console.linha("  Sai em ordem crescente de ID: tudo à esquerda de um nó é menor e tudo à direita é");
        Console.linha("  maior, então cada nó só aparece depois dos menores e antes dos maiores.");
    }

    private void posOrdem() {
        Console.linha("Pós-ordem (esquerda, direita, nó): " + Formatacao.ids(arvore().posOrdem()));
        Console.linha("  Visita cada nó DEPOIS dos filhos; a raiz é sempre a última.");
    }

    private void porNiveis() {
        Console.linha("Por níveis (largura, com fila): " + Formatacao.ids(arvore().porNiveis()));
        Console.linha("  Visita nível por nível, da esquerda para a direita.");
    }

    // ------------------------------------------------------------------
    // Opção 7: balanceamento
    // ------------------------------------------------------------------

    void balancear() {
        executarSubmenu("Balancear árvore", List.of(
                "Analisar balanceamento (altura das subárvores de cada nó)",
                "Reconstruir a árvore balanceada",
                "Estudo comparativo de armazenamento (vetor por níveis)",
                "Mostrar a árvore atual em vetor (índices a partir de 1)"), opcao -> {
            switch (opcao) {
                case 1 -> analisarBalanceamento();
                case 2 -> reconstruir();
                case 3 -> estudoDeArmazenamento();
                case 4 -> mostrarVetor();
            }
        });
    }

    private void analisarBalanceamento() {
        ArvoreBinariaBusca a = arvore();
        List<BalanceamentoNo> analise = a.analisarBalanceamento();
        if (analise.isEmpty()) {
            Console.aviso("A árvore está vazia.");
            return;
        }
        Console.linha(String.format("%-8s %7s %7s %10s   %s", "ID", "h.esq", "h.dir", "diferença", "situação"));
        int violadores = 0;
        for (BalanceamentoNo no : analise) {
            boolean ok = no.estaBalanceado();
            if (!ok) {
                violadores++;
            }
            Console.linha(String.format("%-8d %7d %7d %10d   %s", no.id(), no.alturaEsquerda(),
                    no.alturaDireita(), no.diferenca(), ok ? "ok" : "DESBALANCEADO"));
        }
        Console.linha("(altura -1 = não há filho nesse lado; critério: diferença de no máximo 1 em cada nó)");
        if (violadores == 0) {
            Console.ok("A árvore está balanceada.");
        } else {
            int alturaMinima = 31 - Integer.numberOfLeadingZeros(a.quantidadeNos());
            Console.aviso("A árvore NÃO está balanceada: " + violadores + " nó(s) com diferença maior que 1. "
                    + "Altura atual: " + a.altura() + "; altura mínima possível para " + a.quantidadeNos()
                    + " nós: " + alturaMinima + ".");
        }
    }

    private void reconstruir() {
        if (arvore().estaVazia()) {
            Console.aviso("A árvore está vazia.");
            return;
        }
        if (!entrada.confirmar("Reconstruir a árvore de forma balanceada? (as conexões do grafo não mudam)")) {
            Console.aviso("Reconstrução cancelada.");
            return;
        }
        ResultadoBalanceamento resultado = sistema.balancearArvore();
        Console.ok("Árvore reconstruída: altura " + resultado.alturaAntes() + " -> " + resultado.alturaDepois()
                + ". As conexões do grafo foram mantidas.");
    }

    private void estudoDeArmazenamento() {
        int n = entrada.lerInteiro("Quantos nós terá a árvore em escada de exemplo (1 a 200)? ", 1, 200);
        Console.linhaEmBranco();
        Console.linha(String.format("%-24s %5s %7s %22s %22s %9s", "Árvore", "Nós", "Altura",
                "Posições necessárias", "Posições vazias", "% vazias"));
        linhaDoEstudo("Árvore atual", arvore().estudarArmazenamento());
        linhaDoEstudo("Atual, balanceada", arvore().copiaBalanceada().estudarArmazenamento());
        ArvoreBinariaBusca escada = ArvoreBinariaBusca.escadaDeExemplo(n);
        linhaDoEstudo("Escada de " + n + " nós", escada.estudarArmazenamento());
        linhaDoEstudo("Escada, balanceada", escada.copiaBalanceada().estudarArmazenamento());
        Console.linhaEmBranco();
        Console.linha("Em um vetor por níveis, uma árvore de altura h ocupa 2^(h+1) - 1 posições. Cada posição");
        Console.linha("vazia é memória reservada sem uso: balanceada, o vetor fica com tamanho da ordem de n;");
        Console.linha("em escada, a altura é n-1 e o tamanho cresce exponencialmente, quase todo desperdiçado.");
    }

    private void linhaDoEstudo(String rotulo, EstudoArmazenamento estudo) {
        boolean saturado = estudo.posicoesNecessarias() == Long.MAX_VALUE;
        Console.linha(String.format("%-24s %5d %7d %22s %22s %9s", rotulo, estudo.nos(), estudo.altura(),
                Formatacao.posicoes(estudo.posicoesNecessarias(), saturado),
                Formatacao.posicoes(estudo.posicoesVazias(), saturado),
                Formatacao.percentual(estudo.percentualVazio())));
    }

    private void mostrarVetor() {
        EstudoArmazenamento estudo = arvore().estudarArmazenamento();
        if (estudo.nos() == 0) {
            Console.aviso("A árvore está vazia.");
            return;
        }
        if (!estudo.cabeNoVetor()) {
            Console.aviso("O vetor precisaria de " + Formatacao.posicoes(estudo.posicoesNecessarias(),
                    estudo.posicoesNecessarias() == Long.MAX_VALUE) + " posições (limite: "
                    + Formatacao.numero(ArvoreBinariaBusca.LIMITE_VETOR) + "). Balanceie a árvore primeiro.");
            return;
        }
        Localidade[] vetor = arvore().paraVetor();
        Console.linha(String.format("%-8s %-8s %s", "Índice", "ID", "Nome"));
        for (int i = 1; i < vetor.length; i++) {
            if (vetor[i] != null) { // as posições vazias ficam de fora da listagem
                Console.linha(String.format("%-8d %-8d %s", i, vetor[i].getId(), vetor[i].getNome()));
            }
        }
        Console.linha("Posições usadas: " + estudo.nos() + " de " + estudo.posicoesNecessarias()
                + " (vazias: " + estudo.posicoesVazias() + ", ou " + Formatacao.percentual(estudo.percentualVazio())
                + "). A posição 0 não é usada.");
    }
}