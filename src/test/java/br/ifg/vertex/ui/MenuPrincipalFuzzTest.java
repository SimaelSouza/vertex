package br.ifg.vertex.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.ifg.vertex.modelo.Localidade;
import br.ifg.vertex.servico.DadosTeste;
import br.ifg.vertex.servico.SistemaCidades;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * "Caça a entradas inválidas": um usuário que digita bobagens aleatórias (letras, vazio, números
 * gigantes, IDs que existem e que não existem) não pode derrubar o programa nem deixar os
 * dados inconsistentes. Se algo inesperado acontecesse, o menu mostraria "erro inesperado".
 */
class MenuPrincipalFuzzTest {

    private static final String[] BOBAGENS = {
            "", "   ", "abc", "x", "-1", "-999", "0", "99999999999999999999", "2147483648", "1e3", "NaN",
            "Infinity", "12,5", "12.5", ",", ".", "1,2,3", "--5", "+7", "0x10", "\t", "ç~ã", "'; DROP TABLE;--",
            "s", "n", "S", "N", "sim", "nao",
    };
    private static final int[] IDS = {3, 5, 10, 15, 20, 30, 40, 50, 60, 70, 80, 90, 95, 100, 999};

    private static String linhaAleatoria(Random sorteio) {
        int tipo = sorteio.nextInt(100);
        if (tipo < 30) {
            return String.valueOf(sorteio.nextInt(17)); // opções de menu e de submenu
        }
        if (tipo < 65) {
            return String.valueOf(IDS[sorteio.nextInt(IDS.length)]);
        }
        if (tipo < 70) {
            return "N".repeat(1 + sorteio.nextInt(300)); // nome enorme
        }
        if (tipo < 75) {
            return String.valueOf(sorteio.nextInt(500)) + "," + sorteio.nextInt(100); // decimal
        }
        return BOBAGENS[sorteio.nextInt(BOBAGENS.length)];
    }

    private static String rodar(SistemaCidades sistema, List<String> linhas) {
        PrintStream original = System.out;
        ByteArrayOutputStream capturado = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturado, true, StandardCharsets.UTF_8));
        try {
            String texto = String.join("\n", linhas) + "\n";
            Entrada entrada = new Entrada(new ByteArrayInputStream(texto.getBytes(StandardCharsets.UTF_8)));
            while (true) { // se o "usuário" sair por acaso, abre o menu de novo até as linhas acabarem
                new MenuPrincipal(entrada, sistema).executar();
            }
        } catch (EntradaEncerradaException e) {
            // esperado: as linhas acabaram no meio de alguma pergunta
        } finally {
            System.setOut(original);
        }
        return capturado.toString(StandardCharsets.UTF_8);
    }

    @Test
    void entradasAleatoriasNuncaQuebramNemDeixamOSistemaInconsistente() {
        int execucoes = 0;
        for (long semente = 1; semente <= 150; semente++) {
            Random sorteio = new Random(semente);
            SistemaCidades sistema = new SistemaCidades();
            DadosTeste.carregarTudo(sistema);
            for (int rodada = 0; rodada < 3; rodada++) {
                List<String> linhas = new ArrayList<>();
                for (int i = 0; i < 700; i++) {
                    linhas.add(linhaAleatoria(sorteio));
                }

                String tela = rodar(sistema, linhas);

                assertFalse(tela.contains("erro inesperado"), "semente " + semente + ": " + trechoDoErro(tela));
                assertTrue(sistema.estaConsistente(), "semente " + semente + ": " + sistema.verificarConsistencia());
                assertTrue(arvoreEmOrdem(sistema), "semente " + semente + ": árvore fora de ordem");
                execucoes++;
            }
        }
        assertTrue(execucoes == 450);
    }

    private static boolean arvoreEmOrdem(SistemaCidades sistema) {
        List<Localidade> ordem = sistema.getArvore().emOrdem();
        for (int i = 1; i < ordem.size(); i++) {
            if (ordem.get(i - 1).getId() >= ordem.get(i).getId()) {
                return false;
            }
        }
        return ordem.size() == sistema.getGrafo().quantidadeVertices();
    }

    private static String trechoDoErro(String tela) {
        int i = tela.indexOf("erro inesperado");
        return i < 0 ? "" : tela.substring(Math.max(0, i - 40), Math.min(tela.length(), i + 120));
    }
}