package hw;

public class MemoryEx {

    // Theoretical memory cost model for different primitive types and references.
    // These are NOT exact JVM values, but a simplified model used for comparison.
    private static final int BYTES_PER_REF     = 8;  // reference to an object
    private static final int BYTES_PER_INT     = 4;  // int field
    private static final int BYTES_PER_BOOLEAN = 1;  // boolean field
    private static final int BYTES_PER_CHAR    = 2;  // one UTF-16 char in a String

    public static void main(String[] args) {

        // We need at least 3 arguments:
        //   0: templateDictFile
        //   1: n (number of words)
        //   2: mode ("fixed" or "var")
        //   3: [fixedLen] (only for mode="fixed")
        if (args.length < 3) {
            System.out.println("Usage: java -cp src hw.MemoryEx <templateDictFile> <n> <mode> [fixedLen]");
            System.out.println("  mode: fixed | var");
            System.out.println("Examples:");
            System.out.println("  java -cp src hw.MemoryEx /usr/share/dict/american-english 10000 fixed 8");
            System.out.println("  java -cp src hw.MemoryEx /usr/share/dict/american-english 50000 var");
            return;
        }

        // Path to the real dictionary used to build statistics (DictionaryModel)
        String templateFile = args[0];

        // How many synthetic words we want to generate in each sample
        int n = Integer.parseInt(args[1]);

        // "fixed" = all words have the same length (fixedLen)
        // "var"   = word length is chosen from length distribution in the model
        String mode = args[2].toLowerCase();

        // Default fixed length, in case user does not override it (when mode=fixed)
        int fixedLen = 8;

        if (mode.equals("fixed")) {
            // In fixed mode, user MUST provide the fixed length as 4th argument
            if (args.length < 4) {
                System.out.println("For mode=fixed you must also give fixedLen (e.g. 8)");
                return;
            }
            fixedLen = Integer.parseInt(args[3]);
        }

        // Print configuration summary
        System.out.println("Template dictionary: " + templateFile);
        System.out.println("n = " + n + ", mode = " + mode +
                (mode.equals("fixed") ? (", fixedLen = " + fixedLen) : ""));
        System.out.println();

        // 1) Build a DictionaryModel from the real dictionary file.
        //    This computes letter and length probabilities used by the generator.
        DictionaryModel model = LexiconStats.buildModelFromFile(templateFile);
        if (model == null) {
            System.out.println("Could not build DictionaryModel. Exiting.");
            return;
        }

        // 2) Create a deterministic synthetic dictionary generator (no Random).
        //    It will always generate the same sequence of words for this model.
        DeterministicSyntheticDict gen = new DeterministicSyntheticDict(model);

        // How many independent "samples" we will run.
        // Each sample:
        //   - generates a synthetic word set
        //   - builds a Trie and a CompressedTrie
        //   - estimates memory for both
        int samples = 5;

        long sumTrie = 0;   // sum of Trie memory over all samples
        long sumCTrie = 0;  // sum of CompressedTrie memory over all samples

        for (int s = 0; s < samples; s++) {
            System.out.println("=== SAMPLE " + (s + 1) + " / " + samples + " ===");

            // 3) Generate synthetic lexicon (array of words, not a List).
            String[] words;
            if (mode.equals("fixed")) {
                // All words have the same length fixedLen
                words = gen.generateFixedLength(n, fixedLen);
            } else {
                // Word length chosen from model's length distribution
                words = gen.generateVariableLength(n);
            }

            System.out.println("Generated " + words.length + " words. Example: " + words[0]);

            // 4) Build a classic Trie using the generated words.
            Trie t = new Trie();
            for (int i = 0; i < words.length; i++) {
                t.insert(words[i]);
            }
            // Estimate memory usage of the entire Trie structure.
            long memTrie = estimateTrieMemory(t);
            System.out.println("Trie memory (bytes)      : " + memTrie);

            // 5) Build a CompressedTrie using the same words.
            CompressedTrie cTrie = new CompressedTrie();
            for (int i = 0; i < words.length; i++) {
                cTrie.insert(words[i]);
            }
            // Estimate memory usage of the entire CompressedTrie structure.
            long memCTrie = estimateCompressedTrieMemory(cTrie);
            System.out.println("CompressedTrie memory (bytes): " + memCTrie);
            System.out.println();

            // Accumulate totals for averaging later
            sumTrie += memTrie;
            sumCTrie += memCTrie;
        }

        // Compute averages over the number of samples
        double avgTrie = (double) sumTrie / samples;
        double avgCTrie = (double) sumCTrie / samples;

        System.out.println("=== AVERAGE OVER " + samples + " SAMPLES ===");
        System.out.println("Average Trie memory (bytes)      : " + (long) avgTrie);
        System.out.println("Average CompressedTrie (bytes)   : " + (long) avgCTrie);

        // Also print in KB for convenience
        double avgTrieKB = avgTrie / 1024.0;
        double avgCTrieKB = avgCTrie / 1024.0;
        System.out.printf("Trie   : %.2f KB\n", avgTrieKB);
        System.out.printf("CTrie  : %.2f KB\n", avgCTrieKB);
        System.out.println("===========================================");
    }

    // ============== Memory estimation for classic Trie ==============

    /**
     * Estimates the memory usage of a full Trie structure, starting from its root.
     * Uses a recursive traversal of all nodes.
     */
    private static long estimateTrieMemory(Trie t) {
        if (t == null) return 0L;
        Trie.TrieNode root = t.getRoot();
        if (root == null) return 0L;
        return estimateTrieNode(root);
    }

    /**
     * Recursively estimates memory usage of a single Trie node and all its descendants.
     *
     * Assumptions:
     *  - Each node stores:
     *      TrieNode[] children
     *      boolean isEndOfWord
     *      int importance
     *  - Each child pointer is a reference (BYTES_PER_REF).
     */
    private static long estimateTrieNode(Trie.TrieNode node) {
        if (node == null) return 0L;

        long bytes = 0L;

        // Node fields:
        //   TrieNode[] children;
        //   boolean isEndOfWord;
        //   int importance;
        bytes += BYTES_PER_REF;      // reference to children array
        bytes += BYTES_PER_BOOLEAN;  // isEndOfWord flag
        bytes += BYTES_PER_INT;      // importance value

        // Now account for the children array itself (26 references)
        Trie.TrieNode[] children = node.children;
        if (children != null) {
            // Memory for array elements: each entry is a reference
            bytes += children.length * BYTES_PER_REF;

            // Recursively add memory for all non-null child nodes
            for (int i = 0; i < children.length; i++) {
                if (children[i] != null) {
                    bytes += estimateTrieNode(children[i]);
                }
            }
        }

        return bytes;
    }

    // ============== Memory estimation for CompressedTrie ==============

    /**
     * Estimates memory usage for a CompressedTrie starting from its root.
     */
    private static long estimateCompressedTrieMemory(CompressedTrie trie) {
        if (trie == null || trie.getRoot() == null) return 0L;
        return estimateCNode(trie.getRoot());
    }

    /**
     * Recursively estimates memory usage of a CompressedTrieNode and all its descendants.
     *
     * CompressedTrieNode fields (as assumed here):
     *   RobinHoodHashing edgeTable;
     *   boolean isEndOfWord;
     *   int importance;
     */
    private static long estimateCNode(CompressedTrieNode node) {
        if (node == null) return 0L;

        long bytes = 0L;

        // Node fields:
        //   RobinHoodHashing edgeTable;
        //   boolean isEndOfWord;
        //   int importance;
        bytes += BYTES_PER_REF;      // edgeTable reference
        bytes += BYTES_PER_BOOLEAN;  // isEndOfWord flag
        bytes += BYTES_PER_INT;      // importance value

        RobinHoodHashing table = node.getEdgeTable();
        if (table == null) {
            // No edges, so just return the node overhead
            return bytes;
        }

        // Memory for the hash table object itself (excluding Edge objects)
        bytes += estimateHashTableMemory(table);

        // Now traverse each Edge stored in the hash table
        Edge[] arr = table.getTable();
        if (arr != null) {
            for (int i = 0; i < arr.length; i++) {
                Edge e = arr[i];
                // Only count entries that are actually occupied
                if (e != null && e.occupied) {
                    // Memory for the Edge object
                    bytes += estimateEdgeMemory(e);
                    // Plus memory for the child node recursively
                    bytes += estimateCNode(e.child);
                }
            }
        }

        return bytes;
    }

    /**
     * Estimates memory usage of the RobinHoodHashing structure, not including
     * the Edge objects themselves (those are counted separately).
     *
     * Fields (assumed):
     *   Edge[] table;
     *   int capacity, size, maxProbeLength, primeIndex;
     */
    private static long estimateHashTableMemory(RobinHoodHashing table) {
        long bytes = 0L;

        // Reference to the Edge[] array
        bytes += BYTES_PER_REF;

        // Four int fields
        bytes += 4L * BYTES_PER_INT;

        int cap = table.getCapacity();
        if (cap > 0) {
            // Each slot in the hash table is a reference to an Edge
            bytes += (long) cap * BYTES_PER_REF;
        }

        return bytes;
    }

    /**
     * Estimates memory usage of a single Edge object.
     *
     * Fields (assumed):
     *   String label;
     *   CompressedTrieNode child;
     *   boolean occupied;
     *
     * We also approximate the memory used by the String label:
     *   - we count only the characters (len * BYTES_PER_CHAR)
     *   - we ignore String object header, etc., since this is a rough model.
     */
    private static long estimateEdgeMemory(Edge e) {
        long bytes = 0L;

        // References inside Edge:
        //   label (String)
        //   child (CompressedTrieNode)
        //   occupied (boolean)
        bytes += BYTES_PER_REF;      // label reference
        bytes += BYTES_PER_REF;      // child reference
        bytes += BYTES_PER_BOOLEAN;  // occupied flag

        // Approximate label character data size
        if (e.label != null) {
            int len = e.label.length();
            bytes += (long) len * BYTES_PER_CHAR;
        }

        return bytes;
    }
}
