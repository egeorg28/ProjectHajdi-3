package hw;

/**
 * Small helper program to:
 *  1) Build a DictionaryModel from a real dictionary file
 *  2) Use a deterministic generator to create synthetic words
 *  3) Insert those words into a CompressedTrie
 *
 * This class is mainly for quick experiments and sanity checks,
 * not for full memory comparison (that’s what MemoryEx does).
 */
public class Memory {

    public static void main(String[] args) {

        // Path to the template dictionary used to build the statistical model.
        // You can change this to any wordlist you want.
        String templateDict = "/usr/share/dict/words";  // or any other dictionary file

        // Build a DictionaryModel from the template dictionary.
        // This computes letter and length distributions.
        DictionaryModel model = LexiconStats.buildModelFromFile(templateDict);
        if (model == null) {
            System.out.println("Could not build model from " + templateDict);
            return;
        }

        // Deterministic synthetic dictionary generator, based on the model.
        // It will always generate the same sequence of words for this model.
        DeterministicSyntheticDict gen = new DeterministicSyntheticDict(model);

        // Number of synthetic words to generate.
        int n = 10000;

        // Scenario 1: all words have fixed length 8.
        // generateFixedLength returns a plain String[] (no Lists).
        String[] words = gen.generateFixedLength(n, 8);

        // Build a CompressedTrie and insert all generated words into it.
        CompressedTrie cTrie = new CompressedTrie();
        for (int i = 0; i < n; i++) {
            cTrie.insert(words[i]);
        }

        // Simple confirmation message.
        System.out.println("Inserted " + n + " words into CompressedTrie using template: " + templateDict);
    }
}
