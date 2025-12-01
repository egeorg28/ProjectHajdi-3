package hw;

import java.util.Scanner;

/**
 * Main application for testing the CompressedTrie using:
 *  - a dictionary file (one word per line)
 *  - a text file (free text to update importance)
 *
 * Then it provides an interactive menu:
 *  1) Top-k frequent words with a prefix
 *  2) Average frequency of words with a prefix
 *  3) Predict next letter after a prefix
 *  0) Exit
 */
public class MainApp {

    public static void main(String[] args) {

        // ====== 1. Check command-line arguments ======
        if (args.length < 2) {
            System.out.println("Usage: java hw.MainApp <dictionaryFile> <textFile>");
            System.out.println("Example: java hw.MainApp dict.txt text.txt");
            return;
        }

        String dictFile = args[0]; // first argument = dictionary file
        String textFile = args[1]; // second argument = text file (corpus)

        // ====== 2. Create the compressed trie ======
        CompressedTrie trie = new CompressedTrie();

        // ====== 3. Load dictionary ======
        System.out.println("=== LOADING DICTIONARY FROM FILE: " + dictFile + " ===");
        boolean okDict = trie.loadDictionary(dictFile);
        if (!okDict) {
            System.out.println("Failed to load dictionary. Exiting.");
            return;
        }
        System.out.println("Dictionary loaded successfully.");

        // ====== 4. Update importance from text file ======
        System.out.println("=== UPDATING IMPORTANCE FROM TEXT FILE: " + textFile + " ===");
        trie.updateImportanceFromText(textFile);
        System.out.println("Finished updating importance from text.");

        // ====== 5. Interactive menu ======
        Scanner in = new Scanner(System.in);

        while (true) {
            // Print menu options
            System.out.println();
            System.out.println("========== MENU ==========");
            System.out.println("1. Top-k frequent words with prefix");
            System.out.println("2. Average frequency of words with prefix");
            System.out.println("3. Predict next letter after prefix");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");

            // Read user choice (integer)
            int choice;
            if (in.hasNextInt()) {
                choice = in.nextInt();
            } else {
                // If user types something that is not an int, consume it and continue
                String junk = in.next();
                System.out.println("Invalid input: " + junk);
                continue;
            }

            // Consume end of line
            in.nextLine();

            if (choice == 0) {
                System.out.println("Exiting program. Goodbye!");
                break;
            }

            switch (choice) {
                case 1:
                    // 1. Top-k frequent words with prefix
                    System.out.print("Enter prefix: ");
                    String prefix1 = in.nextLine().trim().toLowerCase();

                    System.out.print("Enter k (max number of words): ");
                    int k;
                    if (in.hasNextInt()) {
                        k = in.nextInt();
                    } else {
                        String junk = in.next();
                        System.out.println("Invalid integer for k: " + junk);
                        break;
                    }
                    in.nextLine(); // consume newline

                    trie.topKFrequentWordsWithPrefix(prefix1, k);
                    break;

                case 2:
                    // 2. Average frequency of words with prefix
                    System.out.print("Enter prefix: ");
                    String prefix2 = in.nextLine().trim().toLowerCase();

                    double avg = trie.getAverageFrequencyOfPrefix(prefix2);
                    System.out.println("Average frequency for prefix \"" + prefix2 + "\" = " + avg);
                    break;

                case 3:
                    // 3. Predict next letter after prefix
                    System.out.print("Enter prefix: ");
                    String prefix3 = in.nextLine().trim().toLowerCase();

                    char next = trie.predictNextLetter(prefix3);
                    if (next != '\0') {
                        System.out.println("Predicted next letter: '" + next + "'");
                    } else {
                        System.out.println("No prediction available for prefix \"" + prefix3 + "\".");
                    }
                    break;

                default:
                    System.out.println("Unknown option. Please choose 0, 1, 2, or 3.");
                    break;
            }
        }

        in.close();
    }
}
