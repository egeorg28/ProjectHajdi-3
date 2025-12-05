package hw;

/**
 * Generates synthetic (artificial) words based on a DictionaryModel,
 * WITHOUT using Random and WITHOUT using Lists – only arrays.
 *
 * Concept:
 *  - We pre-construct two “pools”:
 *      1) letterPool: a large array of letters whose frequencies approximate the
 *         letterProb distribution of the model.
 *      2) lengthPool: a large array of integers whose frequencies approximate the
 *         lengthProb distribution of the model.
 *  - To obtain the next letter or next length, we do NOT use randomness.
 *    Instead, we cycle deterministically through each pool using an index.
 *
 * This makes the generator fully deterministic while still respecting the
 * probability distributions provided by the model.
 */
public class DeterministicSyntheticDict {

    /**
     * The model providing:
     *  - letterProb: probability of each letter (a–z)
     *  - lengthProb: probability of each possible word length
     *  - maxLen: maximum allowed word length
     */
    private final DictionaryModel model;

    /**
     * Pool of letters.
     * This array contains many characters (e.g., 2000 entries), where the
     * frequency of each letter corresponds to its probability in the model.
     * Example: if 'e' has a high probability, it will appear many times in the array.
     */
    private final char[] letterPool;

    /**
     * Pool of word lengths.
     * This array contains many integers (e.g., 500 entries), where each integer
     * represents a possible word length. The frequency of each length corresponds
     * to its probability in the model.
     */
    private final int[] lengthPool;

    /**
     * Index pointing to the current position in letterPool.
     * It moves cyclically:
     *  - whenever we request a new letter, we read letterPool[letterIndex]
     *  - then we increment letterIndex and wrap around using modulo
     */
    private int letterIndex = 0;

    /**
     * Index pointing to the current position in lengthPool.
     * Same cyclic behavior as letterIndex.
     */
    private int lengthIndex = 0;

    /**
     * Constructor:
     *  - stores the model
     *  - builds both the letter and length pools based on model probabilities.
     */
    public DeterministicSyntheticDict(DictionaryModel model) {
        this.model = model;
        this.letterPool = buildLetterPool();
        this.lengthPool = buildLengthPool();
    }

    // ================== Letter pool ==================

    /**
     * Builds the letterPool array.
     *
     * Steps:
     *  1. Choose a fixed pool size (2000).
     *  2. Normalize probabilities: compute the sum of p(a..z).
     *  3. For each letter with positive probability, allocate a proportional number
     *     of positions in the pool.
     *  4. If the computed sizes do not fill the entire array, fill remaining slots
     *     with the letter that has the highest probability (fallback).
     *
     * If the model provides zero total probability, we fall back to a simple
     * cyclic a–z pattern.
     */
    private char[] buildLetterPool() {
        int poolSize = 2000;
        char[] pool = new char[poolSize];

        double[] p = model.letterProb;

        // Compute total probability (used for normalization)
        double totalProb = 0.0;
        for (int i = 0; i < 26; i++) {
            totalProb += p[i];
        }

        // If all probabilities are zero → fallback pattern a, b, c, ..., z, a, b, ...
        if (totalProb == 0.0) {
            for (int i = 0; i < poolSize; i++) {
                pool[i] = (char) ('a' + (i % 26));
            }
            return pool;
        }

        // Fill pool proportionally to probabilities
        int pos = 0;
        for (int i = 0; i < 26; i++) {
            if (p[i] <= 0.0) continue;

            // Number of times this letter should appear in the pool
            int count = (int) Math.round(p[i] / totalProb * poolSize);
            char c = (char) ('a' + i);

            for (int k = 0; k < count && pos < poolSize; k++) {
                pool[pos++] = c;
            }
        }

        // If the pool is not completely filled, use the most probable letter as fallback
        int maxIdx = indexOfMax(p);
        char fallbackChar = maxIdx >= 0 ? (char) ('a' + maxIdx) : 'e';

        while (pos < poolSize) {
            pool[pos++] = fallbackChar;
        }

        return pool;
    }

    /**
     * Returns the index of the maximum value in a double array.
     * Used to identify which letter has the highest probability.
     */
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

    /**
     * Builds the lengthPool array.
     *
     * Steps:
     *  1. Choose a pool size (500).
     *  2. Compute total probability over valid lengths (1..maxLen).
     *  3. For each length with positive probability, allocate a proportional
     *     number of positions in the pool.
     *  4. If the pool is not fully filled, use the most probable length as fallback.
     *
     * If all probabilities are zero, fallback is fixed length 8.
     */
    private int[] buildLengthPool() {
        int poolSize = 500;
        int[] pool = new int[poolSize];

        double[] lp = model.lengthProb;
        int maxLen = model.maxLen;

        // Compute total probability
        double totalProb = 0.0;
        for (int len = 1; len <= maxLen; len++) {
            totalProb += lp[len];
        }

        // Fallback: if all probabilities are zero, fill with length 8
        if (totalProb == 0.0) {
            for (int i = 0; i < poolSize; i++) {
                pool[i] = 8;
            }
            return pool;
        }

        // Fill pool according to probabilities
        int pos = 0;
        for (int len = 1; len <= maxLen; len++) {
            double p = lp[len];
            if (p <= 0.0) continue;

            int count = (int) Math.round(p / totalProb * poolSize);
            for (int k = 0; k < count && pos < poolSize; k++) {
                pool[pos++] = len;
            }
        }

        // Fallback if pool not filled
        int mostCommonLen = argMaxLength(lp, maxLen);
        if (mostCommonLen <= 0) mostCommonLen = 8;

        while (pos < poolSize) {
            pool[pos++] = mostCommonLen;
        }

        return pool;
    }

    /**
     * Returns the length (1..maxLen) with the highest probability.
     */
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

    /**
     * Returns the next letter from letterPool and advances the index cyclically.
     */
    private char nextLetter() {
        char c = letterPool[letterIndex];
        letterIndex = (letterIndex + 1) % letterPool.length;
        return c;
    }

    /**
     * Returns the next length from lengthPool and advances the index cyclically.
     */
    private int nextLength() {
        int len = lengthPool[lengthIndex];
        lengthIndex = (lengthIndex + 1) % lengthPool.length;
        return len;
    }

    // ================== PUBLIC API ==================

    /**
     * Scenario (a):
     * Generate n words, all with the same fixed length.
     *
     * For each word:
     *  - allocate a char array of given length
     *  - fill it by repeatedly calling nextLetter()
     *  - convert char[] to String
     */
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

    /**
     * Scenario (b):
     * Generate n words, each with variable length determined by nextLength().
     *
     * Process:
     *  - pick a length from lengthPool
     *  - ensure it is at least 1
     *  - fill the word using nextLetter()
     */
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

    /**
     * Placeholder method (not implemented on purpose).
     * If called, it always throws an exception.
     */
    String[] generateVariableLengthArray(int n) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
