package hw;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * A command-line tool that:
 *  1) Reads a real dictionary file
 *  2) Builds a DictionaryModel from it
 *  3) Uses the DeterministicSyntheticDict generator to produce synthetic words
 *  4) Writes those words into a text file
 *
 * Execution:
 *   java -cp src hw.DumpSyntheticLexicon <templateDictFile> <n> <mode> <outputFile> [fixedLen]
 *
 * Where:
 *   <templateDictFile> : path to the real dictionary (e.g., /usr/share/dict/words)
 *   <n>                : how many synthetic words to generate
 *   <mode>             : "fixed"  or  "var"
 *   <outputFile>       : where to write the synthetic dictionary
 *   [fixedLen]         : required ONLY for mode="fixed"
 */
public class DumpSyntheticLexicon {

    public static void main(String[] args) {

        // --------------------------
        // 0) Validate command-line arguments
        // --------------------------
        if (args.length < 4) {
            System.out.println("Usage: java -cp src hw.DumpSyntheticLexicon <templateDictFile> <n> <mode> <outputFile> [fixedLen]");
            System.out.println("  mode: fixed | var");
            System.out.println("Examples:");
            System.out.println("  java -cp src hw.DumpSyntheticLexicon /usr/share/dict/american-english 50000 fixed lex_fixed_50000_8.txt 8");
            System.out.println("  java -cp src hw.DumpSyntheticLexicon /usr/share/dict/american-english 50000 var   lex_var_50000.txt");
            return;
        }

        // Required arguments
        String templateFile = args[0];       // Real dictionary path
        int n               = Integer.parseInt(args[1]); // Number of synthetic words
        String mode         = args[2].toLowerCase();     // "fixed" or "var"
        String outputFile   = args[3];       // Output file path

        // Optional argument for fixed-length mode
        int fixedLen = 8;  // Default value, used only if mode=fixed

        if (mode.equals("fixed")) {
            // If user requests mode=fixed, they MUST specify the fixed length
            if (args.length < 5) {
                System.out.println("For mode=fixed you must also give fixedLen (e.g. 8)");
                return;
            }
            fixedLen = Integer.parseInt(args[4]);
        }

        // Print configuration summary
        System.out.println("Template dictionary : " + templateFile);
        System.out.println("n = " + n + ", mode = " + mode +
                (mode.equals("fixed") ? (", fixedLen = " + fixedLen) : ""));
        System.out.println("Output file         : " + outputFile);
        System.out.println();

        // --------------------------
        // 1) Build DictionaryModel from the template dictionary
        // --------------------------
        // LexiconStats reads the real dictionary and computes:
        //  - letter frequencies
        //  - length frequencies
        //  - probabilities
        DictionaryModel model = LexiconStats.buildModelFromFile(templateFile);

        // If model could not be created (file missing, empty, etc.)
        if (model == null) {
            System.out.println("Could not build DictionaryModel. Exiting.");
            return;
        }

        // --------------------------
        // 2) Create the deterministic synthetic generator
        // --------------------------
        // NO randomness is used inside DeterministicSyntheticDict.
        DeterministicSyntheticDict gen = new DeterministicSyntheticDict(model);

        // --------------------------
        // 3) Generate synthetic words USING ARRAYS ONLY (not Lists)
        // --------------------------
        String[] words;

        if (mode.equals("fixed")) {
            // Create n words, each of *exact* length 'fixedLen'
            words = gen.generateFixedLength(n, fixedLen);
        } else {
            // Create n words with lengths chosen from lengthPool
            words = gen.generateVariableLength(n);
        }

        System.out.println("Generated " + words.length + " words.");

        // Print example word for visual confirmation
        if (words.length > 0) {
            System.out.println("Example word: " + words[0]);
        }

        // --------------------------
        // 4) Write output to txt file
        // --------------------------
        // We use PrintWriter + FileWriter inside a try-with-resources block.
        // This ensures the file is safely closed even if an exception happens.
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
