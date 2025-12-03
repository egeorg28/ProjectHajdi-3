package hw;

public class MemoryEx {

    // Θεωρητικό μοντέλο κόστους
    private static final int BYTES_PER_REF     = 8;
    private static final int BYTES_PER_INT     = 4;
    private static final int BYTES_PER_BOOLEAN = 1;
    private static final int BYTES_PER_CHAR    = 2;

    public static void main(String[] args) {

        if (args.length < 3) {
            System.out.println("Usage: java -cp src hw.MemoryEx <templateDictFile> <n> <mode> [fixedLen]");
            System.out.println("  mode: fixed | var");
            System.out.println("Examples:");
            System.out.println("  java -cp src hw.MemoryEx /usr/share/dict/american-english 10000 fixed 8");
            System.out.println("  java -cp src hw.MemoryEx /usr/share/dict/american-english 50000 var");
            return;
        }

        String templateFile = args[0];
        int n = Integer.parseInt(args[1]);
        String mode = args[2].toLowerCase();

        int fixedLen = 8;
        if (mode.equals("fixed")) {
            if (args.length < 4) {
                System.out.println("For mode=fixed you must also give fixedLen (e.g. 8)");
                return;
            }
            fixedLen = Integer.parseInt(args[3]);
        }

        System.out.println("Template dictionary: " + templateFile);
        System.out.println("n = " + n + ", mode = " + mode +
                (mode.equals("fixed") ? (", fixedLen = " + fixedLen) : ""));
        System.out.println();

        // 1) Χτίζουμε DictionaryModel από το πραγματικό λεξικό
        DictionaryModel model = LexiconStats.buildModelFromFile(templateFile);
        if (model == null) {
            System.out.println("Could not build DictionaryModel. Exiting.");
            return;
        }

        // 2) Ντετερμινιστικός generator
        DeterministicSyntheticDict gen = new DeterministicSyntheticDict(model);

        // Πόσα δείγματα για τον μέσο όρο
        int samples = 5;

        long sumTrie = 0;
        long sumCTrie = 0;

        for (int s = 0; s < samples; s++) {
            System.out.println("=== SAMPLE " + (s + 1) + " / " + samples + " ===");

            // 3) Συνθετικό λεξικό
            String[] words;
            if (mode.equals("fixed")) {
                words = gen.generateFixedLength(n, fixedLen);
            } else {
                words = gen.generateVariableLength(n);
            }

            System.out.println("Generated " + words.length + " words. Example: " + words[0]);

            // 4) Κλασικό Trie
            Trie t = new Trie();
            for (int i = 0; i < words.length; i++) {
                t.insert(words[i]);
            }
            long memTrie = estimateTrieMemory(t);
            System.out.println("Trie memory (bytes)      : " + memTrie);

            // 5) CompressedTrie
            CompressedTrie cTrie = new CompressedTrie();
            for (int i = 0; i < words.length; i++) {
                cTrie.insert(words[i]);
            }
            long memCTrie = estimateCompressedTrieMemory(cTrie);
            System.out.println("CompressedTrie memory (bytes): " + memCTrie);
            System.out.println();

            sumTrie += memTrie;
            sumCTrie += memCTrie;
        }

        double avgTrie = (double) sumTrie / samples;
        double avgCTrie = (double) sumCTrie / samples;

        System.out.println("=== AVERAGE OVER " + samples + " SAMPLES ===");
        System.out.println("Average Trie memory (bytes)      : " + (long) avgTrie);
        System.out.println("Average CompressedTrie (bytes)   : " + (long) avgCTrie);

        double avgTrieKB = avgTrie / 1024.0;
        double avgCTrieKB = avgCTrie / 1024.0;
        System.out.printf("Trie   : %.2f KB\n", avgTrieKB);
        System.out.printf("CTrie  : %.2f KB\n", avgCTrieKB);
        System.out.println("===========================================");
    }

    // ============== Μνήμη κλασικού Trie ==============

    private static long estimateTrieMemory(Trie t) {
        if (t == null) return 0L;
        Trie.TrieNode root = t.getRoot();
        if (root == null) return 0L;
        return estimateTrieNode(root);
    }

    private static long estimateTrieNode(Trie.TrieNode node) {
        if (node == null) return 0L;

        long bytes = 0L;

        // fields: TrieNode[] children; boolean isEndOfWord; int importance;
        bytes += BYTES_PER_REF;      // children reference
        bytes += BYTES_PER_BOOLEAN;  // isEndOfWord
        bytes += BYTES_PER_INT;      // importance

        // children array: 26 references
        Trie.TrieNode[] children = node.children;
        if (children != null) {
            bytes += children.length * BYTES_PER_REF;
            for (int i = 0; i < children.length; i++) {
                if (children[i] != null) {
                    bytes += estimateTrieNode(children[i]);
                }
            }
        }

        return bytes;
    }

    // ============== Μνήμη CompressedTrie ==============

    private static long estimateCompressedTrieMemory(CompressedTrie trie) {
        if (trie == null || trie.getRoot() == null) return 0L;
        return estimateCNode(trie.getRoot());
    }

    private static long estimateCNode(CompressedTrieNode node) {
        if (node == null) return 0L;

        long bytes = 0L;

        // fields: RobinHoodHashing edgeTable; boolean isEndOfWord; int importance;
        bytes += BYTES_PER_REF;      // edgeTable ref
        bytes += BYTES_PER_BOOLEAN;  // isEndOfWord
        bytes += BYTES_PER_INT;      // importance

        RobinHoodHashing table = node.getEdgeTable();
        if (table == null) {
            return bytes;
        }

        // μνήμη hash table (χωρίς Edge αντικείμενα)
        bytes += estimateHashTableMemory(table);

        // τώρα μετράμε ακμές και παιδιά
        Edge[] arr = table.getTable();
        if (arr != null) {
            for (int i = 0; i < arr.length; i++) {
                Edge e = arr[i];
                if (e != null && e.occupied) {
                    bytes += estimateEdgeMemory(e);
                    bytes += estimateCNode(e.child);
                }
            }
        }

        return bytes;
    }

    private static long estimateHashTableMemory(RobinHoodHashing table) {
        long bytes = 0L;

        // fields: Edge[] table; int capacity, size, maxProbeLength, primeIndex;
        bytes += BYTES_PER_REF;          // Edge[] reference
        bytes += 4L * BYTES_PER_INT;     // 4 int

        int cap = table.getCapacity();
        if (cap > 0) {
            bytes += (long) cap * BYTES_PER_REF;
        }

        return bytes;
    }

    private static long estimateEdgeMemory(Edge e) {
        long bytes = 0L;

        // fields: String label; CompressedTrieNode child; boolean occupied;
        bytes += BYTES_PER_REF;      // label ref
        bytes += BYTES_PER_REF;      // child ref
        bytes += BYTES_PER_BOOLEAN;  // occupied

        if (e.label != null) {
            int len = e.label.length();
            bytes += (long) len * BYTES_PER_CHAR;
        }

        return bytes;
    }
}
