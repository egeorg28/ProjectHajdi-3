package hw;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LexiconStats {

    private static final int MAX_LEN = 30;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java -cp src hw.LexiconStats <wordlist_file>");
            return;
        }

        String filename = args[0];
        StatsData data = computeStats(filename);
        if (data == null) return;

        long totalWords = data.totalWords;
        long totalChars = data.totalChars;
        long[] wordCountByLength = data.wordCountByLength;
        long[] totalLetterCount = data.totalLetterCount;
        long[] totalCharsByLength = data.totalCharsByLength;
        long[][] letterCountByLength = data.letterCountByLength;

        System.out.println("=== LEXICON STATS FOR: " + filename + " ===");
        System.out.println("Total words: " + totalWords);
        System.out.println("Total characters: " + totalChars);
        System.out.println();

        // 1. Κατανομή μήκους λέξεων
        System.out.println("=== WORD LENGTH DISTRIBUTION ===");
        for (int len = 1; len <= MAX_LEN; len++) {
            if (wordCountByLength[len] == 0) continue;
            double p = (double) wordCountByLength[len] / totalWords * 100.0;
            System.out.printf("Length %2d: %8d words (%.2f%%)\n",
                    len, wordCountByLength[len], p);
        }
        System.out.println();

        // 2. Συνολική συχνότητα γραμμάτων
        System.out.println("=== OVERALL LETTER FREQUENCY (percentage of all characters) ===");
        for (int i = 0; i < 26; i++) {
            long cnt = totalLetterCount[i];
            if (cnt == 0) continue;
            double p = (double) cnt / totalChars * 100.0;
            char c = (char) ('a' + i);
            System.out.printf("  %c: %.3f%%\n", c, p);
        }
        System.out.println();

        // 3. Συχνότητα γραμμάτων ανά μήκος
        System.out.println("=== LETTER FREQUENCY PER WORD LENGTH ===");
        for (int len = 1; len <= MAX_LEN; len++) {
            if (totalCharsByLength[len] == 0) continue;

            System.out.println("--- Length " + len + " ---");
            long totalCharsThisLen = totalCharsByLength[len];

            for (int i = 0; i < 26; i++) {
                long cnt = letterCountByLength[len][i];
                if (cnt == 0) continue;
                double p = (double) cnt / totalCharsThisLen * 100.0;
                char c = (char) ('a' + i);
                System.out.printf("  %c: %.3f%%\n", c, p);
            }
            System.out.println();
        }

        System.out.println("=== DONE ===");
    }

    /**
     * Χρησιμοποιείται από άλλα κομμάτια του κώδικα για να χτίσουμε LexiconModel
     * από ένα πραγματικό λεξικό (filename).
     */
    public static DictionaryModel buildModelFromFile(String filename) {
        StatsData data = computeStats(filename);
        if (data == null) return null;

        long totalChars = data.totalChars;
        long totalWords = data.totalWords;
        long[] totalLetterCount = data.totalLetterCount;
        long[] wordCountByLength = data.wordCountByLength;

        // 1. letterProbs[i]
        double[] letterProbs = new double[26];
        for (int i = 0; i < 26; i++) {
            if (totalChars == 0) {
                letterProbs[i] = 0.0;
            } else {
                letterProbs[i] = (double) totalLetterCount[i] / totalChars;
            }
        }

        // 2. lengths[] και lengthProbs[]
        int nonZeroLenCount = 0;
        for (int len = 1; len <= MAX_LEN; len++) {
            if (wordCountByLength[len] > 0) nonZeroLenCount++;
        }

        int[] lengths = new int[nonZeroLenCount];
        double[] lengthProbs = new double[nonZeroLenCount];

        int idx = 0;
        for (int len = 1; len <= MAX_LEN; len++) {
            long wc = wordCountByLength[len];
            if (wc == 0) continue;
            lengths[idx] = len;
            lengthProbs[idx] = (double) wc / totalWords;
            idx++;
        }

        return new DictionaryModel(letterProbs, lengths, lengthProbs);
    }

    // Εσωτερική βοηθητική κλάση μόνο για να μη διπλασιάζουμε κώδικα
    private static class StatsData {
        long totalWords;
        long totalChars;
        long[] wordCountByLength;
        long[] totalCharsByLength;
        long[][] letterCountByLength;
        long[] totalLetterCount;
    }

    // Κοινός κώδικας για διάβασμα λεξικού και υπολογισμό στατιστικών
    private static StatsData computeStats(String filename) {
        long[] wordCountByLength = new long[MAX_LEN + 1];
        long[] totalCharsByLength = new long[MAX_LEN + 1];
        long[][] letterCountByLength = new long[MAX_LEN + 1][26];
        long[] totalLetterCount = new long[26];

        long totalWords = 0;
        long totalChars = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;

            while ((line = br.readLine()) != null) {
                String w = line.trim().toLowerCase().replaceAll("[^a-z]", "");
                int len = w.length();
                if (len == 0 || len > MAX_LEN) {
                    continue;
                }

                totalWords++;
                wordCountByLength[len]++;
                totalChars += len;
                totalCharsByLength[len] += len;

                for (int i = 0; i < len; i++) {
                    char c = w.charAt(i);
                    if (c >= 'a' && c <= 'z') {
                        int idx = c - 'a';
                        letterCountByLength[len][idx]++;
                        totalLetterCount[idx]++;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file " + filename + ": " + e.getMessage());
            return null;
        }

        if (totalWords == 0) {
            System.out.println("No usable words found in file " + filename);
            return null;
        }

        StatsData d = new StatsData();
        d.totalWords = totalWords;
        d.totalChars = totalChars;
        d.wordCountByLength = wordCountByLength;
        d.totalCharsByLength = totalCharsByLength;
        d.letterCountByLength = letterCountByLength;
        d.totalLetterCount = totalLetterCount;
        return d;
    }
}
