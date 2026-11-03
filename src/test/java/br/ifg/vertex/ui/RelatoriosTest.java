package br.ifg.vertex.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.ifg.vertex.modelo.TipoLocalidade;
import br.ifg.vertex.servico.DadosTeste;
import br.ifg.vertex.servico.SistemaCidades;
import org.junit.jupiter.api.Test;

class RelatoriosTest {

    private static SistemaCidades completo() {
        SistemaCidades sistema = new SistemaCidades();
        DadosTeste.carregarTudo(sistema);
        return sistema;
    }

    // ---------------- A ----------------

    @Test
    void relatorioAListaEmOrdemDeIdComTotaisEExtremos() {
        String a = Relatorios.cadastro(completo());

        assertTrue(a.contains("Total de localidades: 13"));
        assertTrue(a.indexOf("Jardim Ingá") < a.indexOf("Luziânia")); // ID 3 antes do 50
        assertTrue(a.indexOf("Luziânia") < a.indexOf("Bairro isolado")); // 50 antes do 95
        assertTrue(a.contains("População total: 900.050"));
        assertTrue(a.contains("Maior população: 50 (Luziânia) - 210.000 habitantes"));
        assertTrue(a.contains("Menor população: 95 (Bairro isolado (sem vias)) - 450 habitantes"));
        assertTrue(a.contains("Cidade = 7; Bairro = 3; Instituição = 3;"));
    }

    @Test
    void relatorioAComSistemaVazio() {
        assertTrue(Relatorios.cadastro(new SistemaCidades()).contains("Nenhuma localidade cadastrada."));
    }

    @Test
    void relatorioAComUmaLocalidadeSo() {
        SistemaCidades s = new SistemaCidades();
        s.cadastrarLocalidade(7, "Única", TipoLocalidade.CIDADE, 100);
        String a = Relatorios.cadastro(s);
        assertTrue(a.contains("Maior população: 7 (Única) - 100 habitantes"));
        assertTrue(a.contains("Menor população: 7 (Única) - 100 habitantes"));
    }

    // ---------------- B ----------------

    @Test
    void relatorioBMostraAArvoreAtualEComoFicariaBalanceada() {
        SistemaCidades s = completo();
        String b = Relatorios.arvore(s);

        assertTrue(b.contains("> Árvore atual"));
        assertTrue(b.contains("Raiz: 50 (Luziânia)"));
        assertTrue(b.contains("Altura: 5 | Folhas: 5 | Nós internos: 8"));
        assertTrue(b.contains("Balanceada: não (4 nó(s)"));
        assertTrue(b.contains("> Como ficaria balanceada"));
        assertTrue(b.contains("Altura: 3 | Folhas: 6 | Nós internos: 7"));
        assertTrue(b.contains("96 (não existe)"));
        // a árvore real continua desbalanceada: o relatório só lê
        assertEquals(5, s.getArvore().altura());
    }

    @Test
    void relatorioBDeArvoreBalanceadaTemUmaSecaoSo() {
        SistemaCidades s = completo();
        s.balancearArvore();
        String b = Relatorios.arvore(s);

        assertFalse(b.contains("Como ficaria balanceada"));
        assertTrue(b.contains("Balanceada: sim"));
    }

    @Test
    void relatorioBContaComparacoesDeVerdade() {
        String b = Relatorios.arvore(completo());
        // menor ID (3) fica no nível 5 e custa 6 comparações; a raiz custa 1
        assertTrue(b.contains("Menor ID           3 (Jardim Ingá)                    5      6"));
        assertTrue(b.contains("Raiz               50 (Luziânia)                      0      1"));
    }

    @Test
    void relatorioBComArvoreVaziaOuComUmNo() {
        assertTrue(Relatorios.arvore(new SistemaCidades()).contains("A árvore está vazia."));

        SistemaCidades s = new SistemaCidades();
        s.cadastrarLocalidade(7, "Única", TipoLocalidade.CIDADE, 100);
        String b = Relatorios.arvore(s);
        assertTrue(b.contains("Altura: 0 | Folhas: 1 | Nós internos: 0"));
        assertTrue(b.contains("8 (não existe)"));
    }

    // ---------------- C ----------------

    @Test
    void relatorioCDescreveARedeDeTeste() {
        String c = Relatorios.rede(completo());

        assertTrue(c.contains("Vértices (localidades): 13"));
        assertTrue(c.contains("Arestas (vias de um sentido): 29"));
        assertTrue(c.contains("Vértices isolados: 1"));
        assertTrue(c.contains("95 (Bairro isolado (sem vias))"));
        assertTrue(c.contains("projeção não direcionada, cada via vale nos dois sentidos): 3"));
        assertTrue(c.contains("2) 60, 80  (2 localidade(s))"));
        assertTrue(c.contains("A rede é conexa? não"));
        assertTrue(c.contains("Ciclos com 3 ou mais localidades: existem"));
        assertTrue(c.contains("exemplo: 3 -> 15 -> 5 -> 3"));
        assertTrue(c.contains("(entrada + saída = 11)"));
        assertTrue(c.contains("50 (Luziânia)"));
    }

    @Test
    void relatorioCListaTodasAsLocalidadesEmpatadasComMaisConexoes() {
        SistemaCidades s = new SistemaCidades();
        s.cadastrarLocalidade(1, "A", TipoLocalidade.CIDADE, 1);
        s.cadastrarLocalidade(2, "B", TipoLocalidade.CIDADE, 1);
        s.cadastrarLocalidade(3, "C", TipoLocalidade.CIDADE, 1);
        s.conectar(1, 2, 1, 1, true);

        String c = Relatorios.rede(s);

        assertTrue(c.contains("(entrada + saída = 2)"));
        assertTrue(c.contains("  1 (A)\n  2 (B)"));
        assertTrue(c.contains("A rede é conexa? não"));
        assertTrue(c.contains("Ciclos com 3 ou mais localidades: nenhum"));
    }

    @Test
    void relatorioCComRedeVaziaOuSemConexoes() {
        assertTrue(Relatorios.rede(new SistemaCidades()).contains("A rede está vazia"));

        SistemaCidades s = new SistemaCidades();
        DadosTeste.carregar(s);
        String c = Relatorios.rede(s);
        assertTrue(c.contains("Vértices isolados: 13"));
        assertTrue(c.contains("(entrada + saída = 0)"));
    }

    // ---------------- D ----------------

    @Test
    void relatorioDMostraATabelaDosTresCriterios() {
        String d = Relatorios.rotas(completo(), 20, 70);

        assertTrue(d.contains("De 20 (Novo Gama) para 70 (Cidade Ocidental)"));
        assertTrue(d.contains("72,5 km"));
        assertTrue(d.contains("39,8 km"));
        assertTrue(d.contains("38 min"));
        assertTrue(d.contains("Os três critérios escolheram caminhos diferentes"));
    }

    @Test
    void relatorioDSemRotaInformaEmVezDeTabela() {
        String d = Relatorios.rotas(completo(), 50, 95);
        assertTrue(d.contains("Não existe rota seguindo o sentido das vias."));
        assertFalse(d.contains("Critério"));
    }
}