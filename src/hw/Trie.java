package hw;

/**
 * Classic Trie (prefix tree) implementation for lowercase English letters
 * 'a'–'z'.
 *
 * Supports: - insert(word) - search(word) - delete(word) - display(): print all
 * stored words in alphabetical order
 */
public class Trie {

    // Root node of the Trie. It represents the empty prefix "".
    private TrieNode root;

    /**
     * Constructor: Creates an empty Trie with just the root node.
     *
     * Example: Trie t = new Trie();
     */
    public Trie() {
        root = new TrieNode();
    }

    // ================== SEARCH ==================
    /**
     * What it does: Returns true if the given word exists in the trie as a
     * complete word. Returns false if: - the word is null/empty - it contains
     * characters outside 'a'..'z' - or the path does not end in an end-of-word
     * node.
     *
     * Example: after insert("cat"), search("cat") → true search("ca") → false
     * (prefix but not full word)
     */
    public boolean search(String word) {
        // Reject invalid input
        if (word == null || word.isEmpty()) {
            return false;
        }

        TrieNode node = root; // Start from root

        // Traverse character-by-character
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);

            // We accept only lowercase a–z
            if (ch < 'a' || ch > 'z') {
                return false; // invalid character → word not in trie
            }

            int index = ch - 'a'; // map 'a'→0, 'b'→1, ..., 'z'→25

            // If there is no node for this character, the word does not exist
            if (node.children[index] == null) {
                return false;
            }

            // Move down to the child node
            node = node.children[index];
        }

        // At the end of the loop, we are at the node for the last character.
        // The word exists ONLY if isEndOfWord is true.
        return node.isEndOfWord;
    }

    // ================== INSERT ==================
    /**
     * What it does: Inserts a new word into the trie. Creates new nodes as
     * needed along the path of characters.
     *
     * Example: insert("cat"): root -'c'-> node -'a'-> node -'t'->
     * node(isEndOfWord=true)
     */
    public void insert(String word) {
        // Ignore invalid input
        if (word == null || word.isEmpty()) {
            return;
        }

        TrieNode node = root; // Start from root

        // For each character in the word...
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);

            // Accept only lowercase a–z
            if (ch < 'a' || ch > 'z') {
                System.out.println("Invalid character: " + ch);
                return; // Stop inserting this word
            }

            int index = ch - 'a';

            // If the child for this character does not exist, create it
            if (node.children[index] == null) {
                node.children[index] = new TrieNode();
            }

            // Move to the child node
            node = node.children[index];
        }

        // After processing all characters, mark this node as end of a valid word
        node.isEndOfWord = true;
    }

    // ================== DISPLAY (all words) ==================
    /**
     * What it does: Prints all words stored in the trie in alphabetical order.
     *
     * Internally calls the recursive helper starting from the root with empty
     * prefix.
     */
    public void display() {
        displayHelper(root, "");
    }

    /**
     * Recursive helper: - node: current trie node - prefix: the string built so
     * far along the path from the root to this node
     *
     * If node.isEndOfWord == true → print the prefix as a complete word. Then,
     * recursively visit children in order 'a'..'z', adding each character to
     * the prefix.
     */
    private static void displayHelper(TrieNode node, String prefix) {
        if (node == null) {
            return; // safety check
        }
        // If this node marks the end of a word, print the word (prefix)
        if (node.isEndOfWord) {
            System.out.println(prefix);
        }

        // Loop through all possible children from 'a' to 'z'
        for (int i = 0; i < TrieNode.ALPHABET_SIZE; i++) {
            if (node.children[i] != null) {
                // Convert index back to character: 0→'a', 1→'b', ...
                char ch = (char) ('a' + i);
                // Go deeper adding this character to the prefix
                displayHelper(node.children[i], prefix + ch);
            }
        }
    }
// ================== DELETE ==================

    /**
     * What it does: Deletes a given word from the trie, if it exists. Returns
     * true if the word existed and was removed (logically). Returns false if
     * the word did not exist.
     *
     * Filtering of unused nodes is done in the recursive helper.
     *
     * Example: insert("the"), insert("their"), insert("there") delete("the"):
     * "the" is removed, but path still used by "their" and "there".
     */
    public boolean delete(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        return deleteHelper(root, key, 0);
    }

    /**
     * Recursive delete helper.
     *
     * node = current node key = word we want to delete depth = current index of
     * character we are processing in key
     */
    private static boolean deleteHelper(TrieNode node, String key, int depth) {
        if (node == null) {
            return false;
        }

        // Base case: we reached the end of the key (all characters processed)
        if (depth == key.length()) {
            // If this node was NOT marking the end of a word,
            // then the word doesn't actually exist in the trie.
            if (!node.isEndOfWord) {
                return false;
            }
            // Unmark this node as end of a word (logical deletion)
            node.isEndOfWord = false;

            // Return true because the word existed and we deleted it logically.
            // Actual node cleanup is handled on the way back up.
            return true;
        }

        // Otherwise, go deeper for the next character
        char ch = key.charAt(depth);
        // Only lowercase a–z are allowed
        if (ch < 'a' || ch > 'z') {
            return false;
        }
        int index = ch - 'a';

        TrieNode child = node.children[index];
        if (child == null) {
            // There is no child for this character → the word does not exist
            return false;
        }

        // Try to delete the word in the subtree
        boolean deleted = deleteHelper(child, key, depth + 1);

        // If no deletion happened in the deeper level, nothing to clean up
        if (!deleted) {
            return false;
        }

        // If deletion happened logically, we now check if we can remove the child node:
        // If the child is not end of another word AND has no children → we can remove it.
        if (!child.isEndOfWord && isNodeEmpty(child)) {
            node.children[index] = null; // Remove reference to child (clean up)
        }

        // The word has been deleted; propagate true upward.
        return true;
    }

    /**
     * Utility function that checks if a node has no children at all.
     *
     * Returns: true → node has 0 children false → node has at least 1 child
     */
    private static boolean isNodeEmpty(TrieNode node) {
        for (int i = 0; i < TrieNode.ALPHABET_SIZE; i++) {
            if (node.children[i] != null) {
                return false;
            }
        }
        return true;
    }

// 👉 ΝΕΟ: δίνουμε πρόσβαση στο root για τα πειράματα μνήμης
    TrieNode getRoot() {
        return root;
    }

    // ================== TrieNode (inner class) ==================
    /**
     * Represents a node in the Trie. Each node has: - an array of 26 children
     * pointers (for 'a'..'z') - a boolean flag isEndOfWord to mark if a word
     * ends here.
     */
     static class TrieNode {

        static final int ALPHABET_SIZE = 26; // Number of letters in English lowercase alphabet
        TrieNode[] children; // Array of child nodes (one per letter)
         boolean isEndOfWord; // True if a word ends at this node

        /**
         * Constructor: Initializes: - isEndOfWord = false (no word ends here
         * yet) - children array of size 26, all elements = null
         */
        public TrieNode() {
            this.isEndOfWord = false;
            this.children = new TrieNode[ALPHABET_SIZE];
            // Java automatically initializes array elements to null.
        }
    }

}
