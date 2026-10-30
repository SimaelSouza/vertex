package br.ifg.vertex.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class EntradaTest {

    /** Cria uma Entrada que "digita" as linhas dadas, uma por vez. */
    private Entrada com(String... linhas) {
        String texto = String.join("\n", linhas) + "\n";
        return new Entrada(new ByteArrayInputStream(texto.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void inteiroRepeteAteSerValido() {
        // letras, vazio, fora da faixa, número gigante e finalmente um válido
        Entrada e = com("abc", "", "99", "99999999999999999999", "7");
        assertEquals(7, e.lerInteiro("? ", 0, 15));
    }

    @Test
    void decimalAceitaVirgulaEPonto() {
        assertEquals(12.5, com("12,5").lerDecimal("? ", 0, 1000));
        assertEquals(12.5, com("12.5").lerDecimal("? ", 0, 1000));
    }

    @Test
    void decimalRejeitaTextoNaNEForaDaFaixa() {
        Entrada e = com("x", "NaN", "Infinity", "-3", "4,2");
        assertEquals(4.2, e.lerDecimal("? ", 0, 100));
    }

    @Test
    void textoObrigatorioRepeteSeVazio() {
        assertEquals("Luziânia", com("", "   ", "Luziânia").lerTexto("? "));
    }

    @Test
    void textoOuManterDevolveValorAtualComEnterVazio() {
        assertEquals("Antigo", com("").lerTextoOuManter("Nome", "Antigo"));
        assertEquals("Novo", com("Novo").lerTextoOuManter("Nome", "Antigo"));
    }

    @Test
    void confirmarAceitaSimNaoERepeteNoInvalido() {
        assertTrue(com("talvez", "S").confirmar("ok?"));
        assertFalse(com("nao").confirmar("ok?"));
        assertFalse(com("N").confirmar("ok?"));
    }

    @Test
    void fimDaEntradaLancaExcecaoEspecifica() {
        Entrada vazia = new Entrada(new ByteArrayInputStream(new byte[0]));
        assertThrows(EntradaEncerradaException.class, () -> vazia.lerInteiro("? ", 0, 1));
    }
}