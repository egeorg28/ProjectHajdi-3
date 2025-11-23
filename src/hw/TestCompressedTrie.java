package hw;

public class TestCompressedTrie {

    public static void main(String[] args) {

        // 1. Δημιουργούμε ένα καινούργιο CompressedTrie
        CompressedTrie trie = new CompressedTrie();

        // 2. Λέξεις που θα βάλουμε μέσα στο trie
        String[] wordsToInsert = {
            "car",
            "cart",
            "cat",
            "dog",
            "doll",
            "bear",
            "bell",
            "bid",
            "bull",
            "buy",
            "sell",
        
            "stop"
        };

        System.out.println("=== ΕΙΣΑΓΩΓΗ ΛΕΞΕΩΝ ΣΤΟ TRIE ===");
        for (String w : wordsToInsert) {
            System.out.println("Insert: " + w);
            trie.insert(w);
        }

        // 3. Λέξεις που θα ψάξουμε (κάποιες υπάρχουν, κάποιες όχι)
        String[] wordsToSearch = {
            "car",
            "cart",
            "cat",
            "cap",     // δεν υπάρχει
            "dog",
            "doll",
            "do",      // δεν υπάρχει σαν πλήρης λέξη
            "bear",
            "bell",
            "bid",
            "bull",
            "buy",
            "sell",
            "stock",
            "stop",
            "sto",     // δεν υπάρχει
            "hello"    // δεν υπάρχει
        };

        System.out.println("\n=== ΑΠΟΤΕΛΕΣΜΑΤΑ SEARCH ===");
        for (String w : wordsToSearch) {
            boolean found = trie.search(w);
            System.out.println("search(\"" + w + "\") = " + found);
        }
    }
}

