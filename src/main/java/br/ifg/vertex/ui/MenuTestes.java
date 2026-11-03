package br.ifg.vertex.ui;

import br.ifg.vertex.servico.AutoTeste;
import br.ifg.vertex.servico.CasoDeTeste;
import br.ifg.vertex.servico.DadosTeste;
import br.ifg.vertex.servico.ResultadoTeste;
import br.ifg.vertex.servico.SistemaCidades;
import java.util.ArrayList;
import java.util.List;

/** Opção 15 do menu principal: autoteste e reinício dos dados de demonstração. */
class MenuTestes extends MenuBase {

    MenuTestes(Entrada entrada, SistemaCidades sistema) {
        super(entrada, sistema);
    }

    void executar() {
        executarSubmenu("Executar testes", List.of(
                "Executar os testes automáticos",
                "Recarregar os dados de exemplo (reinicia a demonstração)"), opcao -> {
            switch (opcao) {
                case 1 -> rodarTestes();
                case 2 -> recarregar();
            }
        });
    }

    private void rodarTestes() {
        List<CasoDeTeste> casos = new ArrayList<>(AutoTeste.casos());
        casos.addAll(CasosDeEntrada.casos());
        Console.linha("Os testes usam estruturas próprias: os dados cadastrados não são alterados.");

        List<ResultadoTeste> resultados = AutoTeste.executar(casos);
        String grupoAtual = "";
        int passaram = 0;
        for (ResultadoTeste resultado : resultados) {
            if (!resultado.grupo().equals(grupoAtual)) {
                grupoAtual = resultado.grupo();
                Console.subtitulo(grupoAtual);
            }
            if (resultado.passou()) {
                passaram++;
                Console.ok(resultado.nome());
            } else {
                Console.falha(resultado.nome() + " -> " + resultado.detalhe());
            }
        }
        Console.linhaEmBranco();
        if (passaram == resultados.size()) {
            Console.ok("Todos os " + resultados.size() + " testes passaram.");
        } else {
            Console.erro((resultados.size() - passaram) + " de " + resultados.size() + " testes falharam.");
        }
        Console.linha("Consistência dos dados cadastrados agora: "
                + (sistema.estaConsistente() ? "OK" : "PROBLEMAS " + sistema.verificarConsistencia()));
    }

    private void recarregar() {
        Console.linha("Isto apaga TODAS as localidades e conexões atuais e carrega os dados de exemplo.");
        if (!entrada.confirmar("Deseja recarregar os dados de exemplo?")) {
            Console.aviso("Operação cancelada. Nada foi alterado.");
            return;
        }
        DadosTeste.recarregar(sistema);
        Console.ok(sistema.listarLocalidades().size() + " localidades e "
                + sistema.getGrafo().quantidadeArestas() + " conexões de exemplo carregadas.");
    }
}