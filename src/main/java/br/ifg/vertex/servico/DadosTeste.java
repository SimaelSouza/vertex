package br.ifg.vertex.servico;

import br.ifg.vertex.modelo.TipoLocalidade;

/**
 * Dados de teste pré-cadastrados (13 localidades com cidades reais do Entorno do DF).
 *
 * <p>Tudo entra pelo {@link SistemaCidades}, então a própria carga já exercita as regras
 * de integração. A ordem de inserção foi escolhida para a árvore nascer DESBALANCEADA
 * (altura 5, com 4 nós violando o critério), assim a opção "Balancear árvore" tem algo
 * para corrigir na demonstração.</p>
 *
 * <p>As populações são valores aproximados/fictícios, todos diferentes, para que a
 * localidade de maior e a de menor população sejam únicas (Relatório A).</p>
 *
 * <p>As conexões ({@link #carregarConexoes}) formam três regiões: a rede principal (10
 * localidades, com o hub 50), o par Cristalina-Formosa, desconectado do resto, e o bairro
 * 95, isolado. {@link #carregar} cadastra só as localidades; {@link #carregarTudo} faz as
 * duas coisas, que é o que o programa usa ao abrir.</p>
 */
public final class DadosTeste {

    private DadosTeste() {
    }

    /** Cadastra as 13 localidades de teste, na ordem que deixa a árvore desbalanceada. */
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

    /** Cadastra as localidades e depois as conexões de teste. */
    public static void carregarTudo(SistemaCidades sistema) {
        carregar(sistema);
        carregarConexoes(sistema);
    }

    /**
     * Cria as conexões de teste (as localidades precisam já estar cadastradas). Cenários:
     * pares de mão dupla; vias de mão única (90 chega no 15, mas o 15 não chega no 90);
     * um ciclo só de mão única (3, 15, 5); o "hub" 50 com 5 vias de mão dupla; uma região sem
     * ciclos (60-80), desconectada da rede principal; o bairro 95 sem nenhuma via; e tempos que
     * não são proporcionais às distâncias, para que os critérios de rota divirjam.
     */
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