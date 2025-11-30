package hw;

public class Trie {

    
    private TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    // ================== SEARCH ==================
    public boolean search(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        TrieNode node = root;
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            // Δεχόμαστε μόνο μικρά a–z
            if (ch < 'a' || ch > 'z') {
                return false;
            }
            int index = ch - 'a';
            if (node.children[index] == null) {
                return false;
            }
            node = node.children[index];
        }
        return node.isEndOfWord;
    }

    // ================== INSERT ==================
    public void insert(String word) {
        if (word == null || word.isEmpty()) {
            return;
        }
        TrieNode node = root;
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            // Δεχόμαστε μόνο μικρά a–z
            if (ch < 'a' || ch > 'z') {
                System.out.println("Invalid character: " + ch);
                return;
            }
            int index = ch - 'a';
            if (node.children[index] == null) {
                node.children[index] = new TrieNode();
            }
            node = node.children[index];
        }
        node.isEndOfWord = true;
    }

    // ================== DISPLAY (όλες οι λέξεις) ==================
    public void display() {
        displayHelper(root, "");
    }

    // Αναδρομικά τυπώνει όλες τις λέξεις σε αλφαβητική σειρά
    private static void displayHelper(TrieNode node, String prefix) {
        if (node == null) return;

        // Αν αυτός ο κόμβος είναι τέλος λέξης → τυπώνουμε τη λέξη
        if (node.isEndOfWord) {
            System.out.println(prefix);
        }

        // Περνάμε από όλα τα παιδιά a–z
        for (int i = 0; i < TrieNode.ALPHABET_SIZE; i++) {
            if (node.children[i] != null) {
                char ch = (char) ('a' + i);
                displayHelper(node.children[i], prefix + ch);
            }
        }
    }

    // ================== DELETE ==================
    public boolean delete(String key) {
        if (key == null || key.isEmpty()) return false;
        return deleteHelper(root, key, 0);
    }

    // Αναδρομική διαγραφή σύμφωνα με το lab
    private static boolean deleteHelper(TrieNode node, String key, int depth) {
        if (node == null) {
            return false;
        }

        // Αν φτάσαμε στο τέλος της λέξης
        if (depth == key.length()) {
            // Αν αυτός ο κόμβος ΔΕΝ ήταν τέλος λέξης → η λέξη δεν υπήρχε
            if (!node.isEndOfWord) {
                return false;
            }
            // Σταματά να είναι τέλος λέξης
            node.isEndOfWord = false;

            // ΕΠΙΣΤΡΕΦΟΥΜΕ true γιατί η λέξη ΥΠΗΡΧΕ και τη διαγράψαμε
            // Το καθάρισμα κόμβων θα γίνει πιο πάνω
            return true;
        }

        // Πάμε πιο βαθιά
        char ch = key.charAt(depth);
        if (ch < 'a' || ch > 'z') {
            return false;
        }
        int index = ch - 'a';

        TrieNode child = node.children[index];
        if (child == null) {
            // Δεν υπάρχει τέτοια λέξη
            return false;
        }

        // Προσπαθούμε να διαγράψουμε στο επόμενο επίπεδο
        boolean deleted = deleteHelper(child, key, depth + 1);

        // Αν ΔΕΝ διαγράψαμε λέξη πιο κάτω → τίποτα άλλο να κάνουμε
        if (!deleted) {
            return false;
        }

        // Αν διαγράψαμε τη λέξη, τώρα κάνουμε καθάρισμα:
        // Αν το child ΔΕΝ είναι τέλος άλλης λέξης και δεν έχει παιδιά → μπορούμε να το κόψουμε
        if (!child.isEndOfWord && isNodeEmpty(child)) {
            node.children[index] = null;
        }

        // Η λέξη σβήστηκε (ήδη), άρα συνεχίζουμε να επιστρέφουμε true προς τα πάνω
        return true;
    }


    // Επιστρέφει true αν ο node δεν έχει καθόλου παιδιά
    private static boolean isNodeEmpty(TrieNode node) {
        for (int i = 0; i < TrieNode.ALPHABET_SIZE; i++) {
            if (node.children[i] != null) {
                return false;
            }
        }
        return true;
    }

    // ================== TrieNode ==================
    private static class TrieNode {
        private static final int ALPHABET_SIZE = 26;
        private TrieNode[] children;
        private boolean isEndOfWord;

        public TrieNode() {
            this.isEndOfWord = false;
            this.children = new TrieNode[ALPHABET_SIZE];
            // Η Java βάζει από μόνη της null σε όλα τα στοιχεία
        }
    }


    
}
