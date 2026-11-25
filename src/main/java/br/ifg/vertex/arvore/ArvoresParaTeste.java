package br.ifg.vertex.arvore;

import br.ifg.vertex.modelo.Localidade;
import br.ifg.vertex.modelo.TipoLocalidade;
import java.util.ArrayList;
import java.util.List;

final class ArvoresParaTeste {

    static final int[] IDS_DE_TESTE = {50, 30, 70, 20, 40, 60, 80, 10, 5, 3, 15, 90, 95};

    private ArvoresParaTeste() {
    }

    static Localidade loc(int id) {
        return new Localidade(id, "Local " + id, TipoLocalidade.CIDADE, 1000 + id);
    }

    static ArvoreBinariaBusca arvoreDeTeste() {
        return arvoreCom(IDS_DE_TESTE);
    }

    static ArvoreBinariaBusca arvoreCom(int... ids) {
        ArvoreBinariaBusca arvore = new ArvoreBinariaBusca();
        for (int id : ids) {
            arvore.inserir(loc(id));
        }
        return arvore;
    }

    static List<Integer> ids(List<Localidade> localidades) {
        List<Integer> ids = new ArrayList<>();
        for (Localidade l : localidades) {
            ids.add(l.getId());
        }
        return ids;
    }
}