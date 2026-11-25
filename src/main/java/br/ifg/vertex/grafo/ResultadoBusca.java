package br.ifg.vertex.grafo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public record ResultadoBusca(
        int origem,
        List<Integer> ordemVisita,
        Map<Integer, Integer> pai,
        Map<Integer, Integer> profundidade,
        List<Integer> naoAlcancados) {

    public ResultadoBusca {
        ordemVisita = List.copyOf(ordemVisita);
        pai = Map.copyOf(pai);
        profundidade = Map.copyOf(profundidade);
        naoAlcancados = List.copyOf(naoAlcancados);
    }

    public boolean alcancou(int vertice) {
        return profundidade.containsKey(vertice);
    }

    public int quantidadeAlcancados() {
        return ordemVisita.size();
    }

    public List<Integer> caminhoAte(int destino) {
        if (!alcancou(destino)) {
            return List.of();
        }
        List<Integer> caminho = new ArrayList<>();
        Integer atual = destino;
        while (atual != null) {
            caminho.add(atual);
            atual = pai.get(atual);
        }
        Collections.reverse(caminho);
        return caminho;
    }
}