package br.ifg.vertex.arvore;

import br.ifg.vertex.modelo.Localidade;
import java.util.Optional;

/**
 * Árvore binária de busca (ABB) de localidades, ordenada pelo ID.
 *
 * <p>Convenções (decididas no planejamento):</p>
 * <ul>
 *   <li>Altura e nível são contados em <b>arestas</b>: a raiz está no nível 0;
 *       árvore vazia tem altura -1 e árvore com um só nó tem altura 0.</li>
 *   <li>ID repetido é rejeitado: {@link #inserir} devolve {@code false}.</li>
 *   <li>Esta classe não imprime nada e não lança erros de regra de negócio;
 *       quem converte {@code false}/vazio em mensagens para o usuário é o
 *       {@code SistemaCidades} (etapa 5).</li>
 * </ul>
 *
 * <p>Nesta etapa: inserir, pesquisar, menor/maior, contagens, altura, nível e vazia.
 * Remoção, atualização e percursos entram na etapa 3.</p>
 */
public class ArvoreBinariaBusca {

    private NoArvore raiz;

    // ------------------------------------------------------------------
    // Inserção e busca (iterativas: o caminho é sempre uma descida só)
    // ------------------------------------------------------------------

    /**
     * Insere uma localidade na posição correta.
     *
     * @return {@code true} se inseriu; {@code false} se já existia um nó com esse ID
     * @throws IllegalArgumentException se a localidade for nula
     */
    public boolean inserir(Localidade localidade) {
        if (localidade == null) {
            throw new IllegalArgumentException("A localidade não pode ser nula.");
        }
        if (raiz == null) {
            raiz = new NoArvore(localidade);
            return true;
        }

        int id = localidade.getId();
        NoArvore atual = raiz;
        while (true) {
            if (id == atual.getId()) {
                return false; // ID repetido
            }
            if (id < atual.getId()) {
                if (atual.getEsquerdo() == null) {
                    atual.setEsquerdo(new NoArvore(localidade));
                    return true;
                }
                atual = atual.getEsquerdo();
            } else {
                if (atual.getDireito() == null) {
                    atual.setDireito(new NoArvore(localidade));
                    return true;
                }
                atual = atual.getDireito();
            }
        }
    }

    /** Procura uma localidade pelo ID. Vazio se não existir. */
    public Optional<Localidade> pesquisar(int id) {
        NoArvore atual = raiz;
        while (atual != null) {
            if (id == atual.getId()) {
                return Optional.of(atual.getLocalidade());
            }
            atual = (id < atual.getId()) ? atual.getEsquerdo() : atual.getDireito();
        }
        return Optional.empty();
    }

    /** Atalho: existe uma localidade com esse ID? */
    public boolean contem(int id) {
        return pesquisar(id).isPresent();
    }

    /**
     * Quantas comparações uma busca por esse ID faz: cada nó visitado no caminho
     * conta 1 (existindo o ID ou não). Usado no Relatório B.
     *
     * <p>Exemplo: buscar a raiz custa 1; buscar um ID que não existe custa o
     * número de nós do caminho até cair fora da árvore.</p>
     */
    public int comparacoesNaBusca(int id) {
        int comparacoes = 0;
        NoArvore atual = raiz;
        while (atual != null) {
            comparacoes++;
            if (id == atual.getId()) {
                break;
            }
            atual = (id < atual.getId()) ? atual.getEsquerdo() : atual.getDireito();
        }
        return comparacoes;
    }

    // ------------------------------------------------------------------
    // Menor e maior ID: andar sempre para o mesmo lado até o fim
    // ------------------------------------------------------------------

    /** Localidade de menor ID (o nó mais à esquerda). Vazio se a árvore estiver vazia. */
    public Optional<Localidade> menor() {
        if (raiz == null) {
            return Optional.empty();
        }
        NoArvore atual = raiz;
        while (atual.getEsquerdo() != null) {
            atual = atual.getEsquerdo();
        }
        return Optional.of(atual.getLocalidade());
    }

    /** Localidade de maior ID (o nó mais à direita). Vazio se a árvore estiver vazia. */
    public Optional<Localidade> maior() {
        if (raiz == null) {
            return Optional.empty();
        }
        NoArvore atual = raiz;
        while (atual.getDireito() != null) {
            atual = atual.getDireito();
        }
        return Optional.of(atual.getLocalidade());
    }

    // ------------------------------------------------------------------
    // Contagens e medidas (recursivas: cada nó "pergunta" aos seus filhos)
    // ------------------------------------------------------------------

    public boolean estaVazia() {
        return raiz == null;
    }

    /** Localidade guardada na raiz. Vazio se a árvore estiver vazia. */
    public Optional<Localidade> raiz() {
        return raiz == null ? Optional.empty() : Optional.of(raiz.getLocalidade());
    }

    /** Total de nós: 1 (este nó) + nós da esquerda + nós da direita. */
    public int quantidadeNos() {
        return contarNos(raiz);
    }

    private int contarNos(NoArvore no) {
        if (no == null) {
            return 0;
        }
        return 1 + contarNos(no.getEsquerdo()) + contarNos(no.getDireito());
    }

    /** Folhas: nós sem filhos. */
    public int quantidadeFolhas() {
        return contarFolhas(raiz);
    }

    private int contarFolhas(NoArvore no) {
        if (no == null) {
            return 0;
        }
        if (no.ehFolha()) {
            return 1;
        }
        return contarFolhas(no.getEsquerdo()) + contarFolhas(no.getDireito());
    }

    /**
     * Nós internos: têm pelo menos um filho. Um nó ou é folha ou é interno,
     * então basta tirar as folhas do total.
     */
    public int quantidadeInternos() {
        return quantidadeNos() - quantidadeFolhas();
    }

    /**
     * Altura da árvore em arestas: o caminho mais longo da raiz até uma folha.
     * Vazia = -1; só a raiz = 0.
     */
    public int altura() {
        return altura(raiz);
    }

    private int altura(NoArvore no) {
        if (no == null) {
            return -1;
        }
        return 1 + Math.max(altura(no.getEsquerdo()), altura(no.getDireito()));
    }

    /**
     * Nível de um nó: quantas descidas a partir da raiz até chegar nele
     * (a raiz tem nível 0).
     *
     * @return o nível, ou -1 se o ID não existir na árvore
     */
    public int nivel(int id) {
        int nivel = 0;
        NoArvore atual = raiz;
        while (atual != null) {
            if (id == atual.getId()) {
                return nivel;
            }
            atual = (id < atual.getId()) ? atual.getEsquerdo() : atual.getDireito();
            nivel++;
        }
        return -1;
    }
}