# Dictionary Prefix Engine


A Java implementation of dictionary and text-analysis operations using custom data structures, including a **Compressed Trie**, **Robin Hood Hash Table**, and **Min-Heap**.

The project focuses on efficient word storage, prefix-based searching, frequency analysis, and next-letter prediction without relying solely on Java's built-in data structures.

## Features

* Load and store a dictionary using a compressed trie
* Search for complete words efficiently
* Process a text corpus and record dictionary-word frequencies
* Find the top-k most frequent words matching a prefix
* Calculate the average frequency of words sharing a prefix
* Predict the next character of a given prefix
* Generate and analyze synthetic dictionaries
* Compare memory characteristics of trie-based structures

## Data Structures

### Compressed Trie

Words are stored using compressed string edges rather than one character per edge. Shared prefixes are represented only once, reducing unnecessary trie nodes.

Insertion handles:

* exact edge matches
* existing prefixes
* words that are prefixes of existing words
* partially overlapping prefixes

### Robin Hood Hashing

Each compressed-trie node stores its outgoing edges in a custom open-addressing hash table using **Robin Hood hashing**.

The implementation includes:

* collision resolution
* probe-distance comparison
* dynamic rehashing
* prime-sized hash tables

### Min-Heap

A custom min-heap is used to maintain the best `k` candidates during prefix-frequency queries.

This allows top-k results to be collected without storing and sorting every matching word.

### Standard Trie

The project also contains a traditional character-based Trie supporting:

* insertion
* search
* deletion
* alphabetical traversal

## Prefix Analysis

After loading a dictionary, the program can process a text corpus and assign an importance value to each dictionary word based on how frequently it occurs.

The resulting data can then be queried to:

1. Find the **top-k frequent words** beginning with a prefix.
2. Calculate the **average frequency** of words beginning with a prefix.
3. **Predict the next letter** following a prefix based on word-frequency information.

## Running the Program

Compile the Java source files:

```bash
javac -d out src/hw/*.java
```

Run the main application with a dictionary and text corpus:

```bash
java -cp out hw.MainApp <dictionary-file> <text-file>
```

For example:

```bash
java -cp out hw.MainApp data/dictionary.txt data/sample_text.txt
```

The program will load the dictionary, analyze the text corpus, and display an interactive menu for prefix queries.

## Technologies

* Java
* Tries and Compressed Tries
* Robin Hood Hashing
* Open Addressing
* Min-Heaps
* Recursive Tree Traversal
* File Processing
* Prefix Searching

## Project Purpose

This project was developed as a data structures and algorithms exercise focused on implementing and combining custom structures for efficient dictionary storage and text analysis.
