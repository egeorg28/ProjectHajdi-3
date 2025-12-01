package hw;

/**
 * A simple array-based Min-Heap that stores (word, importance) pairs.
 *
 * The "smallest" element is considered the WORST among the current top-K: -
 * lower importance is worse - if importance ties, lexicographically LARGER word
 * is worse
 *
 * We use this so that the root of the heap is always the least desirable, and
 * we can discard it when inserting a better candidate.
 */
public class MyMinHeap {

    // Entry class: word + importance
    public static class HeapEntry {

        public String word;
        public int importance;

        public HeapEntry(String word, int importance) {
            this.word = word;
            this.importance = importance;
        }
    }

    private HeapEntry[] heap;
    private int size;

    public MyMinHeap(int capacity) {
        this.heap = new HeapEntry[capacity];
        this.size = 0;
    }

    public int getSize() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Compare two entries: returns <0 if a is "worse" (smaller) than b,
     * >0 if a is "better" (greater) than b, 0 if they are equal.
     *
     * Worse = lower importance, or same importance but lexicographically larger
     * word.
     */
    private int compare(HeapEntry a, HeapEntry b) {
        if (a.importance != b.importance) {
            return a.importance - b.importance; // smaller importance = worse
        }
        // For ties, a word that is lexicographically LATER is considered worse
        return -a.word.compareTo(b.word);
    }

    // Standard heap insert without capacity check
    private void insert(HeapEntry entry) {
        heap[size] = entry;
        siftUp(size);
        size++;
    }

    /**
     * Inserts an element but respects maximum size K. If heap is not full →
     * just insert. If heap is full: - if entry is better than root (the worst
     * in heap) → replace root. - otherwise ignore entry.
     */
    public void insertWithCapacity(String word, int importance, int k) {
        HeapEntry entry = new HeapEntry(word, importance);

        if (size < k) {
            insert(entry);
            return;
        }

        if (size == 0) {
            insert(entry);
            return;
        }

        HeapEntry root = heap[0];

        // If new entry is better than the root (worst), replace root
        if (compare(entry, root) > 0) {
            heap[0] = entry;
            siftDown(0);
        }
        // else: new entry is worse or equal, ignore it
    }

    // Heapify upwards
    private void siftUp(int idx) {
        int parent;
        while (idx > 0) {
            parent = (idx - 1) / 2;
            if (compare(heap[idx], heap[parent]) < 0) {
                // heap[idx] is "worse" (smaller) → it should go up?
                // Remember: we maintain min-heap based on "worse" = smaller.
                HeapEntry tmp = heap[idx];
                heap[idx] = heap[parent];
                heap[parent] = tmp;
                idx = parent;
            } else {
                break;
            }
        }
    }

    // Heapify downwards
    private void siftDown(int idx) {
        while (true) {
            int left = 2 * idx + 1;
            int right = 2 * idx + 2;
            int smallest = idx;

            if (left < size && compare(heap[left], heap[smallest]) < 0) {
                smallest = left;
            }
            if (right < size && compare(heap[right], heap[smallest]) < 0) {
                smallest = right;
            }

            if (smallest != idx) {
                HeapEntry tmp = heap[idx];
                heap[idx] = heap[smallest];
                heap[smallest] = tmp;
                idx = smallest;
            } else {
                break;
            }
        }
    }

    /**
     * Returns an array copy of the current heap entries (size elements), in
     * arbitrary heap order. We will sort them outside when needed.
     */
    public HeapEntry[] toArray() {
        HeapEntry[] arr = new HeapEntry[size];
        for (int i = 0; i < size; i++) {
            arr[i] = heap[i];
        }
        return arr;
    }
}
