package br.ifg.vertex.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class LocalidadeTest {

    @Test
    void criaLocalidadeValidaEAparaONome() {
        Localidade l = new Localidade(50, "  Luziânia  ", TipoLocalidade.CIDADE, 210000);
        assertEquals(50, l.getId());
        assertEquals("Luziânia", l.getNome());
        assertEquals(TipoLocalidade.CIDADE, l.getTipo());
        assertEquals(210000, l.getPopulacao());
    }

    @Test
    void rejeitaIdNaoPositivo() {
        assertThrows(IllegalArgumentException.class,
                () -> new Localidade(0, "X", TipoLocalidade.BAIRRO, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new Localidade(-5, "X", TipoLocalidade.BAIRRO, 1));
    }

    @Test
    void rejeitaNomeVazioTipoNuloEPopulacaoNegativa() {
        assertThrows(IllegalArgumentException.class,
                () -> new Localidade(1, "   ", TipoLocalidade.BAIRRO, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new Localidade(1, null, TipoLocalidade.BAIRRO, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new Localidade(1, "X", null, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new Localidade(1, "X", TipoLocalidade.BAIRRO, -1));
    }

    @Test
    void atualizacaoMantemOId() {
        Localidade l = new Localidade(10, "Hospital", TipoLocalidade.INSTITUICAO, 0);
        l.setNome("Hospital Regional");
        l.setPopulacao(300);
        assertEquals(10, l.getId());
        assertEquals("Hospital Regional", l.getNome());
    }

    @Test
    void igualdadeVemSoDoId() {
        Localidade a = new Localidade(7, "A", TipoLocalidade.CIDADE, 1);
        Localidade b = new Localidade(7, "B", TipoLocalidade.BAIRRO, 2);
        Localidade c = new Localidade(8, "A", TipoLocalidade.CIDADE, 1);
        assertEquals(a, b);
        assertNotEquals(a, c);
    }

    @Test
    void toStringTemFormatoLegivel() {
        Localidade l = new Localidade(50, "Luziânia", TipoLocalidade.CIDADE, 210000);
        assertEquals("50 - Luziânia (Cidade, pop. 210000)", l.toString());
    }
}