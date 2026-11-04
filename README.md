# VERTEX — Sistema Inteligente de Gerenciamento de Cidades, Rotas e Infraestrutura

Projeto Final de **Estrutura de Dados II** (IFG — Campus Luziânia), prof. Vitor Lima.
Programa em **Java 21**, orientado a objetos, com menu interativo no terminal. Guarda as
localidades (cidades, bairros e instituições) numa **árvore binária de busca** e as vias de
transporte entre elas num **grafo** com listas de adjacência. As duas estruturas foram
implementadas por nós e trabalham integradas.

> **Integrantes:** _(preencher com nome e matrícula — ainda não confirmado se o trabalho é em grupo ou individual)_

---

## 1. Como compilar e executar

Requisitos: **JDK 21** e **Maven 3.9+** (ou o IntelliJ IDEA, que já traz o Maven).
A única dependência é o **JUnit 5**, usada só nos testes. Nenhuma biblioteca de árvore ou grafo.

```bash
mvn test                          # roda todos os testes JUnit
mvn package                       # compila e gera target/vertex-0.1.0.jar
java -jar target/vertex-0.1.0.jar # executa o programa
```

No **IntelliJ**: abra a pasta do projeto como projeto Maven, rode `br.ifg.vertex.app.Main` e,
para os acentos saírem corretos, coloque em *Run > Edit Configurations > VM options*:
`-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8`.
No **Windows** (prompt de comando), rode `chcp 65001` antes do `java -jar`.

Ao abrir, o programa já carrega os **dados de teste** (13 localidades e 29 conexões), então não é
preciso digitar nada para começar. Novas inserções funcionam normalmente pelo menu.

## 2. Menu principal

| Opção | O que faz |
|---|---|
| 1 a 4 | Cadastrar, pesquisar, atualizar e remover localidade |
| 5 | Visualizar a árvore (desenho lateral, altura, folhas, nós internos) |
| 6 | Percursos: pré-ordem, em ordem, pós-ordem e por níveis |
| 7 | Balanceamento: análise por nó, reconstrução balanceada e estudo do vetor por níveis |
| 8 | Conexões: adicionar, remover, alterar pesos, adjacência, vizinhos, vértices, arestas, graus, isolados |
| 9 e 10 | BFS e DFS a partir de uma origem (DFS também mostra ciclos) |
| 11 | Menor rota por distância (km) ou por tempo (minutos) — Dijkstra |
| 12 | Conectividade: isolados, alcançabilidade, componentes, rede conexa, ciclos |
| 13 | Comparar rotas: BFS × menor distância × menor tempo |
| 14 | Relatórios A (cadastro), B (árvore), C (rede), D (rotas) e todos |
| 15 | Testes automáticos e recarga dos dados de exemplo |
| 0 | Sair (com confirmação) |

As mensagens seguem um padrão: `[OK]`, `[ERRO]`, `[AVISO]`, `[CONFIRMAR]` e `[FALHA]` (nos testes).

## 3. Estrutura do código

```
src/main/java/br/ifg/vertex/
├── app/      Main
├── modelo/   Localidade, TipoLocalidade (enum), Aresta (record)
├── arvore/   NoArvore, ArvoreBinariaBusca, BalanceamentoNo, EstudoArmazenamento
├── grafo/    Grafo, BuscasGrafo (BFS/DFS), Dijkstra, Conectividade, CriterioPeso,
│             ResultadoBusca, ResultadoCaminho
├── servico/  SistemaCidades (integra árvore e grafo), RegraNegocioException,
│             ComparacaoRotas, DadosTeste, AutoTeste, Verifica, ...
└── ui/       MenuPrincipal e submenus, Entrada (teclado seguro), Console, Relatorios, Formatacao
```

A regra central: **só `SistemaCidades` conhece ao mesmo tempo a árvore e o grafo**, e é a única
forma de alterar os dois. A interface (`ui`) fala apenas com ele; nunca chama a árvore ou o grafo
para modificar dados.

## 4. Decisões de projeto

| Decisão | Escolha e motivo |
|---|---|
| ID da localidade | `int` positivo; a ABB compara direto |
| Atributos | `id`, `nome`, `tipo` (CIDADE, BAIRRO, INSTITUICAO), `populacao` |
| ID duplicado | Rejeitado com mensagem; atualizar é uma operação separada |
| Remoção com dois filhos | Sucessor em ordem (menor ID da subárvore direita) |
| Atualizar | Altera os campos do mesmo nó; o ID e a posição não mudam |
| Altura e nível | Contados em **arestas**: raiz no nível 0, árvore vazia tem altura -1 |
| Grafo | `HashMap<Integer, List<Aresta>>`; cada lista fica ordenada por ID de destino, o que torna BFS e DFS determinísticos |
| Via de mão dupla | Duas arestas direcionadas com pesos iguais; alterar/remover pergunta se vale para as duas |
| Laços e pesos | Laço (A → A) é rejeitado; distância e tempo precisam ser maiores que zero |
| Dijkstra | Varredura linear do vértice de menor custo, O(V²), sem `PriorityQueue` |
| Regras violadas | `RegraNegocioException` com mensagem pronta, mostrada como `[ERRO]` |
| Persistência / interface | Só memória e só terminal (o enunciado não pede mais) |

### Componentes conexas: projeção não direcionada

As componentes conexas são calculadas na **projeção não direcionada** da rede: cada via vale nos
dois sentidos, mesmo que seja de mão única. A pergunta respondida é "a rede está toda ligada,
ignorando o sentido das vias?". Já a **alcançabilidade** (A chega em B?) e todas as rotas
**respeitam o sentido** das vias: A → B não implica B → A.

### Regra de ciclos

Toda via de mão dupla forma, no grafo direcionado, um ciclo trivial A → B → A. Se isso contasse,
quase toda rede teria ciclo. Por isso **ir e voltar pela mesma via de mão dupla não conta**: só
são reportados ciclos com **3 ou mais localidades**. A detecção usa DFS com três cores (branco,
cinza, preto), respeitando a direção das arestas, e é feita sobre as **arestas** (de uma aresta
`p → u` só se segue para `u → w` com `w ≠ p`). Assim a regra vale exatamente e nenhum ciclo real
escapa; isso foi conferido contra uma busca por força bruta em 500 grafos aleatórios. A saída são
**exemplos** de ciclos encontrados, não a lista de todos os ciclos possíveis.

## 5. Integração entre árvore e grafo (regras de consistência)

| Regra do enunciado | Como é garantida |
|---|---|
| Nenhum vértice sem localidade na árvore | `cadastrarLocalidade` insere na árvore e depois cria o vértice; se o segundo passo falhar, desfaz o primeiro |
| Nenhuma aresta com localidade inexistente | Antes de tocar o grafo, `SistemaCidades` consulta a árvore (`exigirLocalidade`) |
| Remover localidade remove suas conexões | `removerLocalidade` apaga o vértice e as arestas que saem e chegam, e só depois remove da árvore |
| Atualização refletida nas rotas | O grafo guarda só IDs; o nome é buscado na árvore na hora de mostrar |
| Balancear não altera as conexões | A reconstrução reaproveita os mesmos objetos `Localidade`; o sistema confere que o grafo ficou igual |

`verificarConsistencia()` confere tudo isso (conjunto de IDs da árvore = vértices do grafo, árvore
em ordem, arestas só entre localidades existentes) e roda ao fim dos testes automáticos.

## 6. Dados de teste

Cadastrados em ordem que deixa a árvore **desbalanceada** (altura 5; nós 20, 30, 70 e 80 violam o
critério), para a opção 7 ter algo a corrigir. Depois de balanceada, os 13 nós cabem em altura 3.

| ID | Localidade | Tipo | | ID | Localidade | Tipo |
|---|---|---|---|---|---|---|
| 50 | Luziânia | Cidade | | 60 | Cristalina | Cidade |
| 30 | Valparaíso de Goiás | Cidade | | 80 | Formosa | Cidade |
| 70 | Cidade Ocidental | Cidade | | 10 | Hospital Regional | Instituição |
| 20 | Novo Gama | Cidade | | 5 | Rodoviária Central | Instituição |
| 40 | Santo Antônio do Descoberto | Cidade | | 3 | Jardim Ingá | Bairro |
| 15 | Centro | Bairro | | 90 | IFG (campus) | Instituição |
| 95 | Bairro isolado (sem vias) | Bairro | | | | |

As populações são valores aproximados/fictícios, todos diferentes. As **29 conexões** (distâncias
e tempos fictícios, com casas decimais e sem proporção entre km e minutos) cobrem estes cenários:

- Vias de mão dupla e vias de **mão única** (40 → 50 e 90 → 15).
- **Ida sem volta**: o 90 chega no 15, mas o 15 não chega no 90.
- **Ciclo só de mão única** (3 → 15 → 5 → 3) e uma região sem ciclos (60–80).
- **Hub**: Luziânia (50) tem 11 conexões, muito mais que as demais.
- **3 componentes conexas**: a rede principal (10 localidades), Cristalina–Formosa e o bairro 95, isolado.
- **Rotas**: de 20 para 70, BFS, menor km e menor tempo dão três caminhos diferentes; de 3 para 70, dois coincidem; de 50 para 90, não há rota.

## 7. Respostas às perguntas teóricas

### 7.1 Diferença entre os quatro percursos

Para a árvore formada pela inserção 50, 30, 70, 20, 40, 60, 80:

| Percurso | Ordem de visita | Resultado | Estrutura auxiliar |
|---|---|---|---|
| Pré-ordem | nó, esquerda, direita | 50 30 20 40 70 60 80 | pilha de chamadas |
| Em ordem | esquerda, nó, direita | 20 30 40 50 60 70 80 | pilha de chamadas |
| Pós-ordem | esquerda, direita, nó | 20 40 30 60 80 70 50 | pilha de chamadas |
| Por níveis | nível 0, depois 1, depois 2... | 50 30 70 20 40 60 80 | fila (`Queue`) |

A pré-ordem visita o pai antes dos filhos (útil para copiar a árvore); a pós-ordem visita os filhos
antes do pai (útil para apagar); a por níveis anda "por camadas" e é a que se relaciona com a BFS.

### 7.2 Por que o percurso em ordem de uma ABB sai crescente

Em qualquer nó, tudo à esquerda é menor e tudo à direita é maior. Por indução: uma árvore vazia
ou de um só nó já é crescente; se a subárvore esquerda e a direita são visitadas em ordem
crescente (hipótese), a sequência `esquerda + nó + direita` também é, pois todo valor da
esquerda é menor que o nó e todo valor da direita é maior. Aplicado recursivamente em cada
subárvore, a sequência final é crescente.

### 7.3 BFS × DFS

| Critério | BFS | DFS |
|---|---|---|
| Funcionamento | Explora por camadas, dos mais próximos aos mais distantes | Vai o mais fundo possível e volta (backtracking) |
| Estrutura auxiliar | Fila | Pilha (aqui explícita, sem recursão) |
| Complexidade (listas de adjacência) | O(V + E) | O(V + E) |
| Garante o menor caminho em número de arestas? | Sim | Não |
| Uso natural aqui | Menor número de conexões | Ciclos e alcançabilidade |

### 7.4 Desperdício de memória do vetor por níveis

Com índices a partir de 1 (filho esquerdo `2i`, filho direito `2i + 1`, pai `i / 2`), uma árvore de
altura `h` (em arestas) precisa de `2^(h+1) − 1` posições, mesmo que tenha poucos nós. Resultados
reais do programa (opção 7, estudo comparativo):

| Árvore | Nós | Altura | Posições necessárias | Posições vazias | % vazias |
|---|---|---|---|---|---|
| Dados de teste (atual) | 13 | 5 | 63 | 50 | 79,37% |
| Os mesmos 13 nós, balanceada | 13 | 3 | 15 | 2 | 13,33% |
| "Escada" (IDs em ordem crescente) | 10 | 9 | 1.023 | 1.013 | 99,02% |

Uma árvore balanceada tem `h` próximo de `log2(n)`, então o vetor tem tamanho da mesma ordem de
`n`. Numa escada, `h = n − 1` e o vetor cresce exponencialmente com quase todas as posições
vazias. Por isso o programa só monta o vetor se ele tiver até 100.000 posições; acima disso apenas
informa o tamanho necessário e o desperdício.

### 7.5 Por que BFS, menor distância e menor tempo podem divergir

A BFS conta só **quantidade de arestas**; o Dijkstra por km soma **distâncias**; o Dijkstra por
minutos soma **tempos**. Distância e tempo não são proporcionais (uma estrada curta pode ser de
terra e lenta; uma rodovia mais longa pode ser mais rápida), então os três critérios podem
escolher caminhos diferentes. Exemplo real dos dados de teste, de 20 (Novo Gama) para 70 (Cidade Ocidental):

| Critério | Trechos | Distância | Tempo | Caminho |
|---|---|---|---|---|
| Menos trechos (BFS) | 2 | 72,5 km | 105 min | 20 → 40 → 70 |
| Menor distância | 3 | 39,8 km | 69 min | 20 → 10 → 50 → 70 |
| Menor tempo | 3 | 52 km | 38 min | 20 → 10 → 15 → 70 |

A rota mais curta em arestas é a mais longa e lenta; a de menor distância usa uma via lenta (50 → 70,
45 min); a mais rápida prefere vias mais longas, porém velozes.

## 8. Quadro de complexidade

`n` = nós da árvore, `h` = altura, `V` = vértices, `E` = arestas.

| Operação | Complexidade |
|---|---|
| ABB: inserir, pesquisar, remover, atualizar | O(h) — médio O(log n), pior caso O(n) (árvore em escada) |
| ABB: menor/maior | O(h) |
| ABB: contagens, altura, balanceamento, percursos, reconstrução | O(n) |
| Vetor por níveis | Espaço O(2^h) |
| BFS e DFS | O(V + E) |
| Dijkstra (varredura linear) | O(V²); com heap seria O((V + E) log V) |
| Componentes conexas (projeção não direcionada) | O(V + E) |
| Detecção de ciclos (DFS sobre arestas) | O(E · Δ), com Δ = maior grau de saída |

## 9. Testes

- **JUnit 5** (`mvn test`): mais de 270 casos, cobrindo modelo, árvore (inserção, remoção, percursos, balanceamento, vetor), grafo, BFS/DFS, Dijkstra, conectividade, integração, relatórios e o menu de ponta a ponta com teclado simulado.
- **Entradas inválidas**: `MenuPrincipalFuzzTest` digita centenas de milhares de linhas aleatórias (letras, vazio, números gigantes, IDs inexistentes) e confere que o programa não quebra e os dados continuam consistentes.
- **Autoteste no próprio programa** (opção 15): 36 casos por área, com `[OK]`/`[FALHA]`. Usam estruturas próprias, sem alterar os dados cadastrados. A mesma opção recarrega os dados de exemplo para reiniciar uma demonstração.

## 10. Limitações e melhorias possíveis

- Dijkstra com varredura linear; uma heap própria reduziria a complexidade em redes grandes.
- A árvore é rebalanceada por reconstrução, não por rotações automáticas (AVL não era exigida).
- Os dados ficam só em memória; salvar/carregar em arquivo e uma interface gráfica seriam extras.
- A detecção de ciclos mostra exemplos, não todos os ciclos possíveis.

## 11. Roteiro da demonstração

O roteiro de cerca de 20 minutos, com as teclas de cada passo, está em
[`docs/roteiro-demonstracao.md`](docs/roteiro-demonstracao.md).