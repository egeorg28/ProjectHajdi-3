package hw;

// ========== EDGE CLASS ==========
/**
 * What this class represents: - An Edge stores a CHUNK (string label) that
 * represents multiple characters, and a pointer (child) to another
 * CompressedTrieNode. - It also stores flags that help your custom hashtable
 * manage the edge.
 *
 * Example: If inserting "stop", your trie could store: root --"st"--> node1
 * --"op"--> node2(isEnd=true, importance=0) These "st" and "op" chunks are
 * stored inside Edge.label.
 */
public class Edge {

    public String label; // The string chunk stored on this edge (example: "sto", "ear", "st", etc).

    public CompressedTrieNode child; // The node this edge leads to (your next trie node in the path).

    public boolean occupied = true; // Always true when created, used in your hash table logic to mark active edges.

    // ========== CONSTRUCTOR ==========
    /**
     * What this function does: - Takes a label chunk and a child node - Stores
     * them inside a new Edge object - Sets occupied=true, meaning this edge is
     * active.
     *
     * Example call: Edge e = new Edge("bear", nextNode); → e.label = "bear",
     * e.child = nextNode, e.occupied = true
     */
    public Edge(String label, CompressedTrieNode child) {
        this.label = label.toLowerCase(); // Convert label to lowercase so all stored words are uniform.
        this.child = child; // Save pointer to the child trie node that this edge connects to.
        this.occupied = true; // Mark the edge as active/used.
    }

}
