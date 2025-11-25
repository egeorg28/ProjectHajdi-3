package hw;


    // public static void main(String[] args) {
    //     CompressedTrie trie = new CompressedTrie();

    //     // 1. Μικρό λεξικό
    //     String[] dict = {
    //         "apple",       // 50
    //         "class",       // 30
    //         "application", // 30
    //         "ucy",         // 50
    //         "apt",         // 5
    //         "ape"          // 10
    //     };

    //     for (String w : dict) {
    //         trie.insert(w);
    //     }

    //     // 2. "Φορτώνουμε" συχνότητες χειροκίνητα
    //     // apple (50), application (30), class (30), ucy (50), apt (5), ape (10)

    //     for (int i = 0; i < 50; i++) trie.increaseImportance("apple");
    //     for (int i = 0; i < 30; i++) trie.increaseImportance("application");
    //     for (int i = 0; i < 30; i++) trie.increaseImportance("class");
    //     for (int i = 0; i < 50; i++) trie.increaseImportance("ucy");
    //     for (int i = 0; i < 5;  i++) trie.increaseImportance("apt");
    //     for (int i = 0; i < 10; i++) trie.increaseImportance("ape");

    //     // -------- Βασικά tests --------

    //     // TEST 1: search
    //     System.out.println("search(apple) = " + trie.search("apple"));  // true
    //     System.out.println("search(app)   = " + trie.search("app"));    // false
    //     System.out.println("search(ucy)   = " + trie.search("ucy"));    // true
    //     System.out.println("search(cat)   = " + trie.search("cat"));    // false
    //     System.out.println();

    //     // TEST 2: topKFrequentWordsWithPrefix("ap", 3)
    //     System.out.println("topKFrequentWordsWithPrefix(\"ap\", 3):");
    //     for (String s : trie.topKFrequentWordsWithPrefix("ap", 3)) {
    //         System.out.println("  " + s);
    //     }
    //     // Αναμενόμενο: apple, application, ape (με αυτή τη σειρά)
    //     System.out.println();

    //     // TEST 3: getAverageFrequencyOfPrefix("ap")
    //     double avgAp = trie.getAverageFrequencyOfPrefix("ap");
    //     System.out.println("getAverageFrequencyOfPrefix(\"ap\") = " + avgAp);
    //     // Αναμενόμενο: (50 + 30 + 5 + 10) / 4 = 23.75
    //     System.out.println();

    //     // TEST 4: predictNextLetter("ap")
    //     char next = trie.predictNextLetter("ap");
    //     System.out.println("predictNextLetter(\"ap\") = " + next);
    //     // Αναμενόμενο: 'p'

    //     // TEST 5: prefixes που δεν υπάρχουν
    //     System.out.println("avg(\"zzz\") = " + trie.getAverageFrequencyOfPrefix("zzz")); // 0.0
    //     System.out.println("predict(\"zzz\") = " + trie.predictNextLetter("zzz"));       // '\0' → κανένας
    // }
    

public class TestTrie {

    public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println("Usage: java hw.AutoCompleteApp <dictionaryFile> <textFile>");
            return;
        }

        String dictionaryFile = args[0];
        String textFile = args[1];

        CompressedTrie trie = new CompressedTrie();

        trie.loadDictionary(dictionaryFile);
        trie.updateImportanceFromText(textFile);

        System.out.println("Data loaded!");

        // εδώ βάζεις το διαδραστικό μενού
        // 1. topK
        // 2. average
        // 3. predict
        // 4. exit
    }


}