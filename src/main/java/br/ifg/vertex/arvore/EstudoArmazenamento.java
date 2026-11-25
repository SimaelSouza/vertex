package br.ifg.vertex.arvore;

public record EstudoArmazenamento(
        int nos,
        int altura,
        long posicoesNecessarias,
        long posicoesVazias,
        double percentualVazio) {

    public static EstudoArmazenamento de(int nos, int altura) {
        long necessarias = ArvoreBinariaBusca.posicoesNecessarias(altura);
        long vazias = necessarias - nos;
        double percentual = necessarias == 0 ? 0.0 : (double) vazias / necessarias * 100.0;
        return new EstudoArmazenamento(nos, altura, necessarias, vazias, percentual);
    }

    public boolean cabeNoVetor() {
        return posicoesNecessarias <= ArvoreBinariaBusca.LIMITE_VETOR;
    }
}