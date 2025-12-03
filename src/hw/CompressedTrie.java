package hw;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * A Compressed Trie that stores words using EDGE LABELS (string chunks) instead
 * of one character per node, and stores an "importance" counter on the node
 * where each word ends.
 */
public class CompressedTrie {

    // Root = start of trie = represents the empty word ""
    private CompressedTrieNode root;

    // ========= CONSTRUCTOR =========
    /**
     * Creates an empty compressed trie (only root node).
     */
    public CompressedTrie() {
        this.root = new CompressedTrieNode();
    }

    // ========= INSERT (public) =========
    /**
     * Inserts a word into the compressed trie, splitting edges if needed so
     * common prefixes are stored only once.
     */
    public void insert(String word) {
        if (word == null || word.isEmpty()) {
            return; // Ignore bad input
        }
        insertRecursive(root, word.toLowerCase());
    }

    // ========= SEARCH (public) =========
    /**
     * Returns true if a word is stored as a complete word in the trie.
     */
    public boolean search(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        return searchRecursive(root, word.toLowerCase());
    }

    // ========= INCREASE IMPORTANCE (simple version) =========
    /**
     * Follows the path of a word and increments importance at the terminal
     * node. If the path does not exist, it does nothing.
     *
     * (Not used in the fast text update; kept as a utility.)
     */
    public void increaseImportance(String word) {
        if (word == null || word.isEmpty()) {
            return;
        }
        increaseImportanceRecursive(root, word.toLowerCase());
    }

    // ========= GET IMPORTANCE =========
    /**
     * Returns the importance of a word if it exists, or 0 otherwise.
     */
    public int getImportance(String word) {
        if (word == null || word.isEmpty()) {
            return 0;
        }
        return getImportanceRecursive(root, word.toLowerCase());
    }

    // =====================================================
    //                    RECURSIVE HELPERS
    // =====================================================
    // ========= INSERT RECURSIVE =========
    private void insertRecursive(CompressedTrieNode currentNode, String word) {
        // No letters left → mark node as end-of-word
        if (word.length() == 0) {
            currentNode.isEndOfWord = true;
            return;
        }

        char firstChar = word.charAt(0);
        Edge edge = currentNode.getEdgeByFirstChar(firstChar);

        // No edge: create new edge with whole remaining word
        if (edge == null) {
            CompressedTrieNode newChild = new CompressedTrieNode();
            newChild.isEndOfWord = true;
            currentNode.insertEdge(new Edge(word, newChild));
            return;
        }

        String label = edge.label;
        String commonPrefix = commonPrefix(label, word);

        // Safety: no common prefix (should not normally happen)
        if (commonPrefix.length() == 0) {
            CompressedTrieNode newChild = new CompressedTrieNode();
            newChild.isEndOfWord = true;
            currentNode.insertEdge(new Edge(word, newChild));
            return;
        }

        // CASE A: label == word
        if (commonPrefix.length() == label.length()
                && commonPrefix.length() == word.length()) {
            edge.child.isEndOfWord = true;
            return;
        }

        // CASE B: label is prefix of word
        // e.g., label = "car", word = "cart"
        if (commonPrefix.length() == label.length()
                && commonPrefix.length() < word.length()) {
            String remaining = word.substring(label.length());
            insertRecursive(edge.child, remaining);
            return;
        }

        // CASE C: word is prefix of label
        // e.g., label = "carton", word = "car"
        if (commonPrefix.length() == word.length()
                && commonPrefix.length() < label.length()) {

            String suffixLabel = label.substring(commonPrefix.length());
            CompressedTrieNode oldChild = edge.child;

            CompressedTrieNode middle = new CompressedTrieNode();
            middle.isEndOfWord = true;   // word ends here
            middle.importance = 0;

            edge.label = commonPrefix;   // "car"
            edge.child = middle;

            middle.insertEdge(new Edge(suffixLabel, oldChild)); // "ton" → oldChild
            return;
        }

        // CASE D: partial overlap
        // e.g., label = "car", word = "cat", common = "ca"
        if (commonPrefix.length() < label.length()
                && commonPrefix.length() < word.length()) {

            String suffixLabel = label.substring(commonPrefix.length()); // "r"
            String suffixWord = word.substring(commonPrefix.length());  // "t"

            CompressedTrieNode oldChild = edge.child;

            CompressedTrieNode middle = new CompressedTrieNode();
            middle.isEndOfWord = false;
            middle.importance = 0;

            edge.label = commonPrefix; // "ca"
            edge.child = middle;

            // old branch: "r"
            middle.insertEdge(new Edge(suffixLabel, oldChild));

            // new branch: "t"
            CompressedTrieNode newChild = new CompressedTrieNode();
            newChild.isEndOfWord = true;
            newChild.importance = 0;
            middle.insertEdge(new Edge(suffixWord, newChild));

            return;
        }

        // else: nothing more to do
    }

    // ========= IMPORTANCE INCREMENT RECURSIVE =========
    private static void increaseImportanceRecursive(CompressedTrieNode currentNode, String word) {
        if (word.length() == 0) {
            currentNode.importance++;
            return;
        }

        char firstChar = word.charAt(0);
        Edge edge = currentNode.getEdgeByFirstChar(firstChar);
        if (edge == null) {
            return;
        }

        String label = edge.label;
        if (word.startsWith(label)) {
            String remaining = word.substring(label.length());
            increaseImportanceRecursive(edge.child, remaining);
        }
    }

    // ========= SEARCH RECURSIVE =========
    private static boolean searchRecursive(CompressedTrieNode currentNode, String word) {
        if (word.length() == 0) {
            return currentNode.isEndOfWord;
        }

        char firstChar = word.charAt(0);
        Edge edge = currentNode.getEdgeByFirstChar(firstChar);
        if (edge == null) {
            return false;
        }

        String label = edge.label;
        if (word.startsWith(label)) {
            String remaining = word.substring(label.length());
            return searchRecursive(edge.child, remaining);
        }
        return false;
    }

    // ========== COMMON PREFIX ==========
    private static String commonPrefix(String a, String b) {
        int max = Math.min(a.length(), b.length());
        int i = 0;
        while (i < max && a.charAt(i) == b.charAt(i)) {
            i++;
        }
        return a.substring(0, i);
    }

    // ========= GET IMPORTANCE RECURSIVE =========
    private int getImportanceRecursive(CompressedTrieNode node, String word) {
        if (word.length() == 0) {
            return node.importance;
        }

        char first = word.charAt(0);
        Edge edge = node.getEdgeByFirstChar(first);
        if (edge == null) {
            return 0;
        }

        String label = edge.label;
        if (word.startsWith(label)) {
            String rest = word.substring(label.length());
            return getImportanceRecursive(edge.child, rest);
        }
        return 0;
    }

    // =====================================================
    //      *** NEW: FASTER IMPORTANCE UPDATE FOR TEXT ***
    // =====================================================
    /**
     * NEW method (added for speed): - Tries to find the given word in the trie.
     * - If the full word exists AND ends at a valid word node, it increments
     * importance. - If the word is not in the dictionary, it does nothing.
     *
     * Used in updateImportanceFromText so we only traverse the trie ONCE per
     * word (instead of search + increase).
     */
    public void increaseImportanceIfExists(String word) {
        if (word == null || word.isEmpty()) {
            return;
        }
        increaseImportanceIfExistsRecursive(root, word.toLowerCase());
    }

    /**
     * Recursive helper for increaseImportanceIfExists. Single traversal: - If
     * we reach the end of the word and node.isEndOfWord == true, then
     * importance++ for that node. - If at any point we cannot follow edges, we
     * stop.
     */
    private static void increaseImportanceIfExistsRecursive(CompressedTrieNode currentNode, String word) {
        if (word.length() == 0) {
            if (currentNode.isEndOfWord) {
                currentNode.importance++;
            }
            return;
        }

        char firstChar = word.charAt(0);
        Edge edge = currentNode.getEdgeByFirstChar(firstChar);
        if (edge == null) {
            return; // word not in dictionary
        }

        String label = edge.label;
        if (word.startsWith(label)) {
            String rest = word.substring(label.length());
            increaseImportanceIfExistsRecursive(edge.child, rest);
        } else {
            // label does not match → word not in dictionary
            return;
        }
    }

    // ========= LOAD DICTIONARY =========
    /**
     * Reads a dictionary file (one word per line) and inserts all words into
     * the compressed trie.
     */
    public boolean loadDictionary(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;

            while ((line = br.readLine()) != null) {
                String w = line.trim();
                if (!w.isEmpty()) {
                    insert(w.toLowerCase());
                }
            }
            return true;
        } catch (IOException e) {
            System.out.println("Dictionary file error!");
            return false;
        }
    }

    // ========= UPDATE IMPORTANCE FROM TEXT =========
    /**
     * *** MODIFIED FOR SPEED ***
     *
     * Reads a text file character-by-character, builds words using only
     * letters, and for each completed word calls
     * increaseImportanceIfExists(word).
     *
     * This avoids slow regex split("\\W+") and avoids search+increaseImportance
     * (two traversals). We now have only ONE traversal per dictionary word.
     */
    public void updateImportanceFromText(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

            StringBuilder tokenBuilder = new StringBuilder();
            int ch;

            // Read character by character
            while ((ch = br.read()) != -1) {
                char c = (char) ch;

                if (Character.isLetter(c)) {
                    tokenBuilder.append(Character.toLowerCase(c));
                } else {
                    if (tokenBuilder.length() > 0) {
                        String word = tokenBuilder.toString();
                        increaseImportanceIfExists(word);  // ONE traversal
                        tokenBuilder.setLength(0);        // reset builder
                    }
                }
            }

            // Last word at EOF (if any)
            if (tokenBuilder.length() > 0) {
                String word = tokenBuilder.toString();
                increaseImportanceIfExists(word);
            }

        } catch (IOException e) {
            System.out.println("Text file importance error!");
        }
    }

    // ==========================================================
    //      INNER CLASSES FOR PREFIX OPERATIONS / DFS HELPERS
    // ==========================================================
    /**
     * Helper when we search for a prefix: - node : trie node where the prefix
     * ends - built : full string from root to that node
     */
    private static class PrefixResult {

        CompressedTrieNode node;
        String built;

        PrefixResult(CompressedTrieNode node, String built) {
            this.node = node;
            this.built = built;
        }
    }

    /**
     * Helper for accumulating sum and count (for average).
     */
    private static class SumCount {

        long sum;
        int count;
    }

    // ========= findNodeForPrefix =========
    private PrefixResult findNodeForPrefix(String prefix) {
        if (prefix == null) {
            return null;
        }
        return findNodeForPrefixRecursive(root, "", prefix.toLowerCase());
    }

    private PrefixResult findNodeForPrefixRecursive(CompressedTrieNode node,
            String built,
            String remaining) {
        if (remaining.length() == 0) {
            return new PrefixResult(node, built);
        }

        char first = remaining.charAt(0);
        Edge edge = node.getEdgeByFirstChar(first);
        if (edge == null) {
            return null;
        }

        String label = edge.label;

        // remaining starts with label
        if (remaining.startsWith(label)) {
            String rest = remaining.substring(label.length());
            return findNodeForPrefixRecursive(edge.child, built + label, rest);
        }

        // label starts with remaining → prefix ends inside label
        if (label.startsWith(remaining)) {
            return new PrefixResult(edge.child, built + label);
        }

        return null;
    }

    // ========= DFS HELPERS =========
    private void dfsCollectTopK(CompressedTrieNode node,
            String built,
            MyMinHeap heap,
            int k) {
        if (node == null) {
            return;
        }

        if (node.isEndOfWord) {
            heap.insertWithCapacity(built, node.importance, k);
        }

        RobinHoodHashing table = node.getEdgeTable();
        Edge[] edges = table.getTable();
        int cap = table.getCapacity();

        for (int i = 0; i < cap; i++) {
            Edge e = edges[i];
            if (e != null && e.occupied) {
                dfsCollectTopK(e.child, built + e.label, heap, k);
            }
        }
    }

    private void dfsSumCount(CompressedTrieNode node, SumCount acc) {
        if (node == null) {
            return;
        }

        if (node.isEndOfWord) {
            acc.sum += node.importance;
            acc.count += 1;
        }

        RobinHoodHashing table = node.getEdgeTable();
        Edge[] edges = table.getTable();
        int cap = table.getCapacity();

        for (int i = 0; i < cap; i++) {
            Edge e = edges[i];
            if (e != null && e.occupied) {
                dfsSumCount(e.child, acc);
            }
        }
    }

    // ==========================================================
    //       1) topKFrequentWordsWithPrefix(prefix, k)
    // ==========================================================
    public void topKFrequentWordsWithPrefix(String prefix, int k) {
        if (prefix == null || prefix.isEmpty() || k <= 0) {
            System.out.println("Invalid prefix or k.");
            return;
        }

        PrefixResult pr = findNodeForPrefix(prefix);
        if (pr == null) {
            System.out.println("No words with prefix \"" + prefix + "\".");
            return;
        }

        MyMinHeap heap = new MyMinHeap(k);
        dfsCollectTopK(pr.node, pr.built, heap, k);

        MyMinHeap.HeapEntry[] arr = heap.toArray();
        int n = arr.length;
        if (n == 0) {
            System.out.println("No words with prefix \"" + prefix + "\".");
            return;
        }

        // Sort by:
        // 1) importance descending
        // 2) word ascending (alphabetically) on ties
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                boolean shouldSwap = false;

                if (arr[j].importance > arr[i].importance) {
                    shouldSwap = true;
                } else if (arr[j].importance == arr[i].importance
                        && arr[j].word.compareTo(arr[i].word) < 0) {
                    shouldSwap = true;
                }

                if (shouldSwap) {
                    MyMinHeap.HeapEntry tmp = arr[i];
                    arr[i] = arr[j];
                    arr[j] = tmp;
                }
            }
        }

        System.out.print("Top " + k + " words for prefix \"" + prefix + "\": ");
        for (int i = 0; i < n; i++) {
            System.out.print(arr[i].word);
            if (i < n - 1) {
                System.out.print(" ");
            }
        }
        System.out.println();
    }

    // ==========================================================
    //       2) getAverageFrequencyOfPrefix(prefix)
    // ==========================================================
    public double getAverageFrequencyOfPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return 0.0;
        }

        PrefixResult pr = findNodeForPrefix(prefix);
        if (pr == null) {
            return 0.0;
        }

        SumCount acc = new SumCount();
        dfsSumCount(pr.node, acc);

        if (acc.count == 0) {
            return 0.0;
        }
        return (double) acc.sum / (double) acc.count;
    }

    // ==========================================================
    //       3) predictNextLetter(prefix)
    // ==========================================================
    /**
     * Predicts the next letter after a given prefix.
     *
     * Handles: 1) prefix ends exactly at a node → look at child edges 2) prefix
     * ends in the middle of an edge label → next char in that label
     *
     * Uses getAverageFrequencyOfPrefix(prefix + c) to pick the child with
     * highest average importance.
     */
    public char predictNextLetter(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return '\0';
        }

        prefix = prefix.toLowerCase();

        CompressedTrieNode current = root;
        String remaining = prefix;

        while (true) {
            // Case 1: prefix ended exactly at this node
            if (remaining.length() == 0) {
                if (!current.hasEdges()) {
                    return '\0';
                }

                double bestAverage = 0.0;
                char bestChar = '\0';

                Edge[] edges = current.getEdgeTable().getTable();
                for (int i = 0; i < edges.length; i++) {
                    Edge e = edges[i];
                    if (e != null && e.occupied && !e.label.isEmpty()) {
                        char c = e.label.charAt(0);
                        double avg = getAverageFrequencyOfPrefix(prefix + c);
                        if (avg > bestAverage) {
                            bestAverage = avg;
                            bestChar = c;
                        }
                    }
                }

                return bestChar;
            }

            // Case 2: still characters to match in remaining
            char first = remaining.charAt(0);
            Edge edge = current.getEdgeByFirstChar(first);
            if (edge == null) {
                return '\0';
            }

            String label = edge.label;

            // remaining is prefix of label
            if (label.startsWith(remaining)) {
                int pos = remaining.length();
                if (pos < label.length()) {
                    return label.charAt(pos);
                } else {
                    // remaining == label → move to child node
                    current = edge.child;
                    remaining = "";
                }
            } // label is prefix of remaining
            else if (remaining.startsWith(label)) {
                remaining = remaining.substring(label.length());
                current = edge.child;
            } else {
                // mismatch
                return '\0';
            }
        }
    }
    // μέσα στην CompressedTrie, έξω από άλλα methods, πριν το τελευταίο }

    CompressedTrieNode getRoot() {
        return root;
    }

}
