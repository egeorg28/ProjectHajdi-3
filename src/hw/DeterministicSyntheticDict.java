package hw;

/**
 * Γεννά συνθετικές λέξεις με βάση ένα DictionaryModel,
 * ΧΩΡΙΣ Random, ΧΩΡΙΣ List – μόνο arrays.
 *
 * - Γράμματα: βάση letterProb του model
 * - Μήκη: βάση lengthProb του model (για variable length)
 */
public class DeterministicSyntheticDict {

    private final DictionaryModel model;

    private final char[] letterPool;
    private final int[] lengthPool;

    private int letterIndex = 0;
    private int lengthIndex = 0;

    public DeterministicSyntheticDict(DictionaryModel model) {
        this.model = model;
        this.letterPool = buildLetterPool();
        this.lengthPool = buildLengthPool();
    }

    // ================== Letter pool ==================

    private char[] buildLetterPool() {
        int poolSize = 2000;
        char[] pool = new char[poolSize];

        double[] p = model.letterProb;

        double totalProb = 0.0;
        for (int i = 0; i < 26; i++) {
            totalProb += p[i];
        }
        if (totalProb == 0.0) {
            // fallback: a..z κυκλικά
            for (int i = 0; i < poolSize; i++) {
                pool[i] = (char) ('a' + (i % 26));
            }
            return pool;
        }

        int pos = 0;
        for (int i = 0; i < 26; i++) {
            if (p[i] <= 0.0) continue;
            int count = (int) Math.round(p[i] / totalProb * poolSize);
            char c = (char) ('a' + i);
            for (int k = 0; k < count && pos < poolSize; k++) {
                pool[pos++] = c;
            }
        }

        // συμπλήρωμα αν δεν γέμισε
        int maxIdx = indexOfMax(p);
        char fallbackChar = maxIdx >= 0 ? (char) ('a' + maxIdx) : 'e';
        while (pos < poolSize) {
            pool[pos++] = fallbackChar;
        }

        return pool;
    }

    private int indexOfMax(double[] arr) {
        if (arr == null || arr.length == 0) return -1;
        int idx = 0;
        double best = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > best) {
                best = arr[i];
                idx = i;
            }
        }
        return idx;
    }

    // ================== Length pool ==================

    private int[] buildLengthPool() {
        int poolSize = 500;
        int[] pool = new int[poolSize];

        double[] lp = model.lengthProb;
        int maxLen = model.maxLen;

        double totalProb = 0.0;
        for (int len = 1; len <= maxLen; len++) {
            totalProb += lp[len];
        }

        if (totalProb == 0.0) {
            // fallback: όλα 8
            for (int i = 0; i < poolSize; i++) {
                pool[i] = 8;
            }
            return pool;
        }

        int pos = 0;
        for (int len = 1; len <= maxLen; len++) {
            double p = lp[len];
            if (p <= 0.0) continue;
            int count = (int) Math.round(p / totalProb * poolSize);
            for (int k = 0; k < count && pos < poolSize; k++) {
                pool[pos++] = len;
            }
        }

        int mostCommonLen = argMaxLength(lp, maxLen);
        if (mostCommonLen <= 0) mostCommonLen = 8;

        while (pos < poolSize) {
            pool[pos++] = mostCommonLen;
        }

        return pool;
    }

    private int argMaxLength(double[] lp, int maxLen) {
        int bestLen = -1;
        double best = -1.0;
        for (int len = 1; len <= maxLen; len++) {
            if (lp[len] > best) {
                best = lp[len];
                bestLen = len;
            }
        }
        return bestLen;
    }

    // ================== helpers ==================

    private char nextLetter() {
        char c = letterPool[letterIndex];
        letterIndex = (letterIndex + 1) % letterPool.length;
        return c;
    }

    private int nextLength() {
        int len = lengthPool[lengthIndex];
        lengthIndex = (lengthIndex + 1) % lengthPool.length;
        return len;
    }

    // ================== PUBLIC API ==================

    // Σενάριο (α): σταθερό μήκος
    public String[] generateFixedLength(int n, int length) {
        String[] words = new String[n];
        for (int i = 0; i < n; i++) {
            char[] chars = new char[length];
            for (int j = 0; j < length; j++) {
                chars[j] = nextLetter();
            }
            words[i] = new String(chars);
        }
        return words;
    }

    // Σενάριο (β): ποικιλία μηκών
    public String[] generateVariableLength(int n) {
        String[] words = new String[n];
        for (int i = 0; i < n; i++) {
            int len = nextLength();
            if (len <= 0) len = 1;
            char[] chars = new char[len];
            for (int j = 0; j < len; j++) {
                chars[j] = nextLetter();
            }
            words[i] = new String(chars);
        }
        return words;
    }

    String[] generateVariableLengthArray(int n) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
