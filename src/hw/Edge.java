package hw;

//Οι πληροφορίες στον κόμβο της λίστας
public class Edge {
	public String label; // Η συμβολοσειρά της ακμής
	public CompressedTrieNode child; // Ο κόμβος παιδί
	public boolean occupied=true;

	public Edge(String label, CompressedTrieNode child) {
		this.label = label;
		this.child = child;
		this.occupied=true;

		/*
		 * Υλοποίηση
		 */ }
}