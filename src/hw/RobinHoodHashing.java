package hw;

/**
 * RobinHoodHashing implements an open-addressing hash table
 * that stores Edge objects. Collisions are resolved using the
 * Robin Hood hashing strategy, and the table size grows using
 * a sequence of prime capacities.
 */
public class RobinHoodHashing {

    // The backing array storing edges in the hash table
    private Edge[] table;
    // Current capacity of the hash table (length of table array)
    private int capacity;
    // Number of active (occupied) entries currently stored
    private int size;
    // Maximum probe length encountered so far (used to limit search)
    private int maxProbeLength;
    // Index into the PRIMES array indicating which capacity we are using
    private int primeIndex;

    // Predefined sequence of prime numbers used for resizing (rehashing)
    private static final int[] PRIMES = {3, 7, 11, 17, 23, 29};

    /**
     * Constructs a new RobinHoodHashing table with an initial
     * capacity taken from the first element of the PRIMES array.
     */
    public RobinHoodHashing() {
        this.primeIndex = 0;
        this.capacity = PRIMES[primeIndex];
        this.table = new Edge[capacity];
        this.size = 0;
        this.maxProbeLength = 0;
    }

    /**
     * Hashes a character to an index in the table.
     * The hash is based on the first character of the edge label.
     *
     * @param c the character to hash (expected 'a'..'z')
     * @return an index in [0, capacity)
     */
    // Hash με βάση το ΠΡΩΤΟ γράμμα της ακμής
    private int hash(char c) {
        int h = (c - 'a') % capacity;
        if (h < 0) h += capacity;
        return h;
    }

    /**
     * Inserts an Edge into the hash table using Robin Hood hashing.
     * If the target position is occupied, the algorithm compares
     * probe lengths and may swap entries so that elements with
     * larger probe length "steal" positions from those with smaller
     * probe lengths (the "Robin Hood" idea).
     *
     * Also triggers a rehash if the load factor exceeds 90%.
     *
     * @param edge the edge to insert (assumed non-null, label non-empty)
     */
    // ============= INSERT EDGE =============
    public void insert(Edge edge) {
        char c = edge.label.charAt(0);
        int index = hash(c);
        int probeLen = 0;
        Edge newEdge = edge;

        while (true) {
            // Case 1: empty or logically deleted slot → place newEdge here
            if (table[index] == null || !table[index].occupied) {
                table[index] = newEdge;
                newEdge.occupied = true;
                size++;

                // Update maximum probe length seen so far
                if (probeLen > maxProbeLength) {
                    maxProbeLength = probeLen;
                }

                // If load factor > 0.9, rehash to a bigger table
                if (size * 10 > 9 * capacity) {
                    rehash();
                }
                return;
            } else {
                // Case 2: slot already contains an active edge
                Edge existing = table[index];
                char exC = existing.label.charAt(0);
                int home = hash(exC);
                // existingProbe = distance from its "home" hash index
                int existingProbe =
                        (index - home + capacity) % capacity;

                // If the new element has probed farther than the existing one,
                // we swap them so that the "unluckier" (bigger probe) element
                // gets the better position (Robin Hood principle).
                if (probeLen > existingProbe) {
                    Edge temp = existing;
                    table[index] = newEdge;
                    newEdge = temp;
                    probeLen = existingProbe;
                }

                // Move to the next slot (linear probing)
                probeLen++;
                index = (index + 1) % capacity;
            }
        }
    }

    /**
     * Looks up an Edge by its first character. Uses the same
     * probing sequence as insert, but stops searching when
     * the probe length exceeds maxProbeLength, or when a null
     * slot is encountered (meaning the element is not present).
     *
     * @param c the first character of the desired edge's label
     * @return the matching Edge or null if not found
     */
    // ============= GET EDGE BY FIRST CHAR =============
    public Edge getEdge(char c) {
        int index = hash(c);
        int probeLen = 0;

        // We only need to search up to the current max probe length
        while (probeLen <= maxProbeLength) {
            Edge e = table[index];

            // Reached an empty slot: the edge does not exist
            if (e == null) {
                return null;
            }

            // Check if this slot contains a live edge with matching first char
            if (e.occupied &&
                !e.label.isEmpty() &&
                e.label.charAt(0) == c) {
                return e;
            }

            // Otherwise continue probing
            probeLen++;
            index = (index + 1) % capacity;
        }

        // Not found within max probe range
        return null;
    }

    /**
     * Rehashes the table into a larger one using the next prime capacity.
     * All active entries from the old table are reinserted into the new one,
     * recomputing their positions with the new capacity.
     *
     * If we have already used the last prime in PRIMES, rehash() does nothing.
     */
    // ============= REHASH =============
    private void rehash() {
        if (primeIndex + 1 >= PRIMES.length) {
            // No larger prime capacity available; stop rehashing
            return;
        }

        Edge[] oldTable = table;
        int oldCapacity = capacity;

        // Move to the next prime capacity
        primeIndex++;
        capacity = PRIMES[primeIndex];
        table = new Edge[capacity];
        size = 0;
        maxProbeLength = 0;

        // Reinsert all occupied entries into the new table
        for (int i = 0; i < oldCapacity; i++) {
            Edge e = oldTable[i];
            if (e != null && e.occupied) {
                insert(e);
            }
        }
    }

    /**
     * Returns the internal backing array of edges.
     * NOTE: This exposes the internal representation and
     * should normally be used only for debugging or analysis.
     *
     * @return the Edge[] table used by this hash structure
     */
    public Edge[] getTable() {
        return table;
    }

    /**
     * Returns the current capacity (length of the internal table array).
     *
     * @return current capacity of the hash table
     */
    public int getCapacity() {
        return capacity;
    }
}
