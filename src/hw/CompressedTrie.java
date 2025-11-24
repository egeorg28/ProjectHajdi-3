package hw;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CompressedTrie {
	CompressedTrieNode root; // Η ρίζα του δέντρου

	public CompressedTrie() {
		this.root=new CompressedTrieNode();
		/* δημιουργία κόμβου ρίζας */}

	public void insert(String word) {
        if (word == null || word.isEmpty()) return;
        insertRecursive(root, word);
    }

    public boolean search(String word) {
        if (word == null || word.isEmpty()) return false;
        return searchRecursive(root, word);
    }
    public void increaseImportance(String word) {
    increaseImportanceRecursive(root, word);
}

    // =====================================================
    // PRIVATE ΒΟΗΘΗΤΙΚΕΣ
    // =====================================================

    private void insertRecursive(CompressedTrieNode node, String word) {
        // 1) Αν δεν έχει μείνει τίποτα, αυτός ο κόμβος είναι τέλος λέξης
        if (word.length() == 0) {
            node.isEndOfWord = true;
            return;
        }

        char first = word.charAt(0);
        Edge edge = node.getEdgeByFirstChar(first);

        // 2) Δεν υπάρχει καμία ακμή που να ταιριάζει στο πρώτο γράμμα → νέα ακμή με όλη τη λέξη
        if (edge == null) {
            CompressedTrieNode child = new CompressedTrieNode();
            child.isEndOfWord = true;
            node.insertEdge(new Edge(word, child));
            return;
        }

        String label  = edge.label;
        String common = commonPrefix(label, word);

        // Safety αν για κάποιο λόγο δεν έχουν κοινό prefix (σπάνιο αν getEdgeByFirstChar δουλεύει σωστά)
        if (common.length() == 0) {
            CompressedTrieNode child = new CompressedTrieNode();
            child.isEndOfWord = true;
            node.insertEdge(new Edge(word, child));
            return;
        }

        // ============= ΠΕΡΙΠΤΩΣΕΙΣ =============

        // Περίπτωση Α: ακριβής ταύτιση (label == word)
        if (common.length() == label.length() && common.length() == word.length()) {
            edge.child.isEndOfWord = true;
            return;
        }

        // Περίπτωση Β: label prefix of word (label ⊂ word)
        // π.χ. label = "car", word = "cart"
        if (common.length() == label.length() && common.length() < word.length()) {
            String restWord = word.substring(common.length());  // "t"
            insertRecursive(edge.child, restWord);
            return;
        }

        // Περίπτωση Γ: word prefix of label (word ⊂ label)
        // π.χ. label = "carton", word = "car"
        if (common.length() == word.length() && common.length() < label.length()) {
            String remainingLabel = label.substring(common.length()); // υπόλοιπο του label, π.χ. "ton"

            // Ο παλιός child (εκεί που πήγαινε η ακμή "carton")
            CompressedTrieNode oldChild = edge.child;

            // Νέος ενδιάμεσος κόμβος, εδώ τελειώνει η νέα λέξη
            CompressedTrieNode middle = new CompressedTrieNode();
            middle.isEndOfWord = true;

            // Τροποποιούμε την ακμή ώστε να είναι "car" → middle
            edge.label = common;
            edge.child = middle;

            // Από τον middle φεύγει ακμή με το υπόλοιπο "ton" προς τον παλιό child
            middle.insertEdge(new Edge(remainingLabel, oldChild));

            return;
        }

        // Περίπτωση Δ: partial match (common μικρότερο και από label και από word)
        // π.χ. label = "car", word = "cat", common = "ca"
        if (common.length() < label.length() && common.length() < word.length()) {
            String edgeSuffix = label.substring(common.length()); // "r"
            String wordSuffix = word.substring(common.length());  // "t"

            CompressedTrieNode oldChild = edge.child;

            // Νέος ενδιάμεσος κόμβος
            CompressedTrieNode middle = new CompressedTrieNode();

            // Τροποποιούμε την παλιά ακμή ώστε να είναι "ca" → middle
            edge.label = common;
            edge.child = middle;

            // Από τον middle βγαίνει ακμή για το παλιό suffix, "r"
            middle.insertEdge(new Edge(edgeSuffix, oldChild));

            // Και ακμή για τη νέα λέξη, suffix "t"
            CompressedTrieNode newChild = new CompressedTrieNode();
            newChild.isEndOfWord = true;
            middle.insertEdge(new Edge(wordSuffix, newChild));

            return;
        }

        // Αν φτάσουμε εδώ, κάτι δεν πήγε όπως το περιμένουμε, αλλά δεν κάνουμε κάτι άλλο.
    }

    private void increaseImportanceRecursive(CompressedTrieNode node, String word) {
    if (word.length() == 0) {
        node.importance++;      // ΑΥΞΗΣΗ ΣΗΜΑΝΤΙΚΟΤΗΤΑΣ
        return;
    }

    char first = word.charAt(0);
    Edge edge = node.getEdgeByFirstChar(first);

    if (edge == null) return; // δεν υπάρχει

    String label = edge.label;

    if (word.startsWith(label)) {
        String rest = word.substring(label.length());
        increaseImportanceRecursive(edge.child, rest);
    }
}

    private boolean searchRecursive(CompressedTrieNode node, String word) {
        // Αν έχουμε τελειώσει τη λέξη, απλά κοιτάμε αν αυτός ο κόμβος είναι τέλος λέξης
        if (word.length() == 0) {
            return node.isEndOfWord;
        }

        char first = word.charAt(0);
        Edge edge = node.getEdgeByFirstChar(first);
        if (edge == null) {
            return false;
        }

        String label = edge.label;

        // Αν το word ξεκινάει με το label, συνεχίζουμε πιο κάτω με το υπόλοιπο
        if (word.startsWith(label)) {
            String rest = word.substring(label.length());
            return searchRecursive(edge.child, rest);
        }

        // Αν το label δεν είναι prefix της word, τότε δεν υπάρχει μονοπάτι
        return false;
    }

    // Βοηθάει να βρούμε το κοινό πρόθεμα δύο strings
    private String commonPrefix(String a, String b) {
        int len = Math.min(a.length(), b.length());
        int i = 0;
        while (i < len && a.charAt(i) == b.charAt(i)) {
            i++;
        }
        return a.substring(0, i);
    }
    
    public void loadDictionary(String filename) {
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
        String line;
        while ((line = br.readLine()) != null) {
            String word = line.trim();
            if (word.isEmpty()) continue;

            word = word.toLowerCase();   // 🔹 ΟΛΑ σε πεζά
            insert(word);                // 🔹 Χρησιμοποιείς ΤΟΝ CompressedTrie που ήδη έφτιαξες
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}

public void updateImportanceFromText(String filename) {
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
        String line;
        while ((line = br.readLine()) != null) {

            // Σπάσε τη γραμμή σε λέξεις
            String[] tokens = line.split("\\W+");

            for (String token : tokens) {
                if (!token.isEmpty()) {
                    String word = token.toLowerCase();

                    // Αν η λέξη είναι στο λεξικό, αύξησε τη σημαντικότητά της
                    if (search(word)) {
                        increaseImportance(word);
                    }
                }
            }
        }

    } catch (IOException e) {
        e.printStackTrace();
    }
}


}
