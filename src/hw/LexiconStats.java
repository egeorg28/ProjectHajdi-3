package hw;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Reads a word list (lexicon) file and computes various statistics:
 *  - total number of words and characters
 *  - word length distribution
 *  - overall letter frequency
 *  - letter frequency per word length
 *
 * It also exposes buildModelFromFile(...) which builds a DictionaryModel
 * used later by the synthetic word generator.
 */
public class LexiconStats {

    /**
     * Maximum word length we care about.
     * Words longer than this are ignored.
     */
    private static final int MAX_LEN = 30;

    public static void main(String[] args) {
        // We expect at least one argument: the wordlist file path
        if (args.length < 1) {
            System.out.println("Usage: java -cp src hw.LexiconStats <wordlist_file>");
            return;
        }

        String filename = args[0];

        // Compute detailed statistics (counts, distributions, etc.)
        StatsData data = computeStats(filename);
        if (data == null) return;  // If no usable data, stop here

        // Unpack the StatsData into local variables for convenience
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

        // --------------------------------------------------
        // WORD LENGTH DISTRIBUTION
        // --------------------------------------------------
        System.out.println("=== WORD LENGTH DISTRIBUTION ===");
        for (int len = 1; len <= MAX_LEN; len++) {
            if (wordCountByLength[len] == 0) continue;  // Skip lengths that never appear

            // Probability = (#words of this length) / (total words), expressed in %
            double p = (double) wordCountByLength[len] / totalWords * 100.0;

            System.out.printf("Length %2d: %8d words (%.2f%%)\n",
                    len, wordCountByLength[len], p);
        }
        System.out.println();

        // --------------------------------------------------
        // OVERALL LETTER FREQUENCY
        // --------------------------------------------------
        System.out.println("=== OVERALL LETTER FREQUENCY (percentage of all characters) ===");
        for (int i = 0; i < 26; i++) {
            long cnt = totalLetterCount[i];
            if (cnt == 0) continue;  // Skip letters that never appear

            // Probability = (count of this letter) / (total characters), expressed in %
            double p = (double) cnt / totalChars * 100.0;
            char c = (char) ('a' + i);

            System.out.printf("  %c: %.3f%%\n", c, p);
        }
        System.out.println();

        // --------------------------------------------------
        // LETTER FREQUENCY PER WORD LENGTH
        // --------------------------------------------------
        System.out.println("=== LETTER FREQUENCY PER WORD LENGTH ===");
        for (int len = 1; len <= MAX_LEN; len++) {
            // If there are no characters at this length, skip the whole block
            if (totalCharsByLength[len] == 0) continue;

            System.out.println("--- Length " + len + " ---");
            long totalCharsThisLen = totalCharsByLength[len];

            // For each letter, compute frequency within words of this specific length
            for (int i = 0; i < 26; i++) {
                long cnt = letterCountByLength[len][i];
                if (cnt == 0) continue;  // This letter does not appear in this length

                double p = (double) cnt / totalCharsThisLen * 100.0;
                char c = (char) ('a' + i);
                System.out.printf("  %c: %.3f%%\n", c, p);
            }
            System.out.println();
        }

        System.out.println("=== DONE ===");
    }

    // ====================================================================
    // IMPORTANT: The only buildModelFromFile we will keep and use.
    // This builds a DictionaryModel directly from a wordlist file.
    // ====================================================================
    public static DictionaryModel buildModelFromFile(String filename) {

        // Arrays indexed by length: 1..MAX_LEN
        long[] wordCountByLength = new long[MAX_LEN + 1];
        long[] totalCharsByLength = new long[MAX_LEN + 1];

        // letterCountByLength[len][i] = count of letter ('a' + i) in words of length len
        long[][] letterCountByLength = new long[MAX_LEN + 1][26];

        // totalLetterCount[i] = total count of letter ('a' + i) in the whole file
        long[] totalLetterCount = new long[26];

        long totalWords = 0;   // total number of valid words
        long totalChars = 0;   // total number of characters across all valid words

        // Try-with-resources: reader is automatically closed
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;

            // Read file line by line, each line is treated as one word candidate
            while ((line = br.readLine()) != null) {
                // Normalize: trim, to lowercase, keep only a-z
                String w = line.trim().toLowerCase().replaceAll("[^a-z]", "");
                int len = w.length();

                // Skip empty words or words longer than MAX_LEN
                if (len == 0 || len > MAX_LEN) continue;

                // Update global counters
                totalWords++;
                totalChars += len;
                wordCountByLength[len]++;
                totalCharsByLength[len] += len;

                // Update letter counts (per length and overall)
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

        // If no words or no characters, the file was not useful for our model
        if (totalWords == 0 || totalChars == 0) {
            System.out.println("No usable words found in file " + filename);
            return null;
        }

        // --------------------------------------------------
        // Compute LETTER PROBABILITIES
        // letterProb[i] = P(letter i) over all characters in the file
        // --------------------------------------------------
        double[] letterProb = new double[26];
        for (int i = 0; i < 26; i++) {
            letterProb[i] = (double) totalLetterCount[i] / (double) totalChars;
        }

        // --------------------------------------------------
        // Compute LENGTH PROBABILITIES
        // lengthProb[len] = P(word length = len) over all words in the file
        // --------------------------------------------------
        double[] lengthProb = new double[MAX_LEN + 1];
        for (int len = 1; len <= MAX_LEN; len++) {
            lengthProb[len] = (double) wordCountByLength[len] / (double) totalWords;
        }

        // Create and return a DictionaryModel that encapsulates all stats
        return new DictionaryModel(
                MAX_LEN,
                totalWords,
                totalChars,
                wordCountByLength,
                totalLetterCount,
                letterProb,
                lengthProb
        );
    }

    // ====================================================================
    // Internal helper class: used to return multiple statistics from
    // computeStats(...) in a structured way.
    // ====================================================================
    private static class StatsData {
        long totalWords;              // total number of valid words
        long totalChars;              // total number of characters
        long[] wordCountByLength;     // how many words of each length
        long[] totalCharsByLength;    // total chars for each length
        long[][] letterCountByLength; // per-length letter frequency
        long[] totalLetterCount;      // overall letter frequency
    }

    /**
     * Computes statistical data for a given filename.
     * This is used both by main (for printing stats) and conceptually mirrors
     * what buildModelFromFile does, but returns a StatsData object instead
     * of a DictionaryModel.
     */
    private static StatsData computeStats(String filename) {
        // Counters setup (same structure as in buildModelFromFile)
        long[] wordCountByLength = new long[MAX_LEN + 1];
        long[] totalCharsByLength = new long[MAX_LEN + 1];
        long[][] letterCountByLength = new long[MAX_LEN + 1][26];
        long[] totalLetterCount = new long[26];

        long totalWords = 0;
        long totalChars = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;

            while ((line = br.readLine()) != null) {
                // Clean and normalize word
                String w = line.trim().toLowerCase().replaceAll("[^a-z]", "");
                int len = w.length();
                if (len == 0 || len > MAX_LEN) continue;

                totalWords++;
                totalChars += len;
                wordCountByLength[len]++;
                totalCharsByLength[len] += len;

                // Update counts for each letter
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

        // If file had no valid words, we cannot compute stats
        if (totalWords == 0) return null;

        // Package all statistics into a StatsData object
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
