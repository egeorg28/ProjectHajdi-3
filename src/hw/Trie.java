package hw;

public class Trie {

    // Ρίζα του δέντρου
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

    // ================== main: δοκιμές όλων των μεθόδων ==================
    public static void main(String[] args) {
        Trie trie = new Trie();

        // ---------- INSERT ----------
        System.out.println("=== INSERTING WORDS ===");
        String[] words = { "answer", "any", "by", "bye", "the", "their", "there" };
        for (String w : words) {
            trie.insert(w);
            System.out.println("Inserted: " + w);
        }

        // ---------- DISPLAY ----------
        System.out.println("\n=== DISPLAY ALL WORDS ===");
        trie.display();

        // ---------- SEARCH (υπάρχουν) ----------
        System.out.println("\n=== SEARCH EXISTING WORDS ===");
        String[] exist = { "answer", "any", "bye", "their", "there" };
        for (String w : exist) {
            System.out.println("search(\"" + w + "\") = " + trie.search(w));
        }

        // ---------- SEARCH (δεν υπάρχουν) ----------
        System.out.println("\n=== SEARCH NON-EXISTING WORDS ===");
        String[] notExist = { "ans", "an", "byy", "thee", "hello" };
        for (String w : notExist) {
            System.out.println("search(\"" + w + "\") = " + trie.search(w));
        }

        // ---------- DELETE TESTS ----------
        System.out.println("\n=== DELETE TESTS ===");

        // 1. Διαγραφή λέξης που υπάρχει & δεν είναι πρόθεμα
        System.out.println("delete(\"bye\"): " + trie.delete("bye"));
        System.out.println("search(\"bye\") = " + trie.search("bye"));

        // 2. Διαγραφή λέξης που είναι πρόθεμα άλλων ('the' -> 'their', 'there')
        System.out.println("\ndelete(\"the\"): " + trie.delete("the"));
        System.out.println("search(\"the\")   = " + trie.search("the"));
        System.out.println("search(\"their\") = " + trie.search("their"));
        System.out.println("search(\"there\") = " + trie.search("there"));

        // 3. Διαγραφή λέξης που μοιράζεται πρόθεμα με άλλη ('answer' / 'any')
        System.out.println("\ndelete(\"answer\"): " + trie.delete("answer"));
        System.out.println("search(\"answer\") = " + trie.search("answer"));
        System.out.println("search(\"any\")    = " + trie.search("any"));

        // 4. Διαγραφή λέξης που δεν υπάρχει
        System.out.println("\ndelete(\"banana\"): " + trie.delete("banana"));

        // ---------- DISPLAY μετά από διαγραφές ----------
        System.out.println("\n=== DISPLAY AFTER DELETIONS ===");
        trie.display();

        // ---------- EDGE CASES ----------
        System.out.println("\n=== EDGE CASE TESTS ===");
        System.out.println("search(null)  = " + trie.search(null));
        System.out.println("search(\"\")   = " + trie.search(""));
        System.out.println("delete(null)  = " + trie.delete(null));
        System.out.println("delete(\"\")   = " + trie.delete(""));
        System.out.println("insert(\"Hello\") (invalid chars):");
        trie.insert("Hello");  // Θα τυπώσει "Invalid character: H"
    }
}
