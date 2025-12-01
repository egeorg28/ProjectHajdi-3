package hw;

public class CompressedTrieNode {

    private RobinHoodHashing edgeTable; // Stores all outgoing edges (children) of this node using your custom hashing structure.

    public boolean isEndOfWord; // True if a valid word ends exactly at this node.
    public int importance; // Counts how many times the word ending here was found or used.

    // ========== CONSTRUCTOR ==========
    /**
     * What it does: - Creates an empty trie node. - Also creates an empty edge
     * table (list of children).
     *
     * Example: CompressedTrieNode n = new CompressedTrieNode(); → n.isEndOfWord
     * = false, n.importance = 0, and it has no edges yet.
     */
    public CompressedTrieNode() {
        this.edgeTable = new RobinHoodHashing(); // Initialize the child-edge structure so this node can store children.
        this.isEndOfWord = false; // Default = no word is ending here yet.
        this.importance = 0; // Default importance when node is created is 0, meaning it has not been counted.
    }

    // ========== INSERT EDGE ==========
    /**
     * What it does: - Adds a new Edge object (string label + child pointer)
     * into this node’s edge table.
     *
     * Example: n.insertEdge(new Edge("sto", childNode)); → adds that edge so
     * this node now has a branch starting with 's'.
     */
    public void insertEdge(Edge edge) {
        edgeTable.insert(edge); // Calls insert on your hash structure to store the new edge.
    }

    // ========== GET EDGE BY FIRST CHAR ==========
    /**
     * What it does: - Searches inside the edgeTable for an edge whose label
     * STARTS with character `c` - If such an edge exists, returns it. If not,
     * returns null.
     *
     * Example: n.getEdgeByFirstChar('b') → might return edge with label="bear"
     * n.getEdgeByFirstChar('x') → returns null if no label starts with 'x'
     */
    public Edge getEdgeByFirstChar(char c) {
        return edgeTable.getEdge(c); // Delegates search to your hash table.
    }

    // ========== GIVE ACCESS TO EDGE TABLE ==========
    /**
     * What it does: - Returns the actual edge table object. - This is useful if
     * another method needs to iterate or inspect all stored edges.
     */
    public RobinHoodHashing getEdgeTable() {
        return edgeTable; // Simply returns the structure that contains the children edges.
    }

    // ========== CHECK IF NODE HAS EDGES ==========
    /**
     * What it does: - Returns true if edgeTable is not null (it always exists
     * in your code, but you keep this as a check).
     *
     * Note: It does NOT mean the list is non-empty. It just means "edge
     * structure exists".
     */
    public boolean hasEdges() {
        return edgeTable != null; // Will return true because constructor always initializes it.
    }
}
