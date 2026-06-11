import java.util.Scanner;

public class Main {

    // Definição das constantes de mapeamento de vértices
    private static final int SOURCE = 0;
    private static final int APP_OFFSET = 1;       // Ocupa índices 1 a 26
    private static final int COMP_OFFSET = 27;     // Ocupa índices 27 a 36
    private static final int SINK = 37;
    private static final int TOTAL_VERTICES = 38;  // Total fixo de nós (0 a 37)

    public static void main(String[] args) {

        // Exemplo simulando as linhas do cenário de erro que discutimos antes
        String[] inputDoDia = {
                "A4 01234;",
                "Q1 5;",
                "P4 56789;"
        };

        // 1. Inicializa a rede com os 38 vértices vazios
        FlowNetwork network = new FlowNetwork(TOTAL_VERTICES);
        int totalDemanda = 0;

        // 2. CONSTRÓI O BLOCO 3: Conectar Computadores -> Sorvedouro (SINK)
        // Como todos os 10 computadores sempre existem e têm capacidade 1,
        // podemos criar essas arestas logo de início.
        for (int c = 0; c < 10; c++) {
            int vertexComputador = COMP_OFFSET + c; // Mapeia para o intervalo [27-36]

            // Cria a aresta direcionada Computador -> SINK com capacidade 1.0
            FlowEdge edge = new FlowEdge(vertexComputador, SINK, 1.0);
            network.addEdge(edge);
        }

        // 3. CONSTRÓI OS BLOCOS 1 E 2: Processar a entrada linha por linha
        for (String linha : inputDoDia) {
            if (linha.trim().isEmpty()) continue;

            // Extrai as informações da linha (Ex: "P5 56789;")
            char appChar = linha.charAt(0);                // 'P'
            int demandaApp = linha.charAt(1) - '0';        // 5
            totalDemanda += demandaApp;                    // Acumula para verificar no final

            // Mapeia o caractere da aplicação para o índice do vértice correspondente [1-26]
            int vertexApp = APP_OFFSET + (appChar - 'A');

            // --- BLOCO 1: Origem (SOURCE) -> Aplicação ---
            // Capacidade é igual à demanda de usuários trazida por aquela aplicação
            FlowEdge edgeOrigemApp = new FlowEdge(SOURCE, vertexApp, demandaApp);
            network.addEdge(edgeOrigemApp);

            // --- BLOCO 2: Aplicação -> Computadores Compatíveis ---
            // Captura apenas a parte numérica dos computadores (ex: "56789")
            String computadoresValidos = linha.substring(3, linha.indexOf(';'));

            for (int i = 0; i < computadoresValidos.length(); i++) {
                int compNum = computadoresValidos.charAt(i) - '0'; // ex: 5
                int vertexComputador = COMP_OFFSET + compNum;       // ex: 27 + 5 = 32

                // Cria aresta da Aplicação para o Computador com capacidade 1.0
                FlowEdge edgeAppComp = new FlowEdge(vertexApp, vertexComputador, 1.0);
                network.addEdge(edgeAppComp);
            }
        }
        
        // 1. Roda o algoritmo
        FordFulkerson maxFlow = new FordFulkerson(network, SOURCE, SINK);

        // 2. Verifica se a alocação foi possível (Fluxo == Demanda Total)
        if ((int) maxFlow.value() != totalDemanda) {
            System.out.println("!"); // Falhou: o corte mínimo impediu a alocação
        } else {
            // 3. Sucesso! Vamos reconstruir a string de 10 caracteres
            char[] alocacao = new char[10];
            for (int i = 0; i < 10; i++) alocacao[i] = '_'; // Preenche com underscore

            // Olha para todos os 10 computadores (índices 27 a 36)
            for (int c = 0; c < 10; c++) {
                int vertexComputador = COMP_OFFSET + c;

                // Varre as arestas conectadas a este computador
                for (FlowEdge edge : network.adj(vertexComputador)) {

                    // Queremos a aresta que VEM de uma Aplicação PARA o Computador
                    // e que tenha recebido FLUXO == 1
                    if (edge.to() == vertexComputador && edge.flow() == 1.0) {
                        int vertexApp = edge.from(); // De onde veio? (índice 1 a 26)
                        alocacao[c] = (char) ('A' + (vertexApp - APP_OFFSET));
                        break;
                    }
                }
            }

            // Imprime a string final!
            System.out.println(new String(alocacao));
        }
    }
}