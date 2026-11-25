package br.ifg.vertex.servico;

import br.ifg.vertex.modelo.Localidade;

public record ResultadoPesquisa(Localidade localidade, int nivel, int comparacoes) {
}