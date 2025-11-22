package hw;

public class CompressedTrieNode {

	private SinglyLinkedList edgeList; // λίστα από ακμές/παιδιά

	public boolean isEndOfWord; // true αν εδώ τελειώνει λέξη

	public CompressedTrieNode() {
		this.edgeList = new SinglyLinkedList();
		this.isEndOfWord = false;
		/* Υλοποίηση */ }

	public void insertEdge(Edge edge) {
		edgeList.insert(edge);
		/*
		 * εισαγωγή ακμής στη λίστα με τα παιδιά
		 */ }

	public Edge getEdgeByFirstChar(char c) {
		return edgeList.getEdge(c);
		/*
		 * // Εύρεση ακμής με βάση το πρώτο γράμμα
		 */ }
}
