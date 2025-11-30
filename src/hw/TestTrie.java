
//  * Διαδραστική εφαρμογή:
//  *  - Φορτώνει λεξικό σε CompressedTrie
//  *  - Ενημερώνει importance από αρχείο κειμένου
//  *  - Προσφέρει μενού:
//  *      1. topKFrequentWordsWithPrefix
//  *      2. average frequency για prefix
//  *      3. predict next letter
//  *      4. exit
//  */
// public class TestTrie {

//     public static void main(String[] args) {

//         if (args.length < 2) {
//             System.out.println("Usage: java -cp . hw.TestTrie <dictionaryFile> <textFile>");
//             return;
//         }

//         String dictionaryFile = args[0];
//         String textFile       = args[1];

//         CompressedTrie trie = new CompressedTrie();

//         // ===== ΜΕΡΟΣ 1: ΦΟΡΤΩΣΗ ΛΕΞΙΚΟΥ =====
//         System.out.println("Loading dictionary from: " + dictionaryFile);
//         boolean ok = trie.loadDictionary(dictionaryFile);

//         if (!ok) {
//             System.out.println("Dictionary NOT loaded. Exiting...");
//             return;
//         }
//         System.out.println("Dictionary loaded successfully!");

//         // Φορτώνουμε τις λέξεις του λεξικού σε λίστα (για να κάνουμε prefix queries)
//         List<String> dictionaryWords = loadDictionaryWords(dictionaryFile);
//         if (dictionaryWords.isEmpty()) {
//             System.out.println("Dictionary file is empty. Exiting...");
//             return;
//         }

//         // ===== ΜΕΡΟΣ 2: ΕΝΗΜΕΡΩΣΗ importance ΑΠΟ ΚΕΙΜΕΝΟ =====
//         System.out.println("Updating importance from: " + textFile);
//         trie.updateImportanceFromText(textFile);
//         System.out.println("Finished processing text.");

//         // ===== ΔΙΑΔΡΑΣΤΙΚΟ ΜΕΝΟΥ =====
//         Scanner sc = new Scanner(System.in);
//         while (true) {
//             System.out.println();
//             System.out.println("===== MENU =====");
//             System.out.println("1. Εύρεση των k πιο σημαντικών λέξεων με πρόθεμα prefix");
//             System.out.println("2. Υπολογισμός μέσης συχνότητας λέξεων με πρόθεμα prefix");
//             System.out.println("3. Πρόβλεψη επόμενου γράμματος για πρόθεμα prefix");
//             System.out.println("4. Exit");
//             System.out.print("Choice: ");

//             String choice = sc.nextLine().trim();

//             if (choice.equals("1")) {
//                 // topKFrequentWordsWithPrefix
//                 System.out.print("Δώσε prefix: ");
//                 String prefix = sc.nextLine().trim().toLowerCase();

//                 System.out.print("Δώσε k: ");
//                 String kStr = sc.nextLine().trim();
//                 int k;
//                 try {
//                     k = Integer.parseInt(kStr);
//                 } catch (NumberFormatException e) {
//                     System.out.println("Μη έγκυρος αριθμός k.");
//                     continue;
//                 }

//                 List<String> result = topKFrequentWordsWithPrefix(trie, dictionaryWords, prefix, k);
//                 if (result.isEmpty()) {
//                     System.out.println("Δεν βρέθηκαν λέξεις με το δοθέν prefix.");
//                 } else {
//                     System.out.println("topKFrequentWordsWithPrefix(\"" + prefix + "\", " + k + ") →");
//                     // εκτύπωση όπως στο παράδειγμα: apple application ape
//                     for (int i = 0; i < result.size(); i++) {
//                         System.out.print(result.get(i));
//                         if (i < result.size() - 1) {
//                             System.out.print(" ");
//                         }
//                     }
//                     System.out.println();
//                 }

//             } else if (choice.equals("2")) {
//                 // average frequency για prefix
//                 System.out.print("Δώσε prefix: ");
//                 String prefix = sc.nextLine().trim().toLowerCase();

//                 double avg = getAverageFrequencyOfPrefix(trie, dictionaryWords, prefix);
//                 System.out.println("getAverageFrequencyOfPrefix(\"" + prefix + "\") → " + avg);

//             } else if (choice.equals("3")) {
//                 // predictNextLetter
//                 System.out.print("Δώσε prefix: ");
//                 String prefix = sc.nextLine().trim().toLowerCase();

//                 Character predicted = predictNextLetter(trie, dictionaryWords, prefix);
//                 if (predicted == null) {
//                     System.out.println("Δεν υπάρχει επόμενο γράμμα για πρόθεμα \"" + prefix + "\".");
//                 } else {
//                     System.out.println("predictNextLetter(\"" + prefix + "\") → '" + predicted + "'");
//                 }

//             } else if (choice.equals("4")) {
//                 System.out.println("Exiting...");
//                 break;
//             } else {
//                 System.out.println("Μη έγκυρη επιλογή.");
//             }
//         }

//         sc.close();
//     }

//     // =====================================================
//     // ΒΟΗΘΗΤΙΚΕΣ ΣΥΝΑΡΤΗΣΕΙΣ
//     // =====================================================

//     /**
//      * Διαβάζει ΟΛΕΣ τις λέξεις του λεξικού από αρχείο σε μία λίστα, σε lower-case.
//      */
//     private static List<String> loadDictionaryWords(String filename) {
//         List<String> words = new ArrayList<>();
//         try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
//             String line;
//             while ((line = br.readLine()) != null) {
//                 String w = line.trim();
//                 if (!w.isEmpty()) {
//                     words.add(w.toLowerCase());
//                 }
//             }
//         } catch (IOException e) {
//             System.out.println("Error reading dictionary words from " + filename);
//         }
//         return words;
//     }

//     /**
//      * ΕΠΙΛΟΓΗ 1:
//      * Βρίσκει τις k πιο σημαντικές λέξεις που ξεκινούν με prefix, χρησιμοποιώντας
//      * min-heap (PriorityQueue) μεγέθους ΤΟ ΠΟΛΥ k, όπως ζητάει η εκφώνηση.
//      *
//      * Ταξινόμηση κατά:
//      *  - φθίνουσα σημαντικότητα (importance)
//      *  - σε ισοβαθμία, αλφαβητικά (ascending)
//      */
//     private static List<String> topKFrequentWordsWithPrefix(
//             CompressedTrie trie,
//             List<String> dictionaryWords,
//             String prefix,
//             int k
//     ) {
//         if (k <= 0) return Collections.emptyList();

//         // min-heap ΟΠΟΥ στην κορυφή είναι η "χειρότερη" από τις top-k
//         // comparator: πρώτα κατά importance (ascending),
//         // σε ισοβαθμία κατά λέξη (descending) ώστε να κρατάμε το "μεγαλύτερο"
//         PriorityQueue<WordScore> heap = new PriorityQueue<>((a, b) -> {
//             if (a.importance != b.importance) {
//                 return Integer.compare(a.importance, b.importance); // μικρότερο importance = "χειρότερο"
//             }
//             // για ισοβαθμία, "χειρότερη" θεωρούμε τη λεξικογραφικά ΜΕΓΑΛΥΤΕΡΗ
//             return b.word.compareTo(a.word);
//         });

//         for (String w : dictionaryWords) {
//             if (!w.startsWith(prefix)) continue;

//             int imp = trie.getImportance(w);
//             WordScore ws = new WordScore(w, imp);

//             if (heap.size() < k) {
//                 heap.offer(ws);
//             } else {
//                 // αν η νέα είναι "καλύτερη" από την κορυφή του heap, αντικατάστησέ την
//                 WordScore worst = heap.peek();
//                 if (isBetter(ws, worst)) {
//                     heap.poll();
//                     heap.offer(ws);
//                 }
//             }
//         }

//         if (heap.isEmpty()) {
//             return Collections.emptyList();
//         }

//         // Τώρα το heap περιέχει τις top-k (χωρίς ταξινόμηση).
//         // Τις βάζουμε σε λίστα και τις ταξινομούμε όπως θέλει η εκφώνηση:
//         // φθίνουσα importance, και σε ισοβαθμία αλφαβητικά.
//         List<WordScore> scores = new ArrayList<>(heap);
//         scores.sort((a, b) -> {
//             if (b.importance != a.importance) {
//                 return Integer.compare(b.importance, a.importance);
//             }
//             return a.word.compareTo(b.word);
//         });

//         List<String> result = new ArrayList<>();
//         for (WordScore ws : scores) {
//             result.add(ws.word);
//         }

//         return result;
//     }

//     /**
//      * Επιστρέφει true αν το a είναι "καλύτερο" από το b,
//      * σύμφωνα με:
//      *  - μεγαλύτερο importance
//      *  - σε ισοβαθμία, αλφαβητικά μικρότερη λέξη.
//      */
//     private static boolean isBetter(WordScore a, WordScore b) {
//         if (a.importance != b.importance) {
//             return a.importance > b.importance;
//         }
//         return a.word.compareTo(b.word) < 0;
//     }

//     /**
//      * ΕΠΙΛΟΓΗ 2:
//      * Υπολογίζει averageFreq(prefix) = sum(importance(word)) / |T(prefix)|
//      * όπου T(prefix) = όλες οι λέξεις του λεξικού που ξεκινούν με το prefix.
//      * Αν δεν υπάρχει τέτοια λέξη, επιστρέφει 0.
//      */
//     private static double getAverageFrequencyOfPrefix(
//             CompressedTrie trie,
//             List<String> dictionaryWords,
//             String prefix
//     ) {
//         long sum = 0;
//         int count = 0;

//         for (String w : dictionaryWords) {
//             if (w.startsWith(prefix)) {
//                 sum += trie.getImportance(w);
//                 count++;
//             }
//         }

//         if (count == 0) return 0.0;
//         return (double) sum / count;
//     }

//     /**
//      * ΕΠΙΛΟΓΗ 3:
//      * Προβλέπει το επόμενο γράμμα για λέξη που ξεκινά με prefix.
//      *
//      * Βήματα:
//      *  - βρίσκουμε όλους τους "επόμενους χαρακτήρες" μετά το prefix
//      *    (δηλαδή για κάθε λέξη που ξεκινά με prefix, ο χαρακτήρας στη θέση prefix.length()).
//      *  - για κάθε τέτοιο γράμμα c υπολογίζουμε averageFreq(prefix + c)
//      *  - επιλέγουμε το γράμμα με τη ΜΕΓΙΣΤΗ μέση συχνότητα.
//      *  - σε ισοβαθμία, μπορείς να πάρεις π.χ. το αλφαβητικά μικρότερο.
//      *  - αν δεν υπάρχουν παιδιά, επιστρέφει null.
//      */
//     private static Character predictNextLetter(
//             CompressedTrie trie,
//             List<String> dictionaryWords,
//             String prefix
//     ) {
//         // Βρίσκουμε τα υποψήφια επόμενα γράμματα
//         Set<Character> nextLetters = new HashSet<>();

//         for (String w : dictionaryWords) {
//             if (w.startsWith(prefix) && w.length() > prefix.length()) {
//                 char c = w.charAt(prefix.length());
//                 nextLetters.add(c);
//             }
//         }

//         if (nextLetters.isEmpty()) {
//             return null; // δεν υπάρχουν παιδιά
//         }

//         Character bestChar = null;
//         double bestAvg = Double.NEGATIVE_INFINITY;

//         for (char c : nextLetters) {
//             String extendedPrefix = prefix + c;
//             double avg = getAverageFrequencyOfPrefix(trie, dictionaryWords, extendedPrefix);
//             if (avg > bestAvg ||
//                 (Math.abs(avg - bestAvg) < 1e-9 && bestChar != null && c < bestChar)) {
//                 bestAvg = avg;
//                 bestChar = c;
//             }
//         }

//         return bestChar;
//     }

//     // Μικρή βοηθητική κλάση για να αποθηκεύουμε λέξη + importance
//     private static class WordScore {
//         String word;
//         int importance;

//         WordScore(String word, int importance) {
//             this.word = word;
//             this.importance = importance;
//         }
//     }
// }



/**
 * Αυτό το test ΔΕΝ διαβάζει αρχεία.
 * Φτιάχνει ένα μικρό λεξικό με τις λέξεις του παραδείγματος:
 *
 *  "apple"       (50 εμφανίσεις)
 *  "class"       (30 εμφανίσεις)
 *  "application" (30 εμφανίσεις)
 *  "ucy"         (50 εμφανίσεις)
 *  "apt"         (5 εμφανίσεις)
 *  "ape"         (10 εμφανίσεις)
 *
 * και ελέγχει:
 *  - topKFrequentWordsWithPrefix("ap", 3)
 *  - getAverageFrequencyOfPrefix("ap")
 *  - predictNextLetter("ap")
 *
 * Τα αναμενόμενα αποτελέσματα (από εκφώνηση) είναι:
 *  topK → apple application ape
 *  avg  → 23.75
 *  next → 'p'
 */

package hw;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Διαδραστική εφαρμογή για το project:
 *
 * 1. Διαβάζει αρχεία:
 *      - λεξικό (dictionary file)
 *      - αρχείο ελεύθερου κειμένου (text file)
 * 2. Φτιάχνει CompressedTrie και ενημερώνει τη σημαντικότητα (importance)
 * 3. Προσφέρει μενού:
 *      1) topKFrequentWordsWithPrefix(prefix, k)
 *      2) getAverageFrequencyOfPrefix(prefix)
 *      3) predictNextLetter(prefix)
 *      4) Έξοδος
 */
public class TestTrie {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.println("=== ΕΦΑΡΜΟΓΗ AUTOCOMPLETE ΜΕ COMPRESSED TRIE ===");

        // 1. Ζητάμε από τον χρήστη τα ονόματα των αρχείων
        System.out.print("Δώσε όνομα αρχείου λεξικού (π.χ. hw/dict.txt): ");
        String dictFile = sc.nextLine().trim();

        System.out.print("Δώσε όνομα αρχείου κειμένου (π.χ. hw/text.txt): ");
        String textFile = sc.nextLine().trim();

        // 2. Φτιάχνουμε το trie
        CompressedTrie trie = new CompressedTrie();

        // 3. Φορτώνουμε λεξικό
        System.out.println("\nΦόρτωση λεξικού από: " + dictFile);
        boolean loaded = trie.loadDictionary(dictFile);
        if (!loaded) {
            System.out.println("Σφάλμα: Δεν ήταν δυνατή η φόρτωση του λεξικού. Τερματισμός.");
            sc.close();
            return;
        }
        System.out.println("✔ Το λεξικό φορτώθηκε επιτυχώς.");

        // 4. Διαβάζουμε όλες τις λέξεις από το λεξικό σε λίστα (για τα prefix queries)
        List<String> dictionaryWords = loadDictionaryWords(dictFile);
        if (dictionaryWords.isEmpty()) {
            System.out.println("Σφάλμα: Το αρχείο λεξικού δεν περιέχει λέξεις. Τερματισμός.");
            sc.close();
            return;
        }

        // 5. Ενημερώνουμε τη σημαντικότητα από το αρχείο κειμένου
        System.out.println("\nΕνημέρωση σημαντικότητας από: " + textFile);
        trie.updateImportanceFromText(textFile);
        System.out.println("✔ Ολοκληρώθηκε η ενημέρωση σημαντικότητας.\n");

        // 6. Διαδραστικό μενού
        while (true) {
            System.out.println("===== ΜΕΝΟΥ =====");
            System.out.println("1. Εύρεση των k πιο σημαντικών λέξεων με πρόθεμα prefix");
            System.out.println("2. Υπολογισμός μέσης συχνότητας λέξεων με πρόθεμα prefix");
            System.out.println("3. Πρόβλεψη επόμενου γράμματος για πρόθεμα prefix");
            System.out.println("4. Έξοδος");
            System.out.print("Επιλογή: ");

            String choice = sc.nextLine().trim();

            if (choice.equals("1")) {
                // ---- Επιλογή 1: topKFrequentWordsWithPrefix ----
                System.out.print("Δώσε prefix: ");
                String prefix = sc.nextLine().trim().toLowerCase();

                System.out.print("Δώσε k: ");
                String kStr = sc.nextLine().trim();
                int k;
                try {
                    k = Integer.parseInt(kStr);
                } catch (NumberFormatException e) {
                    System.out.println("Μη έγκυρος αριθμός k.\n");
                    continue;
                }

                List<String> result = topKFrequentWordsWithPrefix(trie, dictionaryWords, prefix, k);
                if (result.isEmpty()) {
                    System.out.println("Δεν βρέθηκαν λέξεις που να ξεκινούν με \"" + prefix + "\".\n");
                } else {
                    System.out.println("topKFrequentWordsWithPrefix(\"" + prefix + "\", " + k + ") →");
                    for (int i = 0; i < result.size(); i++) {
                        if (i > 0) System.out.print(" ");
                        System.out.print(result.get(i));
                    }
                    System.out.println("\n");
                }

            } else if (choice.equals("2")) {
                // ---- Επιλογή 2: μέση συχνότητα ----
                System.out.print("Δώσε prefix: ");
                String prefix = sc.nextLine().trim().toLowerCase();

                double avg = getAverageFrequencyOfPrefix(trie, dictionaryWords, prefix);
                System.out.println("getAverageFrequencyOfPrefix(\"" + prefix + "\") → " + avg + "\n");

            } else if (choice.equals("3")) {
                // ---- Επιλογή 3: πρόβλεψη επόμενου γράμματος ----
                System.out.print("Δώσε prefix: ");
                String prefix = sc.nextLine().trim().toLowerCase();

                Character predicted = predictNextLetter(trie, dictionaryWords, prefix);
                if (predicted == null) {
                    System.out.println("Δεν υπάρχει επόμενο γράμμα για πρόθεμα \"" + prefix + "\".\n");
                } else {
                    System.out.println("predictNextLetter(\"" + prefix + "\") → '" + predicted + "'\n");
                }

            } else if (choice.equals("4")) {
                System.out.println("Έξοδος από την εφαρμογή. Αντίο!");
                break;

            } else {
                System.out.println("Μη έγκυρη επιλογή.\n");
            }
        }

        sc.close();
    }

    // ========================= ΒΟΗΘΗΤΙΚΕΣ =========================

    /**
     * Διαβάζει όλες τις λέξεις του λεξικού σε μια λίστα (σε lower-case).
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
            System.out.println("Σφάλμα κατά την ανάγνωση του λεξικού: " + e.getMessage());
        }
        return words;
    }

    // Μικρή βοηθητική κλάση για να κρατάμε (λέξη, importance)
    private static class WordScore {
        String word;
        int importance;
        WordScore(String w, int imp) {
            this.word = w;
            this.importance = imp;
        }
    }

    /**
     * Σύγκριση "ποιός είναι καλύτερος" για top-k:
     *  - μεγαλύτερη σημαντικότητα
     *  - σε ισοβαθμία, αλφαβητικά μικρότερη λέξη
     */
    private static boolean isBetter(WordScore a, WordScore b) {
        if (a.importance != b.importance) {
            return a.importance > b.importance;
        }
        return a.word.compareTo(b.word) < 0;
    }

    /**
     * ΕΠΙΛΟΓΗ 1:
     * Βρίσκει τις k πιο σημαντικές λέξεις που ξεκινούν με prefix,
     * χρησιμοποιώντας min-heap μεγέθους το πολύ k.
     */
    private static List<String> topKFrequentWordsWithPrefix(
            CompressedTrie trie,
            List<String> dictionaryWords,
            String prefix,
            int k
    ) {
        if (k <= 0) return Collections.emptyList();

        // Min-heap όπου στην κορυφή είναι η "χειρότερη" από τις top-k
        PriorityQueue<WordScore> heap = new PriorityQueue<>((a, b) -> {
            if (a.importance != b.importance) {
                return Integer.compare(a.importance, b.importance);
            }
            // σε ισοβαθμία, χειρότερη θεωρούμε τη λεξικογραφικά μεγαλύτερη
            return b.word.compareTo(a.word);
        });

        for (String w : dictionaryWords) {
            if (!w.startsWith(prefix)) continue;

            int imp = trie.getImportance(w);
            WordScore ws = new WordScore(w, imp);

            if (heap.size() < k) {
                heap.offer(ws);
            } else {
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

        // Μετατρέπουμε σε λίστα και ταξινομούμε:
        //  - κατά importance (φθίνουσα)
        //  - σε ισοβαθμία αλφαβητικά
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
     * ΕΠΙΛΟΓΗ 2:
     * Υπολογίζει:
     *   averageFreq(prefix) = sum(importance(w)) / |T(prefix)|
     * όπου T(prefix) = όλες οι λέξεις που ξεκινούν με prefix.
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
     * Πρόβλεψη επόμενου γράμματος για prefix.
     */
    private static Character predictNextLetter(
            CompressedTrie trie,
            List<String> dictionaryWords,
            String prefix
    ) {
        // Βρίσκουμε όλους τους πιθανούς επόμενους χαρακτήρες
        Set<Character> nextLetters = new HashSet<>();

        for (String w : dictionaryWords) {
            if (w.startsWith(prefix) && w.length() > prefix.length()) {
                char c = w.charAt(prefix.length());
                nextLetters.add(c);
            }
        }

        if (nextLetters.isEmpty()) {
            return null;
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
}
