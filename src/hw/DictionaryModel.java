package hw;

/**
 * Κρατάει το "μοντέλο λεξικού":
 *  - πιθανότητες για κάθε γράμμα a..z
 *  - κατανομή μήκους λέξης (ποια lengths και με ποια πιθανότητα)
 */
public class DictionaryModel {

    /** letterProbs[i] = πιθανότητα για γράμμα (char)('a' + i), i=0..25, άθροισμα ~1.0 */
    public final double[] letterProbs;

    /** lengths[k] = ένα μήκος λέξης που εμφανίζεται στο πραγματικό λεξικό (π.χ. 3,4,5,...) */
    public final int[] lengths;

    /** lengthProbs[k] = πιθανότητα να έχει μια λέξη μήκος lengths[k], άθροισμα ~1.0 */
    public final double[] lengthProbs;

    public DictionaryModel(double[] letterProbs, int[] lengths, double[] lengthProbs) {
        this.letterProbs = letterProbs;
        this.lengths = lengths;
        this.lengthProbs = lengthProbs;
    }
}
