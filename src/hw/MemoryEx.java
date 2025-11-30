package hw;

import java.util.List;

/**
 * Πείραμα μνήμης για CompressedTrie.
 *
 * Χρήση:
 *   java -cp src hw.MemoryEx <templateDictFile> <n> <mode> [fixedLen]
 *
 *   templateDictFile : π.χ. /usr/share/dict/american-english
 *   n                : πόσες λέξεις να παράγουμε (π.χ. 10000)
 *   mode             : "fixed" ή "var"
 *   fixedLen         : αν mode = fixed, τότε π.χ. 8
 *
 * Παραδείγματα:
 *   java -cp src hw.MemoryEx /usr/share/dict/american-english 10000 fixed 8
 *   java -cp src hw.MemoryEx /usr/share/dict/american-english 50000 var
 */
public class MemoryEx {

    // ================================
    // ΜΟΝΤΕΛΟ ΚΟΣΤΟΥΣ (θεωρητική μνήμη)
    // ================================
    private static final int BYTES_PER_REF     = 8; // reference / pointer
    private static final int BYTES_PER_INT     = 4;
    private static final int BYTES_PER_BOOLEAN = 1;
    private static final int BYTES_PER_CHAR    = 2;

    public static void main(String[] args) {

        if (args.length < 3) {
            System.out.println("Usage: java -cp src hw.MemoryEx <templateDictFile> <n> <mode> [fixedLen]");
            System.out.println("  mode: fixed | var");
            return;
        }

        String templateDict = args[0];
        int n = Integer.parseInt(args[1]);
        String mode = args[2].toLowerCase();

        int fixedLen = 8; // default
        if (mode.equals("fixed")) {
            if (args.length < 4) {
                System.out.println("For mode=fixed you must also give fixedLen (e.g. 8)");
                return;
            }
            fixedLen = Integer.parseInt(args[3]);
        }

        System.out.println("Template dictionary: " + templateDict);
        System.out.println("n = " + n + ", mode = " + mode +
                (mode.equals("fixed") ? (", fixedLen = " + fixedLen) : ""));
        System.out.println();

        // 1) Χτίζουμε μοντέλο λεξικού από το πραγματικό wordlist
        DictionaryModel model = LexiconStats.buildModelFromFile(templateDict);
        if (model == null) {
            System.out.println("Could not build model from file: " + templateDict);
            return;
        }

        // 2) Φτιάχνουμε synthetic generator με βάση το μοντέλο
        SyntheticDict gen = new SyntheticDict(model, 12345L);

        // 3) Παράγουμε συνθετικό λεξικό
        List<String> words;
        if (mode.equals("fixed")) {
            words = gen.generateFixedLength(n, fixedLen);
        } else {
            words = gen.generateVariableLength(n);
        }

        System.out.println("Generated " + words.size() + " synthetic words.");
        if (!words.isEmpty()) {
            System.out.println("Example word: " + words.get(0));
        }
        System.out.println();

        // 4) Χτίζουμε CompressedTrie
        CompressedTrie cTrie = new CompressedTrie();
        for (String w : words) {
            cTrie.insert(w);
        }
        System.out.println("Inserted all words into CompressedTrie.");

        // 5) Υπολογισμός θεωρητικής μνήμης
        long memBytes = estimateCompressedTrieMemory(cTrie);
        double memKB = memBytes / 1024.0;
        double memMB = memKB / 1024.0;

        System.out.println();
        System.out.println("Estimated CompressedTrie memory: " + memBytes + " bytes (" +
                String.format("%.2f", memKB) + " KB, " +
                String.format("%.2f", memMB) + " MB)");
    }

    // ================================
    // ΕΚΤΙΜΗΣΗ ΜΝΗΜΗΣ CompressedTrie
    // ================================

    private static long estimateCompressedTrieMemory(CompressedTrie trie) {
        if (trie == null || trie.root == null) {
            return 0L;
        }
        return estimateNode(trie.root);
    }

    private static long estimateNode(CompressedTrieNode node) {
        if (node == null) return 0L;

        long bytes = 0L;

        // === CompressedTrieNode fields ===
        // private RobinHoodHashing edgeTable;
        // public boolean isEndOfWord;
        // public int importance;
        bytes += BYTES_PER_REF;      // edgeTable reference
        bytes += BYTES_PER_BOOLEAN;  // isEndOfWord
        bytes += BYTES_PER_INT;      // importance

        RobinHoodHashing table = node.getEdgeTable();
        if (table == null) {
            return bytes;
        }

        // μνήμη για hash table (χωρίς τα Edge objects εδώ)
        bytes += estimateHashTableMemory(table);

        // DFS στα παιδιά μέσω του πίνακα
        Edge[] arr = table.getTable();   // ΠΡΕΠΕΙ να έχεις υλοποιήσει getTable() στο RobinHoodHashing
        if (arr != null) {
            for (Edge e : arr) {
                if (e != null && e.occupied) {
                    // μνήμη ακμής
                    bytes += estimateEdgeMemory(e);
                    // αναδρομή στο παιδί
                    bytes += estimateNode(e.child);
                }
            }
        }

        return bytes;
    }

    private static long estimateHashTableMemory(RobinHoodHashing table) {
        long bytes = 0L;

        // Fields στον RobinHoodHashing:
        //  private Edge[] table;
        //  private int capacity;
        //  private int size;
        //  private int maxProbeLength;
        //  private int primeIndex;

        bytes += BYTES_PER_REF;          // Edge[] reference
        bytes += 4L * BYTES_PER_INT;     // 4 int πεδία

        Edge[] arr = table.getTable();
        if (arr != null) {
            // μόνο οι θέσεις του array (references)
            bytes += (long) arr.length * BYTES_PER_REF;
        }

        return bytes;
    }

    private static long estimateEdgeMemory(Edge e) {
        long bytes = 0L;

        // Fields στην Edge:
        //  String label;
        //  CompressedTrieNode child;
        //  boolean occupied;

        bytes += BYTES_PER_REF;      // label reference
        bytes += BYTES_PER_REF;      // child reference
        bytes += BYTES_PER_BOOLEAN;  // occupied

        // Μνήμη για το String label (πολύ απλοποιημένα)
        if (e.label != null) {
            int len = e.label.length();
            bytes += (long) len * BYTES_PER_CHAR;
        }

        return bytes;
    }
}
