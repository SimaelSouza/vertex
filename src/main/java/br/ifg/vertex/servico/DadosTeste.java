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
 * <p>As conexões (arestas) de teste serão definidas na etapa 7, junto com os pesos;
 * por enquanto todos os vértices começam isolados.</p>
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
}