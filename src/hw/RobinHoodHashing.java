package hw;
import java.util.ArrayList;
import java.util.List;

public class RobinHoodHashing {

    private Edge[] table;
    private int capacity;
    private int size;
    private int maxProbeLength;
    private int primeIndex;

    private static final int[] PRIMES = {3, 7, 11, 17, 23, 29};

    public RobinHoodHashing() {
        this.primeIndex = 0;
        this.capacity = PRIMES[primeIndex];
        this.table = new Edge[capacity];
        this.size = 0;
        this.maxProbeLength = 0;
    }

    // Hash με βάση το ΠΡΩΤΟ γράμμα της ακμής
    private int hash(char c) {
        int h = (c - 'a') % capacity;
        if (h < 0) h += capacity;
        return h;
    }

    // ============= INSERT EDGE =============
    public void insert(Edge edge) {
        char c = edge.label.charAt(0);
        int index = hash(c);
        int probeLen = 0;
        Edge newEdge = edge;

        while (true) {
            if (table[index] == null || !table[index].occupied) {
                table[index] = newEdge;
                newEdge.occupied = true;
                size++;

                if (probeLen > maxProbeLength) {
                    maxProbeLength = probeLen;
                }

                if (size * 10 > 9 * capacity) {
                    rehash();
                }
                return;
            } else {
                Edge existing = table[index];
                char exC = existing.label.charAt(0);
                int home = hash(exC);
                int existingProbe =
                        (index - home + capacity) % capacity;

                if (probeLen > existingProbe) {
                    Edge temp = existing;
                    table[index] = newEdge;
                    newEdge = temp;
                    probeLen = existingProbe;
                }

                probeLen++;
                index = (index + 1) % capacity;
            }
        }
    }

    // ============= GET EDGE BY FIRST CHAR =============
    public Edge getEdge(char c) {
        int index = hash(c);
        int probeLen = 0;

        while (probeLen <= maxProbeLength) {
            Edge e = table[index];

            if (e == null) {
                return null;
            }

            if (e.occupied &&
                !e.label.isEmpty() &&
                e.label.charAt(0) == c) {
                return e;
            }

            probeLen++;
            index = (index + 1) % capacity;
        }

        return null;
    }

    // ============= REHASH =============
    private void rehash() {
        if (primeIndex + 1 >= PRIMES.length) {
            return;
        }

        Edge[] oldTable = table;
        int oldCapacity = capacity;

        primeIndex++;
        capacity = PRIMES[primeIndex];
        table = new Edge[capacity];
        size = 0;
        maxProbeLength = 0;

        for (int i = 0; i < oldCapacity; i++) {
            Edge e = oldTable[i];
            if (e != null && e.occupied) {
                insert(e);
            }
        }
    }

        // ============= GET ALL EDGES (για DFS στο CompressedTrie) =============
    public List<Edge> getAllEdges() {
        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            Edge e = table[i];
            if (e != null && e.occupied) {
                edges.add(e);
            }
        }
        return edges;
    }

public Edge[] getTable() {
    return table;
}

public int getCapacity() {
    return capacity;
}
}

        

