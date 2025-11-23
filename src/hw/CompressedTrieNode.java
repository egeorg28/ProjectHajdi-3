package hw;

public class CompressedTrieNode {

	private RobinHoodHashing edgeTable; // λίστα από ακμές/παιδιά

	public boolean isEndOfWord; // true αν εδώ τελειώνει λέξη
	public int importance; //how many times we found the world

	public CompressedTrieNode() {
		this.edgeTable = new RobinHoodHashing();
		this.isEndOfWord = false;
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
}
