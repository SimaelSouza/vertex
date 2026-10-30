package br.ifg.vertex.ui;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Leitura segura do teclado. TODA leitura do programa passa por esta classe.
 *
 * <p>Regras:</p>
 * <ul>
 *   <li>Sempre lê a linha inteira com {@code nextLine()} e converte depois
 *       (nunca mistura {@code nextInt()} com {@code nextLine()}).</li>
 *   <li>Cada método repete a pergunta até receber um valor válido, mostrando
 *       uma mensagem de erro específica.</li>
 *   <li>Se a entrada acabar, lança {@link EntradaEncerradaException}.</li>
 * </ul>
 */
public class Entrada {

    private final Scanner scanner;

    /** Recebe o fluxo de entrada (System.in no programa, um texto fixo nos testes). */
    public Entrada(InputStream fonte) {
        this.scanner = new Scanner(fonte, StandardCharsets.UTF_8);
    }

    /** Lê uma linha crua, já sem espaços nas pontas. */
    private String lerLinha(String pergunta) {
        System.out.print(pergunta);
        try {
            return scanner.nextLine().trim();
        } catch (NoSuchElementException e) {
            throw new EntradaEncerradaException();
        }
    }

    /** Texto obrigatório: repete enquanto vier vazio. */
    public String lerTexto(String pergunta) {
        while (true) {
            String linha = lerLinha(pergunta);
            if (!linha.isEmpty()) {
                return linha;
            }
            Console.erro("Este campo não pode ficar vazio.");
        }
    }

    /**
     * Texto opcional para atualizações: se o usuário só apertar Enter,
     * devolve o valor atual (ou seja, "não mudar").
     */
    public String lerTextoOuManter(String pergunta, String valorAtual) {
        String linha = lerLinha(pergunta + " [Enter mantém \"" + valorAtual + "\"]: ");
        return linha.isEmpty() ? valorAtual : linha;
    }

    /** Inteiro entre {@code min} e {@code max} (inclusive). */
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

    /** Decimal entre {@code min} e {@code max}; aceita "12,5" e "12.5". */
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

    /** Pausa até o usuário apertar Enter (útil depois de mostrar resultados longos). */
    public void aguardarEnter() {
        lerLinha("\nPressione Enter para continuar...");
    }
}