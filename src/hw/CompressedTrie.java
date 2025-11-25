package hw;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompressedTrie {

    CompressedTrieNode root;

    public CompressedTrie() {
        this.root = new CompressedTrieNode();
    }

    // =====================================
    // INNER CLASS: WordFrequency
    // =====================================
    private static class WordFrequency {
        String word;
        int importance;

        WordFrequency(String word, int importance) {
            this.word = word;
            this.importance = importance;
        }
    }

    // =====================================
    // INNER CLASS: MinHeap (capacity = k)
    // =====================================
    private static class MinHeap {

        private WordFrequency[] heap;
        private int size;

        public MinHeap(int capacity) {
            heap = new WordFrequency[capacity];
            size = 0;
        }

        public int size() { return size; }

        public boolean isFull() { return size == heap.length; }

        public WordFrequency peekMin() {
            if (size == 0) return null;
            return heap[0];
        }

        private void swap(int i, int j) {
            WordFrequency tmp = heap[i];
            heap[i] = heap[j];
            heap[j] = tmp;
        }

        private int compare(WordFrequency a, WordFrequency b) {
            if (a.importance != b.importance)
                return a.importance - b.importance;
            return a.word.compareTo(b.word);
        }

        public void insert(WordFrequency item) {
            if (size == heap.length) return;

            heap[size] = item;
            int i = size;
            size++;

            while (i > 0) {
                int parent = (i - 1) / 2;
                if (compare(heap[i], heap[parent]) < 0) {
                    swap(i, parent);
                    i = parent;
                } else break;
            }
        }

        public WordFrequency extractMin() {
            if (size == 0) return null;

            WordFrequency min = heap[0];
            heap[0] = heap[size - 1];
            size--;

            int i = 0;
            while (true) {
                int left = 2*i + 1;
                int right = 2*i + 2;
                int smallest = i;

                if (left < size && compare(heap[left], heap[smallest]) < 0)
                    smallest = left;
                if (right < size && compare(heap[right], heap[smallest]) < 0)
                    smallest = right;

                if (smallest == i) break;

                swap(i, smallest);
                i = smallest;
            }
            return min;
        }
    }

    // =====================================
    // INSERT
    // =====================================
    public void insert(String word) {
        if (word == null || word.isEmpty()) return;
        word = word.toLowerCase();
        insertRecursive(root, word);
    }

    private void insertRecursive(CompressedTrieNode node, String word) {

        if (word.length() == 0) {
            node.isEndOfWord = true;
            return;
        }

        char first = word.charAt(0);
        Edge edge = node.getEdgeByFirstChar(first);

        if (edge == null) {
            CompressedTrieNode child = new CompressedTrieNode();
            child.isEndOfWord = true;
            node.insertEdge(new Edge(word, child));
            return;
        }

        String label = edge.label;
        String common = commonPrefix(label, word);

        if (common.length() == 0) {
            CompressedTrieNode child = new CompressedTrieNode();
            child.isEndOfWord = true;
            node.insertEdge(new Edge(word, child));
            return;
        }

        if (common.length() == label.length() && common.length() == word.length()) {
            edge.child.isEndOfWord = true;
            return;
        }

        if (common.length() == label.length() && common.length() < word.length()) {
            String rest = word.substring(common.length());
            insertRecursive(edge.child, rest);
            return;
        }

        if (common.length() == word.length() && common.length() < label.length()) {
            String remainingLabel = label.substring(common.length());

            CompressedTrieNode oldChild = edge.child;
            CompressedTrieNode middle = new CompressedTrieNode();
            middle.isEndOfWord = true;

            edge.label = common;
            edge.child = middle;

            middle.insertEdge(new Edge(remainingLabel, oldChild));
            return;
        }

        if (common.length() < label.length() && common.length() < word.length()) {

            String edgeSuffix = label.substring(common.length());
            String wordSuffix = word.substring(common.length());

            CompressedTrieNode oldChild = edge.child;
            CompressedTrieNode middle = new CompressedTrieNode();

            edge.label = common;
            edge.child = middle;

            middle.insertEdge(new Edge(edgeSuffix, oldChild));

            CompressedTrieNode newChild = new CompressedTrieNode();
            newChild.isEndOfWord = true;

            middle.insertEdge(new Edge(wordSuffix, newChild));
        }
    }

    // =====================================
    // SEARCH
    // =====================================
    public boolean search(String word) {
        if (word == null || word.isEmpty()) return false;
        word = word.toLowerCase();
        return searchRecursive(root, word);
    }

    private boolean searchRecursive(CompressedTrieNode node, String word) {
        if (word.length() == 0) return node.isEndOfWord;

        char first = word.charAt(0);
        Edge edge = node.getEdgeByFirstChar(first);
        if (edge == null) return false;

        String label = edge.label;

        if (word.startsWith(label))
            return searchRecursive(edge.child, word.substring(label.length()));

        return false;
    }

    // =====================================
    // INCREASE IMPORTANCE
    // =====================================
    public void increaseImportance(String word) {
        word = word.toLowerCase();
        increaseImportanceRecursive(root, word);
    }

    private void increaseImportanceRecursive(CompressedTrieNode node, String word) {

        if (word.length() == 0) {
            if (node.isEndOfWord) node.importance++;
            return;
        }

        char first = word.charAt(0);
        Edge edge = node.getEdgeByFirstChar(first);
        if (edge == null) return;

        String label = edge.label;

        if (word.startsWith(label)) {
            increaseImportanceRecursive(edge.child, word.substring(label.length()));
        }
    }

    // =====================================
    // FIND NODE FOR PREFIX (used by all menu functions)
    // =====================================
    private CompressedTrieNode findNodeForPrefix(String prefix) {
        CompressedTrieNode node = root;
        String w = prefix;

        while (w.length() > 0) {
            char first = w.charAt(0);
            Edge edge = node.getEdgeByFirstChar(first);
            if (edge == null) return null;

            String label = edge.label;

            if (w.startsWith(label)) {
                w = w.substring(label.length());
                node = edge.child;
            } else if (label.startsWith(w)) {
                return edge.child;
            } else {
                return null;
            }
        }
        return node;
    }

    // =====================================
    // TOP-K FREQUENCY WITH PREFIX
    // =====================================
    public List<String> topKFrequentWordsWithPrefix(String prefix, int k) {

        List<String> result = new ArrayList<>();
        if (prefix == null || prefix.isEmpty() || k <= 0) return result;

        prefix = prefix.toLowerCase();

        CompressedTrieNode node = findNodeForPrefix(prefix);
        if (node == null) return result;

        MinHeap heap = new MinHeap(k);

        collectTopK(node, prefix, heap, k);

        List<WordFrequency> temp = new ArrayList<>();
        while (heap.size() > 0) {
            temp.add(heap.extractMin());
        }

        Collections.sort(temp, (a, b) -> {
            if (b.importance != a.importance)
                return b.importance - a.importance;
            return a.word.compareTo(b.word);
        });

        for (WordFrequency wf : temp)
            result.add(wf.word);

        return result;
    }

    private void collectTopK(CompressedTrieNode node, String prefix, MinHeap heap, int k) {

        if (node.isEndOfWord) {
            WordFrequency wf = new WordFrequency(prefix, node.importance);

            if (!heap.isFull()) heap.insert(wf);
            else if (heap.peekMin().importance < wf.importance) {
                heap.extractMin();
                heap.insert(wf);
            }
        }

        for (Edge e : node.getAllEdges())
            collectTopK(e.child, prefix + e.label, heap, k);
    }

    // =====================================
    // AVG FREQUENCY OF PREFIX
    // =====================================
    public double getAverageFrequencyOfPrefix(String prefix) {

        if (prefix == null || prefix.isEmpty()) return 0.0;
        prefix = prefix.toLowerCase();

        CompressedTrieNode node = findNodeForPrefix(prefix);
        if (node == null) return 0.0;

        int[] sumCount = new int[2]; // sum = [0], count = [1]

        collectSumAndCount(node, prefix, sumCount);

        if (sumCount[1] == 0) return 0.0;

        return (double) sumCount[0] / sumCount[1];
    }

    private void collectSumAndCount(CompressedTrieNode node, String word, int[] sumCount) {

        if (node.isEndOfWord) {
            sumCount[0] += node.importance;
            sumCount[1] += 1;
        }

        for (Edge e : node.getAllEdges())
            collectSumAndCount(e.child, word + e.label, sumCount);
    }

    // =====================================
    // PREDICT NEXT LETTER
    // =====================================
    public char predictNextLetter(String prefix) {
        if (prefix == null || prefix.isEmpty())
            return '\0';

        prefix = prefix.toLowerCase();

        CompressedTrieNode node = findNodeForPrefix(prefix);
        if (node == null) return '\0';

        List<Edge> children = node.getAllEdges();
        if (children == null || children.isEmpty()) return '\0';

        char best = '\0';
        double bestAvg = -1.0;

        for (Edge e : children) {
            char nextChar = e.label.charAt(0);

            double avg = getAverageFrequencyOfPrefix(prefix + nextChar);

            if (avg > bestAvg ||
                (avg == bestAvg && best != '\0' && nextChar < best)) {

                bestAvg = avg;
                best = nextChar;
            }
        }

        return best;
    }

    // =====================================
    // LOAD DICTIONARY
    // =====================================
    public void loadDictionary(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

            String line;
            while ((line = br.readLine()) != null) {

                String word = line.trim().toLowerCase();
                word = word.replaceAll("[^a-z]", "");

                if (!word.isEmpty())
                    insert(word);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // =====================================
    // UPDATE IMPORTANCE FROM TEXT
    // =====================================
    public void updateImportanceFromText(String filename) {

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

            String line;
            while ((line = br.readLine()) != null) {

                String[] tokens = line.split("\\W+");

                for (String token : tokens) {

                    String word = token.toLowerCase().replaceAll("[^a-z]", "");

                    if (word.isEmpty()) continue;

                    if (search(word))
                        increaseImportance(word);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // COMMON PREFIX
    private String commonPrefix(String a, String b) {
        int len = Math.min(a.length(), b.length());
        int i = 0;
        while (i < len && a.charAt(i) == b.charAt(i))
            i++;
        return a.substring(0, i);
    }
}
