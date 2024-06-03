package hw5;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.lang.ClassNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;

public class BuildIndex {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("使用方法: java BuildIndex <檔案名稱>");
            return;
        }

        String corpusFilePath = args[0];
        try {
            List<String> sentences = readCorpusFile(corpusFilePath);
            List<List<String>> documents = splitIntoDocuments(sentences);
            for (int i = 0; i < documents.size(); i++) {
                Indexer indexer = new Indexer();
                List<String> document = documents.get(i);
                processDocument(document, indexer, i);
                String serializedFileName = "corpus" + i + ".ser";
                indexer.serialize(serializedFileName);
                System.out.println("索引已序列化至 " + serializedFileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 讀取語料庫文件並返回句子列表
    private static List<String> readCorpusFile(String filePath) throws IOException {
        List<String> sentences = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                sentences.add(line);
            }
        }
        return sentences;
    }

    // 將句子分成每5句一個的文件
    private static List<List<String>> splitIntoDocuments(List<String> sentences) {
        List<List<String>> documents = new ArrayList<>();
        for (int i = 0; i < sentences.size(); i += 5) {
            documents.add(sentences.subList(i, Math.min(i + 5, sentences.size())));
        }
        return documents;
    }

    // 處理每個文件並將單字加入索引器
    private static void processDocument(List<String> document, Indexer indexer, int docID) {
        for (String sentence : document) {
            String processedSentence = processSentence(sentence);
            String[] words = processedSentence.split("\\s+");
            for (String word : words) {
                if (!word.isEmpty()) {
                    indexer.addWord(word, docID);
                }
            }
        }
    }

    // 根據解析規則處理每個句子
    private static String processSentence(String sentence) {
        sentence = sentence.replaceAll("[^a-zA-Z]+", " "); // 步驟1
        sentence = sentence.toLowerCase(); // 步驟2
        return sentence.trim();
    }
}