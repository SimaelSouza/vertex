package br.ifg.vertex.ui;

import br.ifg.vertex.modelo.Localidade;
import br.ifg.vertex.modelo.TipoLocalidade;
import br.ifg.vertex.servico.ResultadoPesquisa;
import br.ifg.vertex.servico.SistemaCidades;
import java.util.Optional;

/** Opções 1 a 4 do menu principal: cadastrar, pesquisar, atualizar e remover localidade. */
class MenuLocalidades extends MenuBase {

    MenuLocalidades(Entrada entrada, SistemaCidades sistema) {
        super(entrada, sistema);
    }

    /** Opção 1. */
    void cadastrar() {
        Console.subtitulo("Cadastrar localidade");
        int id = lerId("ID (número positivo): ");
        if (sistema.existeLocalidade(id)) { // avisa já, antes de pedir o resto
            Console.erro("Já existe uma localidade com o ID " + id + ".");
            return;
        }
        String nome = entrada.lerTexto("Nome: ");
        TipoLocalidade tipo = lerTipo();
        int populacao = entrada.lerInteiro("População: ", 0, Integer.MAX_VALUE);

        Localidade nova = sistema.cadastrarLocalidade(id, nome, tipo, populacao);
        Console.ok("Localidade cadastrada: " + nova);
    }

    /** Opção 2. */
    void pesquisar() {
        Console.subtitulo("Pesquisar localidade");
        int id = lerId("ID: ");
        Optional<ResultadoPesquisa> resultado = sistema.pesquisarLocalidade(id);
        if (resultado.isEmpty()) {
            Console.aviso("Nenhuma localidade encontrada com o ID " + id + " ("
                    + sistema.getArvore().comparacoesNaBusca(id) + " comparações na busca).");
            return;
        }
        Localidade l = resultado.get().localidade();
        Console.ok("Localidade encontrada.");
        Console.linha("  ID:                   " + l.getId());
        Console.linha("  Nome:                 " + l.getNome());
        Console.linha("  Tipo:                 " + l.getTipo());
        Console.linha("  População:            " + Formatacao.numero(l.getPopulacao()));
        Console.linha("  Nível na árvore:      " + resultado.get().nivel() + " (a raiz é o nível 0)");
        Console.linha("  Comparações na busca: " + resultado.get().comparacoes());
    }

    /** Opção 3: Enter vazio mantém o valor atual de cada campo; o ID nunca muda. */
    void atualizar() {
        Console.subtitulo("Atualizar localidade");
        int id = lerId("ID da localidade a atualizar: ");
        Localidade atual = sistema.exigirLocalidade(id);
        Console.linha("Dados atuais: " + atual);

        String nome = entrada.lerTextoOuManter("Novo nome", atual.getNome());
        TipoLocalidade tipo = lerTipoOuManter(atual.getTipo());
        int populacao = entrada.lerInteiroOuManter("Nova população", atual.getPopulacao(), 0, Integer.MAX_VALUE);

        sistema.atualizarLocalidade(id, nome, tipo, populacao);
        Console.ok("Localidade atualizada: " + sistema.exigirLocalidade(id));
    }

    /** Opção 4: mostra o que será apagado e pede confirmação. */
    void remover() {
        Console.subtitulo("Remover localidade");
        int id = lerId("ID da localidade a remover: ");
        Localidade localidade = sistema.exigirLocalidade(id);
        int conexoes = sistema.quantidadeConexoes(id);

        Console.linha("Localidade: " + localidade);
        boolean confirmou = entrada.confirmar("Remover a localidade " + id + " (" + localidade.getNome()
                + ") e suas " + conexoes + " conexões (arestas)?");
        if (!confirmou) {
            Console.aviso("Remoção cancelada.");
            return;
        }
        int removidas = sistema.removerLocalidade(id);
        Console.ok("Localidade " + id + " removida, junto com " + removidas + " conexões.");
    }

    private TipoLocalidade lerTipo() {
        listarTipos();
        int escolha = entrada.lerInteiro("Escolha o tipo: ", 1, TipoLocalidade.values().length);
        return TipoLocalidade.values()[escolha - 1];
    }

    private TipoLocalidade lerTipoOuManter(TipoLocalidade atual) {
        listarTipos();
        Console.linha("(tipo atual: " + atual + ")");
        int escolha = entrada.lerInteiroOuManter("Novo tipo", atual.ordinal() + 1,
                1, TipoLocalidade.values().length);
        return TipoLocalidade.values()[escolha - 1];
    }

    private void listarTipos() {
        TipoLocalidade[] tipos = TipoLocalidade.values();
        Console.linha("Tipos:");
        for (int i = 0; i < tipos.length; i++) {
            Console.linha("  " + (i + 1) + " - " + tipos[i]);
        }
    }
}