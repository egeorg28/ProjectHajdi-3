package hw;

public class Memory {

    public static void main(String[] args) {

        String templateDict = "/usr/share/dict/words";  // ή ό,τι άλλο λεξικό θες

        DictionaryModel model = LexiconStats.buildModelFromFile(templateDict);
        if (model == null) {
            System.out.println("Could not build model from " + templateDict);
            return;
        }

        DeterministicSyntheticDict gen = new DeterministicSyntheticDict(model);

        int n = 10000;

        // Σενάριο 1: σταθερό μήκος
        String[] words = gen.generateFixedLength(n, 8);

        CompressedTrie cTrie = new CompressedTrie();
        for (int i = 0; i < n; i++) {
            cTrie.insert(words[i]);
        }

        System.out.println("Inserted " + n + " words into CompressedTrie using template: " + templateDict);
    }
}
