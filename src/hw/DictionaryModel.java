package hw;

/**
 * A data container (model) that stores statistical information extracted
 * from a template dictionary.  
 *
 * The purpose of this model is to provide:
 *  - letter probabilities (for generating letters)
 *  - length probabilities (for generating word lengths)
 *  - frequency counts used to compute those probabilities
 *
 * The DeterministicSyntheticDict uses this model to create synthetic words.
 */
public class DictionaryModel {

    /**
     * Maximum word length observed in the template dictionary.
     * All arrays that depend on word length follow this maximum.
     */
    public final int maxLen;

    /**
     * Total number of words found in the template dictionary.
     * Used to compute length distributions.
     */
    public final long totalWords;

    /**
     * Total number of characters across all words in the template dictionary.
     * Used to compute letter frequency distributions.
     */
    public final long totalChars;

    /**
     * wordCountByLength[L] = how many words of length L existed
     * in the template dictionary.
     *
     * Index range: 0..maxLen, though valid lengths start from 1.
     */
    public final long[] wordCountByLength;

    /**
     * totalLetterCount[i] = how many times the letter ('a' + i)
     * appeared in the template dictionary.
     *
     * Index 0 → 'a', 1 → 'b', …, 25 → 'z'.
     */
    public final long[] totalLetterCount;

    /**
     * letterProb[i] = probability of letter ('a' + i),
     * computed from totalLetterCount / totalChars.
     *
     * Array size = 26 (letters of the English alphabet).
     */
    public final double[] letterProb;

    /**
     * lengthProb[L] = probability that a word has length L,
     * computed using wordCountByLength / totalWords.
     *
     * Array size = maxLen + 1 (index 0 unused).
     */
    public final double[] lengthProb;

    /**
     * Constructor: stores all statistical data exactly as computed elsewhere.
     * No processing is done here; this class is simply a structured container.
     */
    public DictionaryModel(int maxLen,
                           long totalWords,
                           long totalChars,
                           long[] wordCountByLength,
                           long[] totalLetterCount,
                           double[] letterProb,
                           double[] lengthProb) {

        this.maxLen = maxLen;
        this.totalWords = totalWords;
        this.totalChars = totalChars;
        this.wordCountByLength = wordCountByLength;
        this.totalLetterCount = totalLetterCount;
        this.letterProb = letterProb;
        this.lengthProb = lengthProb;
    }
}
