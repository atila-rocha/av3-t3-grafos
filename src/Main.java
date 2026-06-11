import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {

    private static final int SOURCE = 0;
    private static final int APP_OFFSET = 1;
    private static final int COMP_OFFSET = 27;
    private static final int SINK = 37;
    private static final int TOTAL_VERTICES = 38;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        FlowNetwork network = new FlowNetwork(TOTAL_VERTICES);
        int totalDemanda = 0;
        boolean hasData = false; // Flag para saber se lemos alguma linha neste dia

        while (scanner.hasNextLine()) {
            String linha = scanner.nextLine();
            if (linha.trim().isEmpty()) {
                if (hasData) {
                    resolverDia(network, totalDemanda);
                    network = new FlowNetwork(TOTAL_VERTICES);
                    totalDemanda = 0;
                    hasData = false;
                }
                continue;
            }

            if (!hasData) {
                for (int c = 0; c < 10; c++) {
                    network.addEdge(new FlowEdge(COMP_OFFSET + c, SINK, 1.0));
                }
                hasData = true;
            }

            char appChar = linha.charAt(0);
            int demandaApp = linha.charAt(1) - '0';
            totalDemanda += demandaApp;
            int vertexApp = APP_OFFSET + (appChar - 'A');

            FlowEdge edgeOrigemApp = new FlowEdge(SOURCE, vertexApp, demandaApp);
            network.addEdge(edgeOrigemApp);

            String computadoresValidos = linha.substring(3, linha.indexOf(';'));
            for (int i = computadoresValidos.length() - 1; i >= 0; i--) {
                int compNum = computadoresValidos.charAt(i) - '0';
                int vertexComputador = COMP_OFFSET + compNum;

                FlowEdge edgeAppComp = new FlowEdge(vertexApp, vertexComputador, 1.0);
                network.addEdge(edgeAppComp);
            }
        }

        if (hasData) {
            resolverDia(network, totalDemanda);
        }

        scanner.close();
    }

    private static void resolverDia(FlowNetwork network, int totalDemanda) {
        FordFulkerson maxFlow = new FordFulkerson(network, SOURCE, SINK);
        if ((int) maxFlow.value() != totalDemanda) {
            System.out.println("!");
        } else {
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
            System.out.println(new String(alocacao));
        }
    }
}

class Bag<Item> implements Iterable<Item> {
    private Node<Item> first;    // beginning of bag
    private int n;               // number of elements in bag

    // helper linked list class
    private static class Node<Item> {
        private Item item;
        private Node<Item> next;
    }


    public Bag() {
        first = null;
        n = 0;
    }


    public boolean isEmpty() {
        return first == null;
    }


    public int size() {
        return n;
    }


    public void add(Item item) {
        Node<Item> oldfirst = first;
        first = new Node<Item>();
        first.item = item;
        first.next = oldfirst;
        n++;
    }

    public Iterator<Item> iterator()  {
        return new LinkedIterator(first);
    }

    private class LinkedIterator implements Iterator<Item> {
        private Node<Item> current;

        public LinkedIterator(Node<Item> first) {
            current = first;
        }

        public boolean hasNext()  {
            return current != null;
        }

        public Item next() {
            if (!hasNext()) throw new NoSuchElementException();
            Item item = current.item;
            current = current.next;
            return item;
        }
    }
}

class FlowEdge {
    // to deal with floating-point roundoff errors
    private static final double FLOATING_POINT_EPSILON = 1.0E-10;

    private final int v;             // from
    private final int w;             // to
    private final double capacity;   // capacity
    private double flow;             // flow

    /**
     * Initializes an edge from vertex {@code v} to vertex {@code w} with
     * the given {@code capacity} and zero flow.
     * @param v the tail vertex
     * @param w the head vertex
     * @param capacity the capacity of the edge
     * @throws IllegalArgumentException if either {@code v} or {@code w}
     *    is a negative integer
     * @throws IllegalArgumentException if {@code capacity < 0.0}
     */
    public FlowEdge(int v, int w, double capacity) {
        if (v < 0) throw new IllegalArgumentException("vertex index must be a non-negative integer");
        if (w < 0) throw new IllegalArgumentException("vertex index must be a non-negative integer");
        if (!(capacity >= 0.0)) throw new IllegalArgumentException("Edge capacity must be non-negative");
        this.v         = v;
        this.w         = w;
        this.capacity  = capacity;
        this.flow      = 0.0;
    }

    /**
     * Initializes an edge from vertex {@code v} to vertex {@code w} with
     * the given {@code capacity} and {@code flow}.
     * @param v the tail vertex
     * @param w the head vertex
     * @param capacity the capacity of the edge
     * @param flow the flow on the edge
     * @throws IllegalArgumentException if either {@code v} or {@code w}
     *    is a negative integer
     * @throws IllegalArgumentException if {@code capacity} is negative
     * @throws IllegalArgumentException unless {@code flow} is between
     *    {@code 0.0} and {@code capacity}.
     */
    public FlowEdge(int v, int w, double capacity, double flow) {
        if (v < 0) throw new IllegalArgumentException("vertex index must be a non-negative integer");
        if (w < 0) throw new IllegalArgumentException("vertex index must be a non-negative integer");
        if (!(capacity >= 0.0))  throw new IllegalArgumentException("edge capacity must be non-negative");
        if (!(flow <= capacity)) throw new IllegalArgumentException("flow exceeds capacity");
        if (!(flow >= 0.0))      throw new IllegalArgumentException("flow must be non-negative");
        this.v         = v;
        this.w         = w;
        this.capacity  = capacity;
        this.flow      = flow;
    }

    /**
     * Initializes a flow edge from another flow edge.
     * @param e the edge to copy
     */
    public FlowEdge(FlowEdge e) {
        this.v         = e.v;
        this.w         = e.w;
        this.capacity  = e.capacity;
        this.flow      = e.flow;
    }

    /**
     * Returns the tail vertex of the edge.
     * @return the tail vertex of the edge
     */
    public int from() {
        return v;
    }

    /**
     * Returns the head vertex of the edge.
     * @return the head vertex of the edge
     */
    public int to() {
        return w;
    }

    /**
     * Returns the capacity of the edge.
     * @return the capacity of the edge
     */
    public double capacity() {
        return capacity;
    }

    /**
     * Returns the flow on the edge.
     * @return the flow on the edge
     */
    public double flow() {
        return flow;
    }

    /**
     * Returns the endpoint of the edge that is different from the given vertex
     * (unless the edge represents a self-loop in which case it returns the same vertex).
     * @param vertex one endpoint of the edge
     * @return the endpoint of the edge that is different from the given vertex
     *   (unless the edge represents a self-loop in which case it returns the same vertex)
     * @throws IllegalArgumentException if {@code vertex} is not one of the endpoints
     *   of the edge
     */
    public int other(int vertex) {
        if      (vertex == v) return w;
        else if (vertex == w) return v;
        else throw new IllegalArgumentException("invalid endpoint");
    }

    /**
     * Returns the residual capacity of the edge in the direction
     *  to the given {@code vertex}.
     * @param vertex one endpoint of the edge
     * @return the residual capacity of the edge in the direction to the given vertex
     *   If {@code vertex} is the tail vertex, the residual capacity equals
     *   {@code capacity() - flow()}; if {@code vertex} is the head vertex, the
     *   residual capacity equals {@code flow()}.
     * @throws IllegalArgumentException if {@code vertex} is not one of the endpoints of the edge
     */
    public double residualCapacityTo(int vertex) {
        if      (vertex == v) return flow;              // backward edge
        else if (vertex == w) return capacity - flow;   // forward edge
        else throw new IllegalArgumentException("invalid endpoint");
    }

    /**
     * Increases the flow on the edge in the direction to the given vertex.
     * If {@code vertex} is the tail vertex, this increases the flow on the edge by {@code delta};
     * if {@code vertex} is the head vertex, this decreases the flow on the edge by {@code delta}.
     * @param vertex one endpoint of the edge
     * @param delta amount by which to increase flow
     * @throws IllegalArgumentException if {@code vertex} is not one of the endpoints
     *         of the edge
     * @throws IllegalArgumentException if {@code delta} makes the flow
     *         on the edge either negative or larger than its capacity
     * @throws IllegalArgumentException if {@code delta} is {@code NaN}
     */
    public void addResidualFlowTo(int vertex, double delta) {
        if (!(delta >= 0.0)) throw new IllegalArgumentException("Delta must be non-negative");

        if      (vertex == v) flow -= delta;           // backward edge
        else if (vertex == w) flow += delta;           // forward edge
        else throw new IllegalArgumentException("invalid endpoint");

        // round flow to 0 or capacity if within floating-point precision
        if (Math.abs(flow) <= FLOATING_POINT_EPSILON)
            flow = 0;
        if (Math.abs(flow - capacity) <= FLOATING_POINT_EPSILON)
            flow = capacity;

        if (!(flow >= 0.0))      throw new IllegalArgumentException("Flow is negative");
        if (!(flow <= capacity)) throw new IllegalArgumentException("Flow exceeds capacity");
    }


    /**
     * Returns a string representation of the edge.
     * @return a string representation of the edge
     */
    public String toString() {
        return v + "->" + w + " " + flow + "/" + capacity;
    }

}

class FlowNetwork {
    private static final String NEWLINE = System.getProperty("line.separator");

    private final int V;
    private int E;
    private Bag<FlowEdge>[] adj;

    /**
     * Initializes an empty flow network with {@code V} vertices and 0 edges.
     * @param V the number of vertices
     * @throws IllegalArgumentException if {@code V < 0}
     */
    public FlowNetwork(int V) {
        if (V < 0) throw new IllegalArgumentException("Number of vertices in a Graph must be non-negative");
        this.V = V;
        this.E = 0;
        adj = (Bag<FlowEdge>[]) new Bag[V];
        for (int v = 0; v < V; v++)
            adj[v] = new Bag<FlowEdge>();
    }


    /**
     * Returns the number of vertices in the edge-weighted graph.
     * @return the number of vertices in the edge-weighted graph
     */
    public int V() {
        return V;
    }

    /**
     * Returns the number of edges in the edge-weighted graph.
     * @return the number of edges in the edge-weighted graph
     */
    public int E() {
        return E;
    }

    // throw an IllegalArgumentException unless {@code 0 <= v < V}
    private void validateVertex(int v) {
        if (v < 0 || v >= V)
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V-1));
    }

    /**
     * Adds the edge {@code e} to the network.
     * @param e the edge
     * @throws IllegalArgumentException unless endpoints of edge are between
     *         {@code 0} and {@code V-1}
     */
    public void addEdge(FlowEdge e) {
        int v = e.from();
        int w = e.to();
        validateVertex(v);
        validateVertex(w);
        adj[v].add(e);
        adj[w].add(e);
        E++;
    }

    /**
     * Returns the edges incident on vertex {@code v} (includes both edges pointing to
     * and from {@code v}).
     * @param v the vertex
     * @return the edges incident on vertex {@code v} as an Iterable
     * @throws IllegalArgumentException unless {@code 0 <= v < V}
     */
    public Iterable<FlowEdge> adj(int v) {
        validateVertex(v);
        return adj[v];
    }

    // return list of all edges - excludes self loops
    public Iterable<FlowEdge> edges() {
        Bag<FlowEdge> list = new Bag<FlowEdge>();
        for (int v = 0; v < V; v++)
            for (FlowEdge e : adj(v)) {
                if (e.to() != v)
                    list.add(e);
            }
        return list;
    }


    /**
     * Returns a string representation of the flow network.
     * This method takes time proportional to <em>E</em> + <em>V</em>.
     * @return the number of vertices <em>V</em>, followed by the number of edges <em>E</em>,
     *    followed by the <em>V</em> adjacency lists
     */
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(V + " " + E + NEWLINE);
        for (int v = 0; v < V; v++) {
            s.append(v + ":  ");
            for (FlowEdge e : adj[v]) {
                if (e.to() != v) s.append(e + "  ");
            }
            s.append(NEWLINE);
        }
        return s.toString();
    }
}

class FordFulkerson {
    private static final double FLOATING_POINT_EPSILON = 1.0E-11;

    private final int V;          // number of vertices
    private boolean[] marked;     // marked[v] = true iff s->v path in residual graph
    private FlowEdge[] edgeTo;    // edgeTo[v] = last edge on shortest residual s->v path
    private double value;         // current value of max flow

    /**
     * Compute a maximum flow and minimum cut in the network {@code G}
     * from vertex {@code s} to vertex {@code t}.
     *
     * @param  G the flow network
     * @param  s the source vertex
     * @param  t the sink vertex
     * @throws IllegalArgumentException unless {@code 0 <= s < V}
     * @throws IllegalArgumentException unless {@code 0 <= t < V}
     * @throws IllegalArgumentException if {@code s == t}
     * @throws IllegalArgumentException if initial flow is infeasible
     */
    public FordFulkerson(FlowNetwork G, int s, int t) {
        V = G.V();
        validate(s);
        validate(t);
        if (s == t)               throw new IllegalArgumentException("Source equals sink");
        if (!isFeasible(G, s, t)) throw new IllegalArgumentException("Initial flow is infeasible");

        // while there exists an augmenting path, use it
        value = excess(G, t);
        while (hasAugmentingPath(G, s, t)) {

            // compute bottleneck capacity
            double bottle = Double.POSITIVE_INFINITY;
            for (int v = t; v != s; v = edgeTo[v].other(v)) {
                bottle = Math.min(bottle, edgeTo[v].residualCapacityTo(v));
            }

            // augment flow
            for (int v = t; v != s; v = edgeTo[v].other(v)) {
                edgeTo[v].addResidualFlowTo(v, bottle);
            }

            value += bottle;
        }

        // check optimality conditions
        assert check(G, s, t);
    }

    /**
     * Returns the value of the maximum flow.
     *
     * @return the value of the maximum flow
     */
    public double value()  {
        return value;
    }

    /**
     * Returns true if the specified vertex is on the {@code s} side of the mincut.
     *
     * @param  v vertex
     * @return {@code true} if vertex {@code v} is on the {@code s} side of the mincut;
     *         {@code false} otherwise
     * @throws IllegalArgumentException unless {@code 0 <= v < V}
     */
    public boolean inCut(int v)  {
        validate(v);
        return marked[v];
    }

    // throw an IllegalArgumentException if v is outside prescribed range
    private void validate(int v)  {
        if (v < 0 || v >= V)
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V-1));
    }


    // is there an augmenting path?
    // if so, upon termination edgeTo[] will contain a parent-link representation of such a path
    // this implementation finds a shortest augmenting path (fewest number of edges),
    // which performs well both in theory and in practice
    private boolean hasAugmentingPath(FlowNetwork G, int s, int t) {
        edgeTo = new FlowEdge[G.V()];
        marked = new boolean[G.V()];

        // breadth-first search
        Queue<Integer> queue = new Queue<Integer>();
        queue.enqueue(s);
        marked[s] = true;
        while (!queue.isEmpty() && !marked[t]) {
            int v = queue.dequeue();

            for (FlowEdge e : G.adj(v)) {
                int w = e.other(v);

                // if residual capacity from v to w
                if (e.residualCapacityTo(w) > 0) {
                    if (!marked[w]) {
                        edgeTo[w] = e;
                        marked[w] = true;
                        queue.enqueue(w);
                    }
                }
            }
        }

        // is there an augmenting path?
        return marked[t];
    }



    // return excess flow at vertex v
    private double excess(FlowNetwork G, int v) {
        double excess = 0.0;
        for (FlowEdge e : G.adj(v)) {
            if (v == e.from()) excess -= e.flow();
            else               excess += e.flow();
        }
        return excess;
    }

    // return excess flow at vertex v
    private boolean isFeasible(FlowNetwork G, int s, int t) {

        // check that capacity constraints are satisfied
        for (int v = 0; v < G.V(); v++) {
            for (FlowEdge e : G.adj(v)) {
                if (e.flow() < -FLOATING_POINT_EPSILON || e.flow() > e.capacity() + FLOATING_POINT_EPSILON) {
                    System.err.println("Edge does not satisfy capacity constraints: " + e);
                    return false;
                }
            }
        }

        // check that net flow into a vertex equals zero, except at source and sink
        if (Math.abs(value + excess(G, s)) > FLOATING_POINT_EPSILON) {
            System.err.println("Excess at source = " + excess(G, s));
            System.err.println("Max flow         = " + value);
            return false;
        }
        if (Math.abs(value - excess(G, t)) > FLOATING_POINT_EPSILON) {
            System.err.println("Excess at sink   = " + excess(G, t));
            System.err.println("Max flow         = " + value);
            return false;
        }
        for (int v = 0; v < G.V(); v++) {
            if (v == s || v == t) continue;
            else if (Math.abs(excess(G, v)) > FLOATING_POINT_EPSILON) {
                System.err.println("Net flow out of " + v + " doesn't equal zero");
                return false;
            }
        }
        return true;
    }



    // check optimality conditions
    private boolean check(FlowNetwork G, int s, int t) {

        // check that flow is feasible
        if (!isFeasible(G, s, t)) {
            System.err.println("Flow is infeasible");
            return false;
        }

        // check that s is on the source side of min cut and that t is not on source side
        if (!inCut(s)) {
            System.err.println("source " + s + " is not on source side of min cut");
            return false;
        }
        if (inCut(t)) {
            System.err.println("sink " + t + " is on source side of min cut");
            return false;
        }

        // check that value of min cut = value of max flow
        double mincutValue = 0.0;
        for (int v = 0; v < G.V(); v++) {
            for (FlowEdge e : G.adj(v)) {
                if ((v == e.from()) && inCut(e.from()) && !inCut(e.to()))
                    mincutValue += e.capacity();
            }
        }

        if (Math.abs(mincutValue - value) > FLOATING_POINT_EPSILON) {
            System.err.println("Max flow value = " + value + ", min cut value = " + mincutValue);
            return false;
        }

        return true;
    }
}

class Queue<Item> implements Iterable<Item> {
    private Node<Item> first;    // beginning of queue
    private Node<Item> last;     // end of queue
    private int n;               // number of elements on queue

    // helper linked list class
    private static class Node<Item> {
        private Item item;
        private Node<Item> next;
    }

    /**
     * Initializes an empty queue.
     */
    public Queue() {
        first = null;
        last  = null;
        n = 0;
    }

    /**
     * Returns true if this queue is empty.
     *
     * @return {@code true} if this queue is empty; {@code false} otherwise
     */
    public boolean isEmpty() {
        return first == null;
    }

    /**
     * Returns the number of items in this queue.
     *
     * @return the number of items in this queue
     */
    public int size() {
        return n;
    }

    /**
     * Returns the item least recently added to this queue.
     *
     * @return the item least recently added to this queue
     * @throws NoSuchElementException if this queue is empty
     */
    public Item peek() {
        if (isEmpty()) throw new NoSuchElementException("Queue underflow");
        return first.item;
    }

    /**
     * Adds the item to this queue.
     *
     * @param  item the item to add
     */
    public void enqueue(Item item) {
        Node<Item> oldlast = last;
        last = new Node<Item>();
        last.item = item;
        last.next = null;
        if (isEmpty()) first = last;
        else           oldlast.next = last;
        n++;
    }

    /**
     * Removes and returns the item on this queue that was least recently added.
     *
     * @return the item on this queue that was least recently added
     * @throws NoSuchElementException if this queue is empty
     */
    public Item dequeue() {
        if (isEmpty()) throw new NoSuchElementException("Queue underflow");
        Item item = first.item;
        first = first.next;
        n--;
        if (isEmpty()) last = null;   // to avoid loitering
        return item;
    }

    /**
     * Returns a string representation of this queue.
     *
     * @return the sequence of items in FIFO order, separated by spaces
     */
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (Item item : this) {
            s.append(item);
            s.append(' ');
        }
        return s.toString();
    }

    /**
     * Returns an iterator that iterates over the items in this queue in FIFO order.
     *
     * @return an iterator that iterates over the items in this queue in FIFO order
     */
    public Iterator<Item> iterator()  {
        return new LinkedIterator(first);
    }

    // a linked-list iterator
    private class LinkedIterator implements Iterator<Item> {
        private Node<Item> current;

        public LinkedIterator(Node<Item> first) {
            current = first;
        }

        public boolean hasNext() {
            return current != null;
        }

        public Item next() {
            if (!hasNext()) throw new NoSuchElementException();
            Item item = current.item;
            current = current.next;
            return item;
        }
    }
}