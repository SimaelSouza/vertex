package br.ifg.vertex.servico;

import br.ifg.vertex.modelo.Localidade;
import br.ifg.vertex.modelo.TipoLocalidade;

public final class DadosTeste {

    private DadosTeste() {
    }

    public static void carregar(SistemaCidades sistema) {
        sistema.cadastrarLocalidade(50, "Luziânia", TipoLocalidade.CIDADE, 210_000);
        sistema.cadastrarLocalidade(30, "Valparaíso de Goiás", TipoLocalidade.CIDADE, 180_000);
        sistema.cadastrarLocalidade(70, "Cidade Ocidental", TipoLocalidade.CIDADE, 95_000);
        sistema.cadastrarLocalidade(20, "Novo Gama", TipoLocalidade.CIDADE, 110_000);
        sistema.cadastrarLocalidade(40, "Santo Antônio do Descoberto", TipoLocalidade.CIDADE, 75_000);
        sistema.cadastrarLocalidade(60, "Cristalina", TipoLocalidade.CIDADE, 62_000);
        sistema.cadastrarLocalidade(80, "Formosa", TipoLocalidade.CIDADE, 120_000);
        sistema.cadastrarLocalidade(10, "Hospital Regional", TipoLocalidade.INSTITUICAO, 2_300);
        sistema.cadastrarLocalidade(5, "Rodoviária Central", TipoLocalidade.INSTITUICAO, 3_500);
        sistema.cadastrarLocalidade(3, "Jardim Ingá", TipoLocalidade.BAIRRO, 12_000);
        sistema.cadastrarLocalidade(15, "Centro", TipoLocalidade.BAIRRO, 28_000);
        sistema.cadastrarLocalidade(90, "IFG (campus)", TipoLocalidade.INSTITUICAO, 1_800);
        sistema.cadastrarLocalidade(95, "Bairro isolado (sem vias)", TipoLocalidade.BAIRRO, 450);
    }

    public static void carregarTudo(SistemaCidades sistema) {
        carregar(sistema);
        carregarConexoes(sistema);
    }

    public static void recarregar(SistemaCidades sistema) {
        for (Localidade localidade : sistema.listarLocalidades()) {
            sistema.removerLocalidade(localidade.getId());
        }
        carregarTudo(sistema);
    }

    public static void carregarConexoes(SistemaCidades sistema) {
        // hub: Luziânia
        sistema.conectar(50, 30, 12.5, 20, true);
        sistema.conectar(50, 70, 21.0, 45, true);
        sistema.conectar(50, 5, 3.2, 8, true);
        sistema.conectar(50, 3, 6.7, 14, true);
        sistema.conectar(50, 10, 4.8, 12, true);
        // restante da rede principal
        sistema.conectar(30, 20, 9.0, 28, true);
        sistema.conectar(20, 10, 14.0, 12, true);
        sistema.conectar(10, 15, 16.0, 11, true);
        sistema.conectar(15, 70, 22.0, 15, true);
        sistema.conectar(20, 40, 34.0, 50, true);
        sistema.conectar(40, 70, 38.5, 55, true);
        // vias de mão única
        sistema.conectar(40, 50, 30.0, 35, false);
        sistema.conectar(90, 15, 5.5, 10, false);
        // ciclo só de mão única: 3 -> 15 -> 5 -> 3
        sistema.conectar(3, 15, 7.5, 16, false);
        sistema.conectar(15, 5, 9.0, 18, false);
        sistema.conectar(5, 3, 5.2, 11, false);
        // grupo desconectado do resto (sem ciclos)
        sistema.conectar(60, 80, 95.5, 80, true);
    }
}