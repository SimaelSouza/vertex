package br.ifg.vertex.ui;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Entrada {

    private final Scanner scanner;

    public Entrada(InputStream fonte) {
        this.scanner = new Scanner(fonte, StandardCharsets.UTF_8);
    }

    private String lerLinha(String pergunta) {
        System.out.print(pergunta);
        try {
            return scanner.nextLine().trim();
        } catch (NoSuchElementException e) {
            throw new EntradaEncerradaException();
        }
    }

    public String lerTexto(String pergunta) {
        while (true) {
            String linha = lerLinha(pergunta);
            if (!linha.isEmpty()) {
                return linha;
            }
            Console.erro("Este campo não pode ficar vazio.");
        }
    }

    public String lerTextoOuManter(String pergunta, String valorAtual) {
        String linha = lerLinha(pergunta + " [Enter mantém \"" + valorAtual + "\"]: ");
        return linha.isEmpty() ? valorAtual : linha;
    }

    public int lerInteiroOuManter(String pergunta, int valorAtual, int min, int max) {
        while (true) {
            String linha = lerLinha(pergunta + " [Enter mantém " + valorAtual + "]: ");
            if (linha.isEmpty()) {
                return valorAtual;
            }
            try {
                int valor = Integer.parseInt(linha);
                if (valor >= min && valor <= max) {
                    return valor;
                }
                Console.erro("Digite um número entre " + min + " e " + max + ".");
            } catch (NumberFormatException e) {
                Console.erro("Entrada inválida. Digite um número inteiro entre " + min + " e " + max + ".");
            }
        }
    }

    public int lerInteiro(String pergunta, int min, int max) {
        while (true) {
            String linha = lerLinha(pergunta);
            try {
                int valor = Integer.parseInt(linha);
                if (valor >= min && valor <= max) {
                    return valor;
                }
                Console.erro("Digite um número entre " + min + " e " + max + ".");
            } catch (NumberFormatException e) {
                Console.erro("Entrada inválida. Digite um número inteiro entre " + min + " e " + max + ".");
            }
        }
    }

    public double lerDecimal(String pergunta, double min, double max) {
        while (true) {
            String linha = lerLinha(pergunta).replace(',', '.');
            try {
                double valor = Double.parseDouble(linha);
                if (Double.isNaN(valor) || Double.isInfinite(valor)) {
                    Console.erro("Digite um número válido.");
                } else if (valor < min || valor > max) {
                    Console.erro("Digite um valor entre " + min + " e " + max + ".");
                } else {
                    return valor;
                }
            } catch (NumberFormatException e) {
                Console.erro("Entrada inválida. Digite um número (exemplo: 12,5).");
            }
        }
    }

    /** Pergunta s/n e repete até a resposta ser válida. */
    public boolean confirmar(String pergunta) {
        while (true) {
            Console.confirmar(pergunta);
            String linha;
            try {
                linha = scanner.nextLine().trim().toLowerCase();
            } catch (NoSuchElementException e) {
                throw new EntradaEncerradaException();
            }
            if (linha.equals("s") || linha.equals("sim")) {
                return true;
            }
            if (linha.equals("n") || linha.equals("nao") || linha.equals("não")) {
                return false;
            }
            Console.erro("Responda com s ou n.");
        }
    }

    public void aguardarEnter() {
        lerLinha("\nPressione Enter para continuar...");
    }
}