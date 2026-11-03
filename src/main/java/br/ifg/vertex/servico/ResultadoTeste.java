package br.ifg.vertex.servico;

/** O resultado de um caso do autoteste: passou ou falhou (e por quê). */
public record ResultadoTeste(String grupo, String nome, boolean passou, String detalhe) {
}