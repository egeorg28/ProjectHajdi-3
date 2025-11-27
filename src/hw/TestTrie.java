package hw;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Διαδραστική εφαρμογή:
 *  - Φορτώνει λεξικό σε CompressedTrie
 *  - Ενημερώνει importance από αρχείο κειμένου
 *  - Προσφέρει μενού:
 *      1. topKFrequentWordsWithPrefix
 *      2. average frequency για prefix
 *      3. predict next letter
 *      4. exit
 */
public class TestTrie {

    public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println("Usage: java -cp . hw.TestTrie <dictionaryFile> <textFile>");
            return;
        }

        String dictionaryFile = args[0];
        String textFile       = args[1];

        CompressedTrie trie = new CompressedTrie();

        // ===== ΜΕΡΟΣ 1: ΦΟΡΤΩΣΗ ΛΕΞΙΚΟΥ =====
        System.out.println("Loading dictionary from: " + dictionaryFile);
        boolean ok = trie.loadDictionary(dictionaryFile);

        if (!ok) {
            System.out.println("Dictionary NOT loaded. Exiting...");
            return;
        }
        System.out.println("Dictionary loaded successfully!");

        // Φορτώνουμε τις λέξεις του λεξικού σε λίστα (για να κάνουμε prefix queries)
        List<String> dictionaryWords = loadDictionaryWords(dictionaryFile);
        if (dictionaryWords.isEmpty()) {
            System.out.println("Dictionary file is empty. Exiting...");
            return;
        }

        // ===== ΜΕΡΟΣ 2: ΕΝΗΜΕΡΩΣΗ importance ΑΠΟ ΚΕΙΜΕΝΟ =====
        System.out.println("Updating importance from: " + textFile);
        trie.updateImportanceFromText(textFile);
        System.out.println("Finished processing text.");

        // ===== ΔΙΑΔΡΑΣΤΙΚΟ ΜΕΝΟΥ =====
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println();
            System.out.println("===== MENU =====");
            System.out.println("1. Εύρεση των k πιο σημαντικών λέξεων με πρόθεμα prefix");
            System.out.println("2. Υπολογισμός μέσης συχνότητας λέξεων με πρόθεμα prefix");
            System.out.println("3. Πρόβλεψη επόμενου γράμματος για πρόθεμα prefix");
            System.out.println("4. Exit");
            System.out.print("Choice: ");

            String choice = sc.nextLine().trim();

            if (choice.equals("1")) {
                // topKFrequentWordsWithPrefix
                System.out.print("Δώσε prefix: ");
                String prefix = sc.nextLine().trim().toLowerCase();

                System.out.print("Δώσε k: ");
                String kStr = sc.nextLine().trim();
                int k;
                try {
                    k = Integer.parseInt(kStr);
                } catch (NumberFormatException e) {
                    System.out.println("Μη έγκυρος αριθμός k.");
                    continue;
                }

                List<String> result = topKFrequentWordsWithPrefix(trie, dictionaryWords, prefix, k);
                if (result.isEmpty()) {
                    System.out.println("Δεν βρέθηκαν λέξεις με το δοθέν prefix.");
                } else {
                    System.out.println("topKFrequentWordsWithPrefix(\"" + prefix + "\", " + k + ") →");
                    // εκτύπωση όπως στο παράδειγμα: apple application ape
                    for (int i = 0; i < result.size(); i++) {
                        System.out.print(result.get(i));
                        if (i < result.size() - 1) {
                            System.out.print(" ");
                        }
                    }
                    System.out.println();
                }

            } else if (choice.equals("2")) {
                // average frequency για prefix
                System.out.print("Δώσε prefix: ");
                String prefix = sc.nextLine().trim().toLowerCase();

                double avg = getAverageFrequencyOfPrefix(trie, dictionaryWords, prefix);
                System.out.println("getAverageFrequencyOfPrefix(\"" + prefix + "\") → " + avg);

            } else if (choice.equals("3")) {
                // predictNextLetter
                System.out.print("Δώσε prefix: ");
                String prefix = sc.nextLine().trim().toLowerCase();

                Character predicted = predictNextLetter(trie, dictionaryWords, prefix);
                if (predicted == null) {
                    System.out.println("Δεν υπάρχει επόμενο γράμμα για πρόθεμα \"" + prefix + "\".");
                } else {
                    System.out.println("predictNextLetter(\"" + prefix + "\") → '" + predicted + "'");
                }

            } else if (choice.equals("4")) {
                System.out.println("Exiting...");
                break;
            } else {
                System.out.println("Μη έγκυρη επιλογή.");
            }
        }

        sc.close();
    }

    // =====================================================
    // ΒΟΗΘΗΤΙΚΕΣ ΣΥΝΑΡΤΗΣΕΙΣ
    // =====================================================

    /**
     * Διαβάζει ΟΛΕΣ τις λέξεις του λεξικού από αρχείο σε μία λίστα, σε lower-case.
     */
    private static List<String> loadDictionaryWords(String filename) {
        List<String> words = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String w = line.trim();
                if (!w.isEmpty()) {
                    words.add(w.toLowerCase());
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading dictionary words from " + filename);
        }
        return words;
    }

    /**
     * ΕΠΙΛΟΓΗ 1:
     * Βρίσκει τις k πιο σημαντικές λέξεις που ξεκινούν με prefix, χρησιμοποιώντας
     * min-heap (PriorityQueue) μεγέθους ΤΟ ΠΟΛΥ k, όπως ζητάει η εκφώνηση.
     *
     * Ταξινόμηση κατά:
     *  - φθίνουσα σημαντικότητα (importance)
     *  - σε ισοβαθμία, αλφαβητικά (ascending)
     */
    private static List<String> topKFrequentWordsWithPrefix(
            CompressedTrie trie,
            List<String> dictionaryWords,
            String prefix,
            int k
    ) {
        if (k <= 0) return Collections.emptyList();

        // min-heap ΟΠΟΥ στην κορυφή είναι η "χειρότερη" από τις top-k
        // comparator: πρώτα κατά importance (ascending),
        // σε ισοβαθμία κατά λέξη (descending) ώστε να κρατάμε το "μεγαλύτερο"
        PriorityQueue<WordScore> heap = new PriorityQueue<>((a, b) -> {
            if (a.importance != b.importance) {
                return Integer.compare(a.importance, b.importance); // μικρότερο importance = "χειρότερο"
            }
            // για ισοβαθμία, "χειρότερη" θεωρούμε τη λεξικογραφικά ΜΕΓΑΛΥΤΕΡΗ
            return b.word.compareTo(a.word);
        });

        for (String w : dictionaryWords) {
            if (!w.startsWith(prefix)) continue;

            int imp = trie.getImportance(w);
            WordScore ws = new WordScore(w, imp);

            if (heap.size() < k) {
                heap.offer(ws);
            } else {
                // αν η νέα είναι "καλύτερη" από την κορυφή του heap, αντικατάστησέ την
                WordScore worst = heap.peek();
                if (isBetter(ws, worst)) {
                    heap.poll();
                    heap.offer(ws);
                }
            }
        }

        if (heap.isEmpty()) {
            return Collections.emptyList();
        }

        // Τώρα το heap περιέχει τις top-k (χωρίς ταξινόμηση).
        // Τις βάζουμε σε λίστα και τις ταξινομούμε όπως θέλει η εκφώνηση:
        // φθίνουσα importance, και σε ισοβαθμία αλφαβητικά.
        List<WordScore> scores = new ArrayList<>(heap);
        scores.sort((a, b) -> {
            if (b.importance != a.importance) {
                return Integer.compare(b.importance, a.importance);
            }
            return a.word.compareTo(b.word);
        });

        List<String> result = new ArrayList<>();
        for (WordScore ws : scores) {
            result.add(ws.word);
        }

        return result;
    }

    /**
     * Επιστρέφει true αν το a είναι "καλύτερο" από το b,
     * σύμφωνα με:
     *  - μεγαλύτερο importance
     *  - σε ισοβαθμία, αλφαβητικά μικρότερη λέξη.
     */
    private static boolean isBetter(WordScore a, WordScore b) {
        if (a.importance != b.importance) {
            return a.importance > b.importance;
        }
        return a.word.compareTo(b.word) < 0;
    }

    /**
     * ΕΠΙΛΟΓΗ 2:
     * Υπολογίζει averageFreq(prefix) = sum(importance(word)) / |T(prefix)|
     * όπου T(prefix) = όλες οι λέξεις του λεξικού που ξεκινούν με το prefix.
     * Αν δεν υπάρχει τέτοια λέξη, επιστρέφει 0.
     */
    private static double getAverageFrequencyOfPrefix(
            CompressedTrie trie,
            List<String> dictionaryWords,
            String prefix
    ) {
        long sum = 0;
        int count = 0;

        for (String w : dictionaryWords) {
            if (w.startsWith(prefix)) {
                sum += trie.getImportance(w);
                count++;
            }
        }

        if (count == 0) return 0.0;
        return (double) sum / count;
    }

    /**
     * ΕΠΙΛΟΓΗ 3:
     * Προβλέπει το επόμενο γράμμα για λέξη που ξεκινά με prefix.
     *
     * Βήματα:
     *  - βρίσκουμε όλους τους "επόμενους χαρακτήρες" μετά το prefix
     *    (δηλαδή για κάθε λέξη που ξεκινά με prefix, ο χαρακτήρας στη θέση prefix.length()).
     *  - για κάθε τέτοιο γράμμα c υπολογίζουμε averageFreq(prefix + c)
     *  - επιλέγουμε το γράμμα με τη ΜΕΓΙΣΤΗ μέση συχνότητα.
     *  - σε ισοβαθμία, μπορείς να πάρεις π.χ. το αλφαβητικά μικρότερο.
     *  - αν δεν υπάρχουν παιδιά, επιστρέφει null.
     */
    private static Character predictNextLetter(
            CompressedTrie trie,
            List<String> dictionaryWords,
            String prefix
    ) {
        // Βρίσκουμε τα υποψήφια επόμενα γράμματα
        Set<Character> nextLetters = new HashSet<>();

        for (String w : dictionaryWords) {
            if (w.startsWith(prefix) && w.length() > prefix.length()) {
                char c = w.charAt(prefix.length());
                nextLetters.add(c);
            }
        }

        if (nextLetters.isEmpty()) {
            return null; // δεν υπάρχουν παιδιά
        }

        Character bestChar = null;
        double bestAvg = Double.NEGATIVE_INFINITY;

        for (char c : nextLetters) {
            String extendedPrefix = prefix + c;
            double avg = getAverageFrequencyOfPrefix(trie, dictionaryWords, extendedPrefix);
            if (avg > bestAvg ||
                (Math.abs(avg - bestAvg) < 1e-9 && bestChar != null && c < bestChar)) {
                bestAvg = avg;
                bestChar = c;
            }
        }

        return bestChar;
    }

    // Μικρή βοηθητική κλάση για να αποθηκεύουμε λέξη + importance
    private static class WordScore {
        String word;
        int importance;

        WordScore(String word, int importance) {
            this.word = word;
            this.importance = importance;
        }
    }
}
