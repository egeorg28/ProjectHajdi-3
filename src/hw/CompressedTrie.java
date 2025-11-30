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
     CompressedTrieNode root;

    // ========= CONSTRUCTOR =========
    /**
     * What it does: Creates an empty compressed trie.
     *
     * Example: CompressedTrie t = new CompressedTrie(); → t has only an empty
     * root node.
     */
    public CompressedTrie() {
        this.root = new CompressedTrieNode(); // Make the empty root
    }

    // ========= INSERT (public) =========
    /**
     * What it does: Inserts a word into the compressed trie, splitting edges if
     * needed so common prefixes are stored only once.
     *
     * Example: t.insert("bear"); → creates path: root --"bear"-->
     * newNode(isEnd=true)
     */
    public void insert(String word) {
        if (word == null || word.isEmpty()) {
            return; // Ignore bad input
        }
        // Lowercase for uniformity and start recursion from root
        insertRecursive(root, word.toLowerCase());
    }

    // ========= SEARCH (public) =========
    /**
     * What it does: Returns true if a word is stored as a complete word in the
     * trie.
     *
     * Example: t.search("be"); → true if "be" was inserted and marked end=true
     * t.search("bell"); → true if path matches labels and end node is end=true
     */
    public boolean search(String word) {
        if (word == null || word.isEmpty()) {
            return false; // Bad input → false
        }
        // Start search from root
        return searchRecursive(root, word.toLowerCase());
    }

    // ========= INCREASE IMPORTANCE =========
    /**
     * What it does: Follows the stored path of a word and adds +1 to
     * `importance` on the node where the word ends (does nothing if path does
     * not exist).
     *
     * Example: t.insert("stop"); t.increaseImportance("stop");
     * t.increaseImportance("stop"); t.getImportance("stop") → 2
     */
    public void increaseImportance(String word) {
        if (word == null || word.isEmpty()) {
            return; // Ignore bad input
        }
        // Start from root
        increaseImportanceRecursive(root, word.toLowerCase());
    }

    // ========= GET IMPORTANCE =========
    /**
     * What it does: Returns the importance counter of a complete word stored in
     * the trie. If the word is not stored, returns 0.
     *
     * Example: t.getImportance("stop") → 2 (if importance was increased twice)
     * t.getImportance("hello") → 0 (not inserted)
     */
    public int getImportance(String word) {
        if (word == null || word.isEmpty()) {
            return 0; // Bad input → 0
        }
        // Start importance fetch from root
        return getImportanceRecursive(root, word.toLowerCase());
    }

    // =====================================================
    //                    RECURSIVE HELPERS
    // =====================================================
    // ========= INSERT RECURSIVE =========
    /**
     * What it does: Core recursive insert logic for compressed trie. It finds
     * the matching edge, computes common prefix, and splits the edge if
     * necessary.
     *
     * Example call (internally called by public insert): insertRecursive(node,
     * "cart")
     */
    private void insertRecursive(CompressedTrieNode currentNode, String word) {
        // If no letters left to insert → mark this node as the end of a word
        if (word.length() == 0) {
            currentNode.isEndOfWord = true;
            return;
        }

        // Take next letter to find correct edge
        char firstChar = word.charAt(0);
        // Find edge whose label starts with firstChar
        Edge edge = currentNode.getEdgeByFirstChar(firstChar);

        // If no such edge exists → create a new edge with the whole remaining word
        if (edge == null) {
            CompressedTrieNode newChild = new CompressedTrieNode();
            newChild.isEndOfWord = true; // word ends here
            currentNode.insertEdge(new Edge(word, newChild)); // edge label = full word
            return;
        }

        // The string chunk stored on that edge
        String label = edge.label;
        // Compute longest shared start between edge.label and the word
        String commonPrefix = commonPrefix(label, word);

        // Safety: if for some reason no common prefix (should not happen normally)
        if (commonPrefix.length() == 0) {
            CompressedTrieNode newChild = new CompressedTrieNode();
            newChild.isEndOfWord = true;
            currentNode.insertEdge(new Edge(word, newChild)); // standalone edge
            return;
        }

        // ----- CASE A: EXACT MATCH ----- (label == word)
        if (commonPrefix.length() == label.length() && commonPrefix.length() == word.length()) {
            // Path already exists, just mark child as end of word
            edge.child.isEndOfWord = true;
            return;
        }

        // ----- CASE B: label is prefix of word ----- (go deeper)
        // Example: label="car", word="cart"
        if (commonPrefix.length() == label.length() && commonPrefix.length() < word.length()) {
            // Remove matched label from word (remaining suffix)
            String remaining = word.substring(label.length());
            // Continue insertion from the child node
            insertRecursive(edge.child, remaining);
            return;
        }

        // ----- CASE C: word is prefix of label ----- (split edge)
        // Example: label="carton", word="car"
        if (commonPrefix.length() == word.length() && commonPrefix.length() < label.length()) {
            // leftover part of old label, e.g. "ton"
            String suffixLabel = label.substring(commonPrefix.length());
            // store old child (was pointed by original edge)
            CompressedTrieNode oldChild = edge.child;

            // new middle node where the new word ends
            CompressedTrieNode middle = new CompressedTrieNode();
            middle.isEndOfWord = true;
            middle.importance = 0;

            // shorten edge label to the common part ("car")
            edge.label = commonPrefix;
            // edge now points to middle
            edge.child = middle;

            // middle points to oldChild via the leftover label ("ton")
            middle.insertEdge(new Edge(suffixLabel, oldChild));
            return;
        }

        // ----- CASE D: partial overlap ----- (common shorter than both)
        // Example: label="car", word="cat", common="ca"
        if (commonPrefix.length() < label.length() && commonPrefix.length() < word.length()) {
            // leftover old path "r"
            String suffixLabel = label.substring(commonPrefix.length());
            // leftover new path "t"
            String suffixWord = word.substring(commonPrefix.length());

            // keep old child
            CompressedTrieNode oldChild = edge.child;
            // new split node
            CompressedTrieNode middle = new CompressedTrieNode();
            middle.isEndOfWord = false;
            middle.importance = 0;

            // edge from currentNode now has label = common prefix ("ca")
            edge.label = commonPrefix;
            edge.child = middle;

            // old word branch: middle --"r"--> oldChild
            middle.insertEdge(new Edge(suffixLabel, oldChild));

            // new word branch: middle --"t"--> newChild
            CompressedTrieNode newChild = new CompressedTrieNode();
            newChild.isEndOfWord = true;
            newChild.importance = 0;
            middle.insertEdge(new Edge(suffixWord, newChild));

            return;
        }

        // else → nothing more to do
    }

    // ========= IMPORTANCE INCREMENT RECURSIVE =========
    /**
     * What it does: Same path logic as search but instead of returning boolean,
     * increments importance at the end node of the word.
     *
     * Example: increaseImportanceRecursive(root, "stop")
     */
    private static void increaseImportanceRecursive(CompressedTrieNode currentNode, String word) {
        // If full word has been matched
        if (word.length() == 0) {
            // Add +1 to importance of the word ending here
            currentNode.importance++;
            return;
        }

        // Next character to guide traversal
        char firstChar = word.charAt(0);
        // Find edge starting with this character
        Edge edge = currentNode.getEdgeByFirstChar(firstChar);
        if (edge == null) {
            // No such path → nothing to increment
            return;
        }

        // chunk string
        String label = edge.label;
        // If leftover word starts with this label chunk
        if (word.startsWith(label)) {
            // consume matched chunk
            String remaining = word.substring(label.length());
            // go deeper
            increaseImportanceRecursive(edge.child, remaining);
        }
        // else: mismatch → do nothing
    }

    // ========= SEARCH RECURSIVE =========
    /**
     * What it does: Follows edge labels to check if the word exists as a full
     * word.
     *
     * Example: t.search("stop") → true only if path matches labels and final
     * node is end=true
     */
    private static boolean searchRecursive(CompressedTrieNode currentNode, String word) {
        // If we've fully matched all characters of the word
        if (word.length() == 0) {
            // Word exists only if this node is an end of a word
            return currentNode.isEndOfWord;
        }

        // Next char to find edge
        char firstChar = word.charAt(0);
        // Find correct edge from current node
        Edge edge = currentNode.getEdgeByFirstChar(firstChar);
        if (edge == null) {
            // No path → word not stored
            return false;
        }

        // Edge chunk
        String label = edge.label;
        // If the remaining word starts with the full label chunk
        if (word.startsWith(label)) {
            // Remove matched part
            String remaining = word.substring(label.length());
            // Continue on child
            return searchRecursive(edge.child, remaining);
        }

        // Label mismatch → word not stored
        return false;
    }

    // ========= LOAD DICTIONARY =========
    /**
     * What it does: Reads a dictionary file and inserts each non-empty line as
     * a word.
     *
     * Example: dict.txt contains: be, bear, bell → all inserted in compressed
     * form.
     */
    public boolean loadDictionary(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;

            // Read each line of the dictionary file
            while ((line = br.readLine()) != null) {
                String w = line.trim(); // Remove spaces at start/end
                if (!w.isEmpty()) {     // Skip empty lines
                    insert(w.toLowerCase()); // Insert lowercase word into trie
                }
            }
            return true; // Completed successfully

        } catch (IOException e) {
            System.out.println("Dictionary file error!");
            return false;
        }
    }

    // ========= UPDATE IMPORTANCE FROM TEXT =========
    /**
     * What it does: Reads a text file, splits lines into tokens (words), and
     * for each token that exists in the trie, increases its importance.
     *
     * Example: text.txt contains: "stop stop bull" → after this:
     * importance(stop) = 2 importance(bull) = 1
     */
    public void updateImportanceFromText(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

            String line;
            // Read each line from the text file
            while ((line = br.readLine()) != null) {
                // Split into tokens using non-word characters as separators
                String[] tokens = line.split("\\W+");

                // Classic index-based for loop over tokens
                for (int i = 0; i < tokens.length; i++) {
                    String token = tokens[i]; // one piece from the split

                    if (token != null && !token.isEmpty()) { // skip null or ""
                        String w = token.toLowerCase(); // normalize to lowercase

                        // If this word exists in the dictionary trie
                        if (search(w)) {
                            // then increase its importance by 1
                            increaseImportance(w);
                        }
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Text file importance error!");
        }
    }

    // ========== COMMON PREFIX ==========
    /**
     * What it does: Finds the longest starting sequence of identical characters
     * between 2 strings.
     *
     * Example: commonPrefix("carton", "cart") → "cart" commonPrefix("car",
     * "cat") → "ca"
     */
    private static String commonPrefix(String a, String b) {
        int max = Math.min(a.length(), b.length());
        int i = 0;
        // Increase i while both strings match at position i
        while (i < max && a.charAt(i) == b.charAt(i)) {
            i++;
        }
        // Return substring from 0 to i (not including i)
        return a.substring(0, i);
    }

    /**
     * Recursive helper for getImportance(String word).
     *
     * What it does: - It follows the compressed trie path for the given word,
     * using edge labels (string chunks). - When the whole word has been
     * consumed (word.length() == 0), it returns the importance value stored at
     * that node. - If at some point the path does not exist or the labels don't
     * match, it returns 0.
     *
     * Example: Suppose the trie contains "stop" and its end node has importance
     * = 3.
     *
     * getImportanceRecursive(root, "stop") - matches an edge "st", calls child
     * with "op" - matches an edge "op", calls child with "" - word.length() ==
     * 0 → returns importance (3)
     */
    private int getImportanceRecursive(CompressedTrieNode node, String word) {

        // If we have consumed all characters of the word,
        // this means we are standing at the node where this word should end.
        if (word.length() == 0) {
            // Return the importance value stored at this node.
            return node.importance;
        }

        // Get the first character of the remaining word.
        char first = word.charAt(0);

        // Try to find an outgoing edge from this node
        // whose label starts with that first character.
        Edge edge = node.getEdgeByFirstChar(first);

        // If no such edge exists, then there is no path for this word in the trie,
        // so the word is not stored → importance is 0.
        if (edge == null) {
            return 0;
        }

        // The label (string chunk) stored on this edge.
        String label = edge.label;

        // Check if the remaining word actually starts with this label.
        // If not, the path doesn't match, so the word doesn't exist.
        if (word.startsWith(label)) {
            // Remove the label from the beginning of the word
            // (we have "consumed" that part along the edge).
            String rest = word.substring(label.length());

            // Continue recursively on the child node with the remaining substring.
            return getImportanceRecursive(edge.child, rest);
        }

        // If the word does not start with the label,
        // then the path breaks here and the word does not exist in the trie.
        // Return 0 as importance.
        return 0;
    }
}
