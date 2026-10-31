package br.ifg.vertex.arvore;

import br.ifg.vertex.modelo.Localidade;
import br.ifg.vertex.modelo.TipoLocalidade;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

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
 * <p>Operações: inserir, pesquisar, remover (3 casos), atualizar, menor/maior,
 * contagens, altura, nível, vazia e os quatro percursos. Balanceamento e vetor
 * por níveis entram na etapa 4.</p>
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
        NoArvore no = localizar(id);
        return no == null ? Optional.empty() : Optional.of(no.getLocalidade());
    }

    /** Desce pela árvore até achar o nó com esse ID; {@code null} se não existir. */
    private NoArvore localizar(int id) {
        NoArvore atual = raiz;
        while (atual != null) {
            if (id == atual.getId()) {
                return atual;
            }
            atual = (id < atual.getId()) ? atual.getEsquerdo() : atual.getDireito();
        }
        return null;
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

    // ------------------------------------------------------------------
    // Atualização
    // ------------------------------------------------------------------

    /**
     * Atualiza nome, tipo e população da localidade com esse ID. O ID e a posição
     * do nó na árvore não mudam (por isso a árvore não precisa ser reorganizada).
     *
     * <p>Os novos valores são validados <b>antes</b> de qualquer alteração: se algum
     * for inválido, nada é modificado.</p>
     *
     * @return {@code true} se atualizou; {@code false} se o ID não existe
     * @throws IllegalArgumentException se algum dado novo for inválido
     */
    public boolean atualizar(int id, String novoNome, TipoLocalidade novoTipo, int novaPopulacao) {
        NoArvore no = localizar(id);
        if (no == null) {
            return false;
        }
        // Criar uma Localidade temporária só para reaproveitar as validações do construtor.
        Localidade validada = new Localidade(id, novoNome, novoTipo, novaPopulacao);

        // Altera o objeto que já está no nó (outros pontos do sistema enxergam a mudança).
        Localidade atual = no.getLocalidade();
        atual.setNome(validada.getNome());
        atual.setTipo(validada.getTipo());
        atual.setPopulacao(validada.getPopulacao());
        return true;
    }

    // ------------------------------------------------------------------
    // Remoção
    // ------------------------------------------------------------------

    /**
     * Remove a localidade com esse ID, mantendo a árvore uma ABB válida.
     *
     * @return {@code true} se removeu; {@code false} se o ID não existe
     */
    public boolean remover(int id) {
        if (!contem(id)) {
            return false;
        }
        raiz = remover(raiz, id);
        return true;
    }

    /**
     * Remove o ID da subárvore que começa em {@code no} e devolve a nova raiz dessa
     * subárvore (o pai usa o retorno para se religar). Só é chamada quando o ID
     * existe, então {@code no} nunca é nulo.
     */
    private NoArvore remover(NoArvore no, int id) {
        if (id < no.getId()) {
            no.setEsquerdo(remover(no.getEsquerdo(), id));
            return no;
        }
        if (id > no.getId()) {
            no.setDireito(remover(no.getDireito(), id));
            return no;
        }

        // Achamos o nó a remover. Três casos:

        // Caso 1: folha. O pai passa a apontar para nada.
        if (no.ehFolha()) {
            return null;
        }
        // Caso 2: um só filho. O pai passa a apontar para esse filho.
        if (no.getDireito() == null) {
            return no.getEsquerdo();
        }
        if (no.getEsquerdo() == null) {
            return no.getDireito();
        }
        // Caso 3: dois filhos. Copia para este nó a localidade do sucessor em ordem
        // (o menor ID da subárvore direita) e remove o sucessor lá de baixo, onde
        // ele cai no caso 1 ou 2 (um sucessor nunca tem filho esquerdo).
        NoArvore sucessor = menorNo(no.getDireito());
        no.setLocalidade(sucessor.getLocalidade());
        no.setDireito(remover(no.getDireito(), sucessor.getId()));
        return no;
    }

    /** Nó mais à esquerda da subárvore que começa em {@code no}. */
    private NoArvore menorNo(NoArvore no) {
        while (no.getEsquerdo() != null) {
            no = no.getEsquerdo();
        }
        return no;
    }

    // ------------------------------------------------------------------
    // Percursos
    // ------------------------------------------------------------------

    /** Pré-ordem: o nó, depois a subárvore esquerda, depois a direita. */
    public List<Localidade> preOrdem() {
        List<Localidade> resultado = new ArrayList<>();
        preOrdem(raiz, resultado);
        return resultado;
    }

    private void preOrdem(NoArvore no, List<Localidade> resultado) {
        if (no == null) {
            return;
        }
        resultado.add(no.getLocalidade());
        preOrdem(no.getEsquerdo(), resultado);
        preOrdem(no.getDireito(), resultado);
    }

    /**
     * Em ordem: subárvore esquerda, o nó, subárvore direita.
     *
     * <p>Numa ABB sai em ordem crescente de ID: tudo à esquerda é menor que o nó e
     * tudo à direita é maior, então cada nó só aparece depois de todos os menores
     * e antes de todos os maiores (vale em cada subárvore, por indução).</p>
     */
    public List<Localidade> emOrdem() {
        List<Localidade> resultado = new ArrayList<>();
        emOrdem(raiz, resultado);
        return resultado;
    }

    private void emOrdem(NoArvore no, List<Localidade> resultado) {
        if (no == null) {
            return;
        }
        emOrdem(no.getEsquerdo(), resultado);
        resultado.add(no.getLocalidade());
        emOrdem(no.getDireito(), resultado);
    }

    /** Pós-ordem: subárvore esquerda, subárvore direita, depois o nó. */
    public List<Localidade> posOrdem() {
        List<Localidade> resultado = new ArrayList<>();
        posOrdem(raiz, resultado);
        return resultado;
    }

    private void posOrdem(NoArvore no, List<Localidade> resultado) {
        if (no == null) {
            return;
        }
        posOrdem(no.getEsquerdo(), resultado);
        posOrdem(no.getDireito(), resultado);
        resultado.add(no.getLocalidade());
    }

    /**
     * Por níveis (largura): nível 0, depois 1, depois 2... da esquerda para a direita.
     * Usa uma fila: tira o da frente, guarda no resultado e põe os filhos no fim.
     */
    public List<Localidade> porNiveis() {
        List<Localidade> resultado = new ArrayList<>();
        if (raiz == null) {
            return resultado;
        }
        Queue<NoArvore> fila = new ArrayDeque<>();
        fila.add(raiz);
        while (!fila.isEmpty()) {
            NoArvore no = fila.remove();
            resultado.add(no.getLocalidade());
            if (no.getEsquerdo() != null) {
                fila.add(no.getEsquerdo());
            }
            if (no.getDireito() != null) {
                fila.add(no.getDireito());
            }
        }
        return resultado;
    }
}