package hw;






public class SinglyLinkedList {
	
	private static class Node {
		Edge edge; // Η πληροφορία που βρίσκεται στον κόμβο
		Node next; // Δείκτης στον επ. κόμβο της λίστας

		Node(Edge edge) {
			this.edge=edge;
			this.next=null;
			/* Υλοποίηση */ }
	}

	protected Node head; // Κόμβος κεφαλή της λίστας

	public SinglyLinkedList() {
		this.head=null;
		
		/* Υλοποίηση */ }

	public void insert(Edge edge) {
		Node newNode=new Node(edge);
		newNode.next=head;
		head=newNode;
		/* Εισαγωγή νέας ακμής */ }

	public Edge getEdge(char c) {
		 Node current = head;
	        while (current != null) {
	            if (!current.edge.label.isEmpty() &&
	                current.edge.label.charAt(0) == c) {
	                return current.edge;
	            }
	            current = current.next;
	        }
	        return null;
	    }
		
		
		/*
		 * Επιστροφή ακμής που το label της ξεκινά με τον χαρακτήρα c
		 */ 

}



	


