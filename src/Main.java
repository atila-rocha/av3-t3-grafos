import java.util.Scanner;

public class Main {

    // Definição das constantes de mapeamento de vértices
    private static final int SOURCE = 0;
    private static final int APP_OFFSET = 1;       // Ocupa índices 1 a 26
    private static final int COMP_OFFSET = 27;     // Ocupa índices 27 a 36
    private static final int SINK = 37;
    private static final int TOTAL_VERTICES = 38;  // Total fixo de nós (0 a 37)

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Inicializa as variáveis para o primeiro caso de teste (dia)
        FlowNetwork network = new FlowNetwork(TOTAL_VERTICES);
        int totalDemanda = 0;
        boolean hasData = false; // Flag para saber se lemos alguma linha neste dia

        while (scanner.hasNextLine()) {
            String linha = scanner.nextLine();

            // Condição 1: A linha está vazia. Isso indica o FIM das leituras de um dia.
            if (linha.trim().isEmpty()) {
                if (hasData) {
                    // Executa o algoritmo para os dados acumulados
                    resolverDia(network, totalDemanda);

                    // Prepara o terreno (zera tudo) para o PRÓXIMO dia
                    network = new FlowNetwork(TOTAL_VERTICES);
                    totalDemanda = 0;
                    hasData = false;
                }
                continue; // Pula para a próxima iteração do while
            }

            // Condição 2: A linha tem dados. Vamos construir o grafo!

            // Se for a primeira linha que lemos para esse dia, inicializamos o Bloco 3
            if (!hasData) {
                for (int c = 0; c < 10; c++) {
                    network.addEdge(new FlowEdge(COMP_OFFSET + c, SINK, 1.0));
                }
                hasData = true;
            }

            // Extrai as informações da linha (Ex: "P5 56789;")
            char appChar = linha.charAt(0);
            int demandaApp = linha.charAt(1) - '0';
            totalDemanda += demandaApp;

            // Mapeia o caractere da aplicação para o índice do vértice
            int vertexApp = APP_OFFSET + (appChar - 'A');

            // --- BLOCO 1: Origem (SOURCE) -> Aplicação ---
            FlowEdge edgeOrigemApp = new FlowEdge(SOURCE, vertexApp, demandaApp);
            network.addEdge(edgeOrigemApp);

            // --- BLOCO 2: Aplicação -> Computadores Compatíveis ---
            String computadoresValidos = linha.substring(3, linha.indexOf(';'));

            // IMPORTANTE: Iterando de trás para frente para driblar a inversão da classe Bag
            // e garantir a saída idêntica ao gabarito do UVa!
            for (int i = computadoresValidos.length() - 1; i >= 0; i--) {
                int compNum = computadoresValidos.charAt(i) - '0';
                int vertexComputador = COMP_OFFSET + compNum;

                FlowEdge edgeAppComp = new FlowEdge(vertexApp, vertexComputador, 1.0);
                network.addEdge(edgeAppComp);
            }
        }

        // Condição 3: O arquivo acabou (EOF - End Of File).
        // Se o último dia não teve uma linha vazia no final, precisamos processá-lo agora!
        if (hasData) {
            resolverDia(network, totalDemanda);
        }

        scanner.close();
    }

    /**
     * Método auxiliar isolado apenas para executar o algoritmo de fluxo
     * e imprimir o resultado. Isso deixa a Main muito mais limpa.
     */
    private static void resolverDia(FlowNetwork network, int totalDemanda) {
        // 1. Roda o algoritmo
        FordFulkerson maxFlow = new FordFulkerson(network, SOURCE, SINK);

        // 2. Verifica se a alocação foi possível
        if ((int) maxFlow.value() != totalDemanda) {
            System.out.println("!");
        } else {
            // 3. Sucesso! Reconstruir a string
            char[] alocacao = new char[10];
            for (int i = 0; i < 10; i++) alocacao[i] = '_';

            for (int c = 0; c < 10; c++) {
                int vertexComputador = COMP_OFFSET + c;

                for (FlowEdge edge : network.adj(vertexComputador)) {
                    if (edge.to() == vertexComputador && edge.flow() == 1.0) {
                        int vertexApp = edge.from();
                        alocacao[c] = (char) ('A' + (vertexApp - APP_OFFSET));
                        break;
                    }
                }
            }

            // Imprime a configuração gerada
            System.out.println(new String(alocacao));
        }
    }
}