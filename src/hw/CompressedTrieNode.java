package hw;

public class CompressedTrieNode {

    private RobinHoodHashing edgeTable; // λίστα από ακμές/παιδιά

    public boolean isEndOfWord; // true αν εδώ τελειώνει λέξη
    public int importance; //how many times we found the world

    public CompressedTrieNode() {
        this.edgeTable = new RobinHoodHashing();
        this.isEndOfWord = false;
        this.importance = 0;
        /* Υλοποίηση */ }

    public void insertEdge(Edge edge) {
        edgeTable.insert(edge);
        /*
		 * εισαγωγή ακμής στη λίστα με τα παιδιά
         */ }

    public Edge getEdgeByFirstChar(char c) {
        return edgeTable.getEdge(c);
        /*
		 * // Εύρεση ακμής με βάση το πρώτο γράμμα
         */ }

    public java.util.List<Edge> getAllEdges() {
        return edgeTable.getAllEdges();
    }
public RobinHoodHashing getEdgeTable() {
    return edgeTable;
}

public boolean hasEdges() {
    return edgeTable != null;
}

}
