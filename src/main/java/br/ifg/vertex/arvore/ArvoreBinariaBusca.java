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
 * contagens, altura, nível, vazia, os quatro percursos, análise e reconstrução
 * balanceada e a representação em vetor por níveis (com o estudo de memória).</p>
 */
public class ArvoreBinariaBusca {

    private NoArvore raiz;

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
                return false;
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

    public Optional<Localidade> pesquisar(int id) {
        NoArvore no = localizar(id);
        return no == null ? Optional.empty() : Optional.of(no.getLocalidade());
    }

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

    public boolean contem(int id) {
        return pesquisar(id).isPresent();
    }

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

    // Menor e maior ID: andar sempre para o mesmo lado até o fim

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


    public boolean estaVazia() {
        return raiz == null;
    }

    public Optional<Localidade> raiz() {
        return raiz == null ? Optional.empty() : Optional.of(raiz.getLocalidade());
    }

    public int quantidadeNos() {
        return contarNos(raiz);
    }

    private int contarNos(NoArvore no) {
        if (no == null) {
            return 0;
        }
        return 1 + contarNos(no.getEsquerdo()) + contarNos(no.getDireito());
    }

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

    public int quantidadeInternos() {
        return quantidadeNos() - quantidadeFolhas();
    }

    public int altura() {
        return altura(raiz);
    }

    private int altura(NoArvore no) {
        if (no == null) {
            return -1;
        }
        return 1 + Math.max(altura(no.getEsquerdo()), altura(no.getDireito()));
    }

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

    public boolean remover(int id) {
        if (!contem(id)) {
            return false;
        }
        raiz = remover(raiz, id);
        return true;
    }

    private NoArvore remover(NoArvore no, int id) {
        if (id < no.getId()) {
            no.setEsquerdo(remover(no.getEsquerdo(), id));
            return no;
        }
        if (id > no.getId()) {
            no.setDireito(remover(no.getDireito(), id));
            return no;
        }

        if (no.ehFolha()) {
            return null;
        }


        if (no.getDireito() == null) {
            return no.getEsquerdo();
        }
        if (no.getEsquerdo() == null) {
            return no.getDireito();
        }


        NoArvore sucessor = menorNo(no.getDireito());
        no.setLocalidade(sucessor.getLocalidade());
        no.setDireito(remover(no.getDireito(), sucessor.getId()));
        return no;
    }

    private NoArvore menorNo(NoArvore no) {
        while (no.getEsquerdo() != null) {
            no = no.getEsquerdo();
        }
        return no;
    }

    // Percursos

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

    // Balanceamento

    public List<BalanceamentoNo> analisarBalanceamento() {
        List<BalanceamentoNo> resultado = new ArrayList<>();
        analisar(raiz, resultado);
        return resultado;
    }

    private int analisar(NoArvore no, List<BalanceamentoNo> resultado) {
        if (no == null) {
            return -1;
        }
        int alturaEsquerda = analisar(no.getEsquerdo(), resultado);

        int posicao = resultado.size();
        resultado.add(null);
        int alturaDireita = analisar(no.getDireito(), resultado);
        resultado.set(posicao, new BalanceamentoNo(no.getId(), alturaEsquerda, alturaDireita));

        return 1 + Math.max(alturaEsquerda, alturaDireita);
    }

    public boolean estaBalanceada() {
        return nosDesbalanceados().isEmpty();
    }

    public List<BalanceamentoNo> nosDesbalanceados() {
        List<BalanceamentoNo> violadores = new ArrayList<>();
        for (BalanceamentoNo analise : analisarBalanceamento()) {
            if (!analise.estaBalanceado()) {
                violadores.add(analise);
            }
        }
        return violadores;
    }

    public void reconstruirBalanceada() {
        List<Localidade> ordenadas = emOrdem();
        raiz = construirBalanceada(ordenadas, 0, ordenadas.size() - 1);
    }

    public ArvoreBinariaBusca copiaBalanceada() {
        ArvoreBinariaBusca copia = new ArvoreBinariaBusca();
        List<Localidade> ordenadas = emOrdem();
        copia.raiz = copia.construirBalanceada(ordenadas, 0, ordenadas.size() - 1);
        return copia;
    }

    private NoArvore construirBalanceada(List<Localidade> ordenadas, int ini, int fim) {
        if (ini > fim) {
            return null;
        }
        int meio = (ini + fim) >>> 1;
        NoArvore no = new NoArvore(ordenadas.get(meio));
        no.setEsquerdo(construirBalanceada(ordenadas, ini, meio - 1));
        no.setDireito(construirBalanceada(ordenadas, meio + 1, fim));
        return no;
    }

    public static ArvoreBinariaBusca escadaDeExemplo(int quantidade) {
        if (quantidade < 0) {
            throw new IllegalArgumentException("A quantidade não pode ser negativa.");
        }
        ArvoreBinariaBusca arvore = new ArvoreBinariaBusca();
        for (int id = 1; id <= quantidade; id++) {
            arvore.inserir(new Localidade(id, "Exemplo " + id, TipoLocalidade.BAIRRO, 0));
        }
        return arvore;
    }

    // Vetor por níveis (índices começando em 1)

    public static final int LIMITE_VETOR = 100_000;

    public static int indiceFilhoEsquerdo(int i) {
        validarIndice(i);
        return 2 * i;
    }

    public static int indiceFilhoDireito(int i) {
        validarIndice(i);
        return 2 * i + 1;
    }

    public static int indicePai(int i) {
        validarIndice(i);
        return i / 2;
    }

    private static void validarIndice(int i) {
        if (i < 1) {
            throw new IllegalArgumentException("Os índices do vetor começam em 1.");
        }
    }

    public static long posicoesNecessarias(int altura) {
        if (altura < 0) {
            return 0;
        }
        if (altura >= 62) {
            return Long.MAX_VALUE;
        }
        return (1L << (altura + 1)) - 1;
    }

    public Localidade[] paraVetor() {
        long necessarias = posicoesNecessarias(altura());
        if (necessarias > LIMITE_VETOR) {
            throw new IllegalStateException("O vetor precisaria de " + necessarias
                    + " posições (limite: " + LIMITE_VETOR + ").");
        }
        Localidade[] vetor = new Localidade[(int) necessarias + 1];
        preencherVetor(raiz, 1, vetor);
        return vetor;
    }

    private void preencherVetor(NoArvore no, int indice, Localidade[] vetor) {
        if (no == null) {
            return;
        }
        vetor[indice] = no.getLocalidade();
        preencherVetor(no.getEsquerdo(), indiceFilhoEsquerdo(indice), vetor);
        preencherVetor(no.getDireito(), indiceFilhoDireito(indice), vetor);
    }

    public EstudoArmazenamento estudarArmazenamento() {
        return EstudoArmazenamento.de(quantidadeNos(), altura());
    }

    // Representação textual

    public String desenhoLateral() {
        StringBuilder texto = new StringBuilder();
        desenhar(raiz, 0, "", texto);
        return texto.toString();
    }

    private void desenhar(NoArvore no, int nivel, String ramo, StringBuilder texto) {
        if (no == null) {
            return;
        }
        desenhar(no.getDireito(), nivel + 1, "/-- ", texto);
        texto.append("    ".repeat(nivel)).append(ramo)
                .append(no.getId()).append(" (").append(no.getLocalidade().getNome()).append(")\n");
        desenhar(no.getEsquerdo(), nivel + 1, "\\-- ", texto);
    }
}