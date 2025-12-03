package hw;

public class DictionaryModel {

    // Μέγιστο μήκος λέξης που λάβαμε από το template λεξικό
    public final int maxLen;

    // Συνολικός αριθμός λέξεων και χαρακτήρων στο template λεξικό
    public final long totalWords;
    public final long totalChars;

    // wordCountByLength[L] = πόσες λέξεις μήκους L είχε το template λεξικό
    public final long[] wordCountByLength;

    // totalLetterCount[i] = πόσες φορές εμφανίστηκε το γράμμα (char)('a'+i)
    public final long[] totalLetterCount;

    // letterProb[i] = πιθανότητα γράμματος (char)('a'+i)
    public final double[] letterProb;   // μέγεθος 26

    // lengthProb[L] = πιθανότητα μήκους L (1..maxLen)
    public final double[] lengthProb;   // μέγεθος maxLen+1

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
