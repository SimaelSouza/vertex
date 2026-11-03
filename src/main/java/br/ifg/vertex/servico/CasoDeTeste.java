package br.ifg.vertex.servico;

/**
 * Um caso do autoteste: o grupo (área), o nome mostrado na tela e o trecho que verifica.
 * O trecho lança {@link AssertionError} (ou outra exceção) quando o caso falha.
 */
public record CasoDeTeste(String grupo, String nome, Runnable verificacao) {
}