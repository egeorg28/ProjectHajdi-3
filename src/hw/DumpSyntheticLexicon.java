package hw;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class DumpSyntheticLexicon {

    public static void main(String[] args) {

        if (args.length < 4) {
            System.out.println("Usage: java -cp src hw.DumpSyntheticLexicon <templateDictFile> <n> <mode> <outputFile> [fixedLen]");
            System.out.println("  mode: fixed | var");
            System.out.println("Examples:");
            System.out.println("  java -cp src hw.DumpSyntheticLexicon /usr/share/dict/american-english 50000 fixed lex_fixed_50000_8.txt 8");
            System.out.println("  java -cp src hw.DumpSyntheticLexicon /usr/share/dict/american-english 50000 var   lex_var_50000.txt");
            return;
        }

        String templateFile = args[0];
        int n               = Integer.parseInt(args[1]);
        String mode         = args[2].toLowerCase();
        String outputFile   = args[3];

        int fixedLen = 8;
        if (mode.equals("fixed")) {
            if (args.length < 5) {
                System.out.println("For mode=fixed you must also give fixedLen (e.g. 8)");
                return;
            }
            fixedLen = Integer.parseInt(args[4]);
        }

        System.out.println("Template dictionary : " + templateFile);
        System.out.println("n = " + n + ", mode = " + mode +
                (mode.equals("fixed") ? (", fixedLen = " + fixedLen) : ""));
        System.out.println("Output file         : " + outputFile);
        System.out.println();

        // 1) Χτίζουμε μοντέλο από το πραγματικό λεξικό
        DictionaryModel model = LexiconStats.buildModelFromFile(templateFile);
        if (model == null) {
            System.out.println("Could not build DictionaryModel. Exiting.");
            return;
        }

        // 2) Deterministic generator (ΧΩΡΙΣ Random)
        DeterministicSyntheticDict gen = new DeterministicSyntheticDict(model);

        // 3) Παράγουμε λέξεις σε ΠΙΝΑΚΑ (όχι List)
        String[] words;
        if (mode.equals("fixed")) {
            words = gen.generateFixedLength(n, fixedLen);
        } else {
            words = gen.generateVariableLength(n);
        }

        System.out.println("Generated " + words.length + " words.");
        if (words.length > 0) {
            System.out.println("Example word: " + words[0]);
        }

        // 4) Γράφουμε σε txt
        try (PrintWriter out = new PrintWriter(new FileWriter(outputFile))) {
            for (int i = 0; i < words.length; i++) {
                out.println(words[i]);
            }
        } catch (IOException e) {
            System.out.println("Error writing to " + outputFile + ": " + e.getMessage());
            return;
        }

        System.out.println("✔ Synthetic lexicon written to: " + outputFile);
    }
}
