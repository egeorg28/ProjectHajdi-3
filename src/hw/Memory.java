package hw;

import java.util.List;

public class Memory {

    public static void main(String[] args) {
        // Διαλέγεις ΠΟΙΟ πραγματικό λεξικό θες ως πρότυπο:
        String templateDict = "/usr/share/dict/words";
        // ή "hw/dict.txt", ή "/mnt/c/Users/.../some_wordlist.txt"

        DictionaryModel model = LexiconStats.buildModelFromFile(templateDict);
        if (model == null) {
            System.out.println("Could not build model from " + templateDict);
            return;
        }

        SyntheticDict gen = new SyntheticDict(model, 12345L);

        int n = 10000;

        // π.χ. σενάριο fixed length:
        List<String> words = gen.generateFixedLength(n, 8);

        CompressedTrie cTrie = new CompressedTrie();
        for (String w : words) {
            cTrie.insert(w);
        }

        System.out.println("Inserted " + n + " words into CompressedTrie using template: " + templateDict);
    }
}
