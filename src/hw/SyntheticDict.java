package hw;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Γεννήτρια συνθετικών λεξικών που βασίζεται σε LexiconModel.
 *
 * - letterProbs από model.letterProbs
 * - lengthProbs από model.lengthProbs
 */
public class SyntheticDict {

    private final Random random;

    private final char[] LETTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private final double[] letterCumulative;
    private final int[] lengths;
    private final double[] lengthCumulative;

    public SyntheticDict(DictionaryModel model, long seed) {
        this.random = new Random(seed);
        this.letterCumulative = buildCumulative(model.letterProbs);
        this.lengths = model.lengths;
        this.lengthCumulative = buildCumulative(model.lengthProbs);
    }

    public SyntheticDict(DictionaryModel model) {
        this(model, System.currentTimeMillis());
    }

    private double[] buildCumulative(double[] probs) {
        double[] cum = new double[probs.length];
        double sum = 0.0;
        for (int i = 0; i < probs.length; i++) {
            sum += probs[i];
            cum[i] = sum;
        }
        for (int i = 0; i < cum.length; i++) {
            cum[i] /= sum;
        }
        return cum;
    }

    private char randomLetter() {
        double r = random.nextDouble();
        for (int i = 0; i < letterCumulative.length; i++) {
            if (r <= letterCumulative[i]) {
                return LETTERS[i];
            }
        }
        return LETTERS[LETTERS.length - 1];
    }

    private int randomLengthVariable() {
        double r = random.nextDouble();
        for (int i = 0; i < lengthCumulative.length; i++) {
            if (r <= lengthCumulative[i]) {
                return lengths[i];
            }
        }
        return lengths[lengths.length - 1];
    }

    /** Σενάριο (α): σταθερό μήκος. */
    public List<String> generateFixedLength(int n, int length) {
        List<String> words = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            StringBuilder sb = new StringBuilder(length);
            for (int j = 0; j < length; j++) {
                sb.append(randomLetter());
            }
            words.add(sb.toString());
        }
        return words;
    }

    /** Σενάριο (β): μεταβλητό μήκος, σύμφωνα με lengthProbs του model. */
    public List<String> generateVariableLength(int n) {
        List<String> words = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            int len = randomLengthVariable();
            StringBuilder sb = new StringBuilder(len);
            for (int j = 0; j < len; j++) {
                sb.append(randomLetter());
            }
            words.add(sb.toString());
        }
        return words;
    }

    public static void main(String[] args) {
        // Demo: φτιάχνουμε model από /usr/share/dict/words (ή όποιο file θέλεις)
        String dictFile = "/usr/share/dict/words"; // άλλαξέ το αν θες
        DictionaryModel model = LexiconStats.buildModelFromFile(dictFile);
        if (model == null) {
            System.out.println("Could not build model from " + dictFile);
            return;
        }

        SyntheticDict gen = new SyntheticDict(model, 12345L);

        System.out.println("=== Fixed length, n=10, length=8 ===");
        for (String w : gen.generateFixedLength(10, 8)) {
            System.out.println(w);
        }

        System.out.println("\n=== Variable length, n=10 ===");
        for (String w : gen.generateVariableLength(10)) {
            System.out.println(w + " (len=" + w.length() + ")");
        }
    }
}
