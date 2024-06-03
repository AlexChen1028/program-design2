package hw4;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

public class TFIDFCalculator {
    List<Trie> documents;

    TFIDFCalculator() {
        documents = new ArrayList<>();
    }

    void addDocument(List<String> lines) {
        Trie trie = new Trie();
        for (String line : lines) {
            String[] words = line.replaceAll("[^a-zA-Z]", " ").toLowerCase().split("\\s+");
            for (String word : words) {
                if (!word.isEmpty()) {
                    trie.insert(word);
                }
            }
        }
        documents.add(trie);
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis(); // 獲取程式開始時間
        TFIDFCalculator calculator = new TFIDFCalculator();
        String docPath = args[0];
        String testCase = args[1];
        //System.out.print(testCase + " ");

        if (args.length < 2) {
            System.out.println("Usage: java TFIDFCalculator <doc_path> <test_case>");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(docPath))) {

            List<String> lines = new ArrayList<>();
            String line;

            // 讀取文件並每五行分為一個文本存入documents
            while ((line = reader.readLine()) != null) {
                lines.add(line);
                if (lines.size() == 5) {
                    calculator.addDocument(lines);
                    lines.clear();
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading input file: " + e.getMessage());
        }

        // 讀取測試案例
        try (BufferedReader testCaseReader = new BufferedReader(new FileReader(testCase));
            FileWriter writer = new FileWriter("output.txt")) {
            String[] terms = testCaseReader.readLine().split("\\s+");
            String[] indices = testCaseReader.readLine().split("\\s+");

            for (int i = 0; i < terms.length; i++) {
                String term = terms[i];
                int docIndex = Integer.parseInt(indices[i]);
                Trie document = calculator.documents.get(docIndex);
                double tfidf = TFIDF.tfidf(term, document, calculator.documents);
                double tf = TFIDF.tf(term, document);
                double idf = TFIDF.idf(term, calculator.documents);
                //System.out.print(tf + " " + idf + " ");
                writer.write(String.format("%.5f", tfidf) + " ");
                //System.out.print(tfidf + " ");  // 也輸出到控制台
            }
        } catch (IOException e) {
            System.err.println("Error reading test case file or writing output file: " + e.getMessage());
        }

        long endTime = System.currentTimeMillis(); // 獲取程式結束時間
        long runtime = endTime - startTime; // 計算運行時間（毫秒）
        System.out.println("runtime: " + runtime + " ms");
    }
}

// 定義Trie節點類
class TrieNode {
    TrieNode[] children;
    int frequency;

    TrieNode() {
        children = new TrieNode[26];
        frequency = 0;
    }
}

class Trie {
    TrieNode root;
    List<String> words;
    Hashtable<String, Integer> wordFrequency;

    Trie() {
        root = new TrieNode();
        words = new ArrayList<>();
        wordFrequency = new Hashtable<>();
    }

    //插入單詞
    void insert(String word) {
        TrieNode node = root;
        for (char ch : word.toCharArray()) {
            int index = ch - 'a';
            if (node.children[index] == null) {
                node.children[index] = new TrieNode();
            }
            node = node.children[index];
        }
        node.frequency++;
        wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);

        if (!words.contains(word)) {
            words.add(word);
        }
    }

    // 遞迴輸出 Trie 中所有的詞彙
    void printAllWords(TrieNode node, String prefix) {
        if (node == null) {
            return;
        }

        if (node.frequency > 0) {
            //System.out.print(prefix + " ");
        }

        for (int i = 0; i < 26; i++) {
            if (node.children[i] != null) {
                char ch = (char) ('a' + i);
                printAllWords(node.children[i], prefix + ch);
            }
        }
    }

    void printAllWords() {
        printAllWords(root, "");
    }

    int getFrequency(String word) {
        return wordFrequency.getOrDefault(word, 0);
    }

    List<String> getWords() {
        return words;
    }
}

class TFIDF {
    public static double tf(String term, Trie document) {
        int termFrequency = document.getFrequency(term);
        //System.out.print(termFrequency + " ");
        int totalWords = document.getWords().stream().mapToInt(document::getFrequency).sum();
        //System.out.print(totalWords + " ");
        document.printAllWords();
        return totalWords == 0 ? 0 : (double) termFrequency / totalWords;
    }

    private static Hashtable<String, Double> documentExist = new Hashtable<String, Double>(); // 儲存己算過的idf


    public static double idf(String term, List<Trie> documents) {
        int docCount = documents.size();
        Double exist = documentExist.get(term);
        //System.out.print(docCount + " ");
        int termInDocCount = 0;
        if(exist == null) {
            for (Trie document : documents) {
                if (document.getFrequency(term) > 0) {
                    termInDocCount++;
                }
            }

            exist = Math.log((double) docCount / termInDocCount);
            documentExist.put(term, exist);
        }
        //System.out.print(termInDocCount + " ");
        return exist;
    }
    
    public static double tfidf(String term, Trie document, List<Trie> documents) {
        return tf(term, document) * idf(term, documents);
    }
}