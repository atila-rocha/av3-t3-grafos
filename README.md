# UVa 259 - Software Allocation

## 📌 Sobre o Problema
- **Nome do Problema:** Software Allocation
- **ID:** 259
- **Link do Problema:** [https://onlinejudge.org/external/2/259.pdf](https://onlinejudge.org/external/2/259.pdf)

## 👥 Integrantes do Grupo
- Átila Silvio Carvalho Rocha Melo Oliveira
- Victor Menezes do Vale
- Lucas Barroso Sá

## 💻 Linguagem Utilizada
- **Java**

## Link da apresentação
[https://youtu.be/aSCo05kJPLc](https://youtu.be/aSCo05kJPLc)

## 🚀 Como Executar a Solução

1. Certifique-se de ter o JDK (Java Development Kit) instalado na sua máquina.
2. Clone o projeto usando o seguinte comando:
   ```bash
   git clone [https://github.com/atila-rocha/av3-t3-grafos.git](https://github.com/atila-rocha/av3-t3-grafos.git)
   ```
3. Acesse a pasta `src` usando o seguinte comando:
   ```bash
    cd src
   ```
4. Abra o terminal e compile os arquivos:
   ```bash
   javac -d out ./*.java
   ```
5. Execute o programa redirecionando um arquivo de texto contendo os casos de teste:
```bash
java -cp out Main

```


## Modelagem do Grafo

O problema consiste em alocar instâncias de aplicações computacionais (A-Z) a computadores específicos (0-9), garantindo que nenhum computador rode mais de uma aplicação por dia.

Este cenário é um clássico problema de **Emparelhamento Bipartido**, que foi modelado de forma eficiente utilizando uma **Rede de Fluxo Máximo (Maximum Flow)** em um grafo direcionado. A ideia central é injetar uma "demanda de aplicações" através de uma rede de canos (arestas), onde as restrições do problema agem como as larguras (capacidades) desses canos.

### Definição de Vértices, Origem e Sorvedouro

O nosso grafo possui tamanho fixo de **38 vértices**, divididos em 4 camadas estruturais:

1. **Origem (Source - Vértice 0):** O ponto de partida do fluxo. Representa a entrada de usuários solicitando as aplicações do dia.
2. **Vértices de Aplicações (Vértices 1 a 26):** Representam os softwares de 'A' a 'Z'.
3. **Vértices de Computadores (Vértices 27 a 36):** Representam as máquinas físicas de '0' a '9'.
4. **Sorvedouro (Sink - Vértice 37):** O destino final. Representa a conclusão bem-sucedida do processamento de uma aplicação por um computador.

### Arestas e Capacidades

As regras do problema moldam as arestas criadas e seus limites de tráfego:

* **Origem $\rightarrow$ Aplicações:** Aresta criada para cada aplicação solicitada. A **capacidade** é exatamente o número de usuários que pediram a aplicação (ex: `A4` $\rightarrow$ capacidade 4). Evita gerar mais fluxo do que a demanda.
* **Aplicações $\rightarrow$ Computadores:** Aresta criada apenas se a aplicação for compatível com a máquina (ex: `01234`). A **capacidade é 1**, pois uma instância de aplicação ocupa apenas uma vaga na máquina escolhida.
* **Computadores $\rightarrow$ Sorvedouro:** Arestas fixas para os 10 computadores. A **capacidade é 1**, traduzindo a restrição de que máquinas não possuem multitarefa (processam no máximo 1 aplicação por dia).


## Algoritmo Utilizado

Utilizamos o algoritmo de **Ford-Fulkerson** implementado com uma **Busca em Largura (BFS)** para encontrar os caminhos aumentantes. Na literatura de ciência da computação, essa variação específica é formalmente conhecida como **Algoritmo de Edmonds-Karp**.

Essa escolha é ideal pois a BFS garante encontrar sempre o caminho mais curto em número de arestas, evitando loops infinitos e garantindo independência dos valores de capacidade das arestas.

### O Papel do Grafo Residual

A estrutura do grafo residual é fundamental para a solução pois permite "desfazer" alocações. Durante a execução, as arestas mantêm o registro de sua **capacidade residual direta** e do seu **fluxo atual (aresta reversa)**.
Se o algoritmo fizer uma alocação ruim no início (ex: colocar a aplicação 'A' no computador '0', bloqueando outra aplicação que *só* poderia usar o '0'), as arestas reversas no grafo residual permitem que o algoritmo envie fluxo de volta, desfazendo a alocação e abrindo caminho para uma distribuição ótima geral.


## Conversão do Fluxo na Resposta do Problema

O resultado final é construído em duas etapas:

1. **Validação:** Somamos toda a demanda lida no dia (ex: 10 usuários) e comparamos com o **Fluxo Máximo** alcançado pelo algoritmo. Se `Fluxo Máximo < Demanda Total`, ocorreu um fracasso na alocação, e imprimimos `!`.
2. **Recuperação das Rotas:** Se a alocação foi viável (`Fluxo Máximo == Demanda Total`), nós inicializamos uma string com dez caracteres de underscore `__________`. Iteramos pelos 10 vértices de computadores observando suas arestas adjacentes. A aresta oriunda de um vértice de Aplicação cujo **fluxo final seja igual a 1.0** indica que ocorreu o emparelhamento, logo, substituímos o underscore pelo caractere da aplicação correspondente.

### Tratamento do Corte Mínimo

Quando a demanda não pode ser atendida (saída `!`), o problema demonstra matematicamente o Teorema do Fluxo Máximo/Corte Mínimo. O sistema acusa falha porque a busca em largura esbarrou em um **Corte Mínimo** da rede. Isso ocorre geralmente devido ao Princípio da Casa dos Pombos: um subconjunto de aplicações exige, somado, mais máquinas do que o grupo de computadores com as quais elas são compatíveis, estrangulando o escoamento global.


## Análise de Complexidade

A complexidade temporal do algoritmo de Edmonds-Karp é $O(V \cdot E^2)$.

* O número de vértices ($V$) é rigidamente constante e pequeno: **38**.
* O número máximo de arestas ($E$) é limitado a **296** no pior caso absoluto (26 de demanda + 260 compatibilidades possíveis + 10 restrições de máquina).
* Por ser estritamente limitado por constantes minúsculas, a execução do algoritmo para cada caso de teste roda em **tempo constante $O(1)$**.

Portanto, para $T$ casos de teste no arquivo de entrada, a complexidade total assintótica do nosso programa é **$O(T)$**, o que executa na casa de décimos de milissegundo, muito inferior ao Time Limit do juiz online.


## Casos Especiais do Problema

1. **Leitura Invertida por causa do LIFO:** A biblioteca base utilizada modela as listas de adjacência (classe `Bag`) como pilhas invertidas (LIFO). Para garantir que a prioridade de alocação batesse letra por letra com o gabarito oficial do UVa (ex: priorizar computadores menores primeiro), a leitura da string de computadores compatíveis teve que ser iterada e adicionada no grafo *de trás para frente*.
2. **Controle de EOF e Linhas em Branco:** O modelo de teste agrupa as informações por dia, não há indicativo prévio do tamanho da entrada. Foi necessário implementar lógicas no `Scanner` para tratar linhas em branco como "fechamento de dia" (gatilho para executar a rede e limpá-la em seguida) e garantir que o último dia processado pelo gatilho do End-of-File (EOF) não fosse perdido.


## ✅ Comprovante de Accepted


