package hw;


public class RobinHoodHashing {

    // ================== ΠΕΔΙΑ ΤΟΥ HASHING ==================
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

    private int hash(int key) {
        int h = key % capacity;
        if (h < 0) h += capacity;
        return h;
    }

    // ============= INSERT =============
    public void insert(int key) {
        int index = hash(key);
        int probeLen = 0;
        Edge newEdge = new Edge(key);

        while (true) {
            if (table[index] == null || !table[index].occupied) {
                table[index] = newEdge;
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

                int existingHome = hash(existing.key);
                int existingProbe =
                        (index - existingHome + capacity) % capacity;

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

    // ============= SEARCH =============
    public boolean search(int key) {
        int index = hash(key);
        int probeLen = 0;

        while (probeLen <= maxProbeLength) {
            Edge e = table[index];

            if (e == null) {
                return false;
            }
            if (e.occupied && e.key == key) {
                return true;
            }
            probeLen++;
            index = (index + 1) % capacity;
        }
        return false;
    }

    // ============= REHASH =============
    private void rehash() {
        if (primeIndex + 1 >= PRIMES.length) {
            System.out.println("Cannot rehash: reached max prime capacity.");
            return;
        }

        int oldCapacity = capacity;
        Edge[] oldTable = table;

        primeIndex++;
        capacity = PRIMES[primeIndex];
        table = new Edge[capacity];
        size = 0;
        maxProbeLength = 0;

        for (int i = 0; i < oldCapacity; i++) {
            Edge e = oldTable[i];
            if (e != null && e.occupied) {
                insert(e.key);
            }
        }
    }

    public void printTable() {
        System.out.println("Table (capacity=" + capacity +
                           ", size=" + size +
                           ", maxProbeLength=" + maxProbeLength + ")");
        for (int i = 0; i < capacity; i++) {
            System.out.println(i + " -> " + (table[i] == null ? "_" : table[i]));
        }
    }
}
