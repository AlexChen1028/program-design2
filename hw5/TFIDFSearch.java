package hw5;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.lang.Math;


public class TFIDFSearch {
    private Map<Integer, List<String>> documents;
    private Map<String, Map<Integer, Double>> tfidfValues;

    public TFIDFSearch(String indexFilePath) {
        this.documents = new HashMap<>();
        this.tfidfValues = new HashMap<>();
        loadDocuments(indexFilePath);
    }

    // 從文件中載入文檔
    private void loadDocuments(String indexFilePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(indexFilePath))) {
            Map<Integer, List<String>> serializedData = (Map<Integer, List<String>>) ois.readObject();
            documents.putAll(serializedData);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // 計算 TF
    private double computeTF(String term, List<String> document) {
        int termFrequency = 0;
        Map<String, Integer> wordFrequency = new HashMap<>();

        for (String word : document) {
            word = word.replaceAll("[^a-zA-Z]", " ").toLowerCase().trim();
            wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
        }

        for (Map.Entry<String, Integer> entry : wordFrequency.entrySet()) {
            //System.out.println("Word: " + entry.getKey() + ", Frequency: " + entry.getValue());
            termFrequency = entry.getValue();
        }
        //System.out.print(termFrequency + " ");
        return (double) termFrequency / document.size();
    }

    // 計算 IDF
    private double computeIDF(String term, int totalDocuments) {
        //System.out.print(term + " ");
        int df = 0;
        for (List<String> document : documents.values()) {
            if(document.contains(term)) {
                df++;
            }
        }
        //System.out.print(df + " ");
        return Math.log((double) totalDocuments / df );
    }

    // 計算 TF-IDF
    private void computeTFIDF(String term) {
        int documentId = 0;
        int count = 0;
        for (Map.Entry<Integer, List<String>> entry : documents.entrySet()) {
            Integer key = entry.getKey();
            List<String> value = entry.getValue();
            // if(value.contains(term)) {
            for(String word : value) {
                String dealt = word.replaceAll("[^a-zA-Z]", " ").toLowerCase();
                //System.out.print(dealt + " ");
                if(dealt.contains(term)) {
                    double tf = computeTF(term, value);
                    //System.out.print(tf + " ");

                    double idf = computeIDF(term, documents.size());
                    double tfidf = tf * idf;

                    // 儲存 TF-IDF 值
                    if (tfidfValues.containsKey(term)) {
                        tfidfValues.get(term).put(documentId - 1, tfidf);
                    }
                    tfidfValues.put(term, new HashMap<>());
                    tfidfValues.get(term).put(documentId - 1, tfidf);
                    //System.out.print(tfidf + " ");
                    count++;
                }
            }
            //System.out.print(count + " ");
            //}
            documentId++;
        }
    }

    // 處理查詢並返回文檔ID列表
    public List<Integer> search(String query, int n) {
        Map<Integer, Double> docScores = new HashMap<>();
        Set<Integer> resultSet = new HashSet<>();

        if(query.contains("OR")) {
            String[] terms = query.replace("OR", "").split("\\s+");
            // 计算查询中每个单词的 TF-IDF 并累加到文档得分中
            for (String term : terms) {
                term = term.trim();
                if (tfidfValues.containsKey(term)) {
                    //Map<Integer, Double> termTfidf = tfidfValues.get(term);
                    computeTFIDF(term);
                    // for (Map.Entry<Integer, Double> entry : termTfidf.entrySet()) {
                    //     int docId = entry.getKey();
                    //     double tfidf = entry.getValue();
                    //     docScores.merge(docId, tfidf, Double::sum);
                    // }
                }
                computeTFIDF(term);
                Map<Integer, Double> termTfidf = tfidfValues.get(term);
                for (Map.Entry<Integer, Double> entry : termTfidf.entrySet()) {
                    int docId = entry.getKey();
                    double tfidf = entry.getValue();
                        //System.out.print(tfidf + " ");
                    docScores.merge(docId, tfidf, Double::sum);
                }
            }
        } else if(query.contains("AND")) {
            String[] terms = query.replace("AND", "").split("\\s+");
            boolean firstTerm = true;
            for (String term : terms) {
                term = term.trim();
                if (!tfidfValues.containsKey(term)) {
                    computeTFIDF(term);
                }
                Map<Integer, Double> termTfidf = tfidfValues.get(term);
                Set<Integer> currentSet = new HashSet<>(termTfidf.keySet());
                if (firstTerm) {
                    resultSet.addAll(currentSet);
                    firstTerm = false;
                } else {
                    resultSet.retainAll(currentSet);
                }
            }
            for (Integer docId : resultSet) {
                double totalTfidf = 0.0;
                for (String term : terms) {
                    term = term.trim();
                    totalTfidf += tfidfValues.get(term).getOrDefault(docId, 0.0);
                }
                docScores.put(docId, totalTfidf);
            }
        } else {
            computeTFIDF(query);
            //System.out.print(query);
            Map<Integer, Double> termTfidf = tfidfValues.get(query);
            
            for (Map.Entry<Integer, Double> entry : termTfidf.entrySet()) {
                int docId = entry.getKey();
                double tfidf = entry.getValue();
                //System.out.print(docId + " ");
                docScores.merge(docId, tfidf, Double::sum);
            }
        }

        // 將文檔依分數高低排序
        List<Map.Entry<Integer, Double>> rankedDocs = new ArrayList<>(docScores.entrySet());
        rankedDocs.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));

        // 取前 n 个文檔 ID
        List<Integer> results = new ArrayList<>();
        for (int i = 0; i < Math.min(n, rankedDocs.size()); i++) {
            results.add(rankedDocs.get(i).getKey());
        }
        while (results.size() < n) {
            results.add(-1); // 不足 n 个文檔時用 -1 補齊
        }
        return results;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("使用方法: java TFIDFSearch <索引文件> <查詢文件>");
            return;
        }

        String indexFilePath = args[0] + ".ser";
        String queryFilePath = args[1];

        TFIDFSearch searcher = new TFIDFSearch(indexFilePath);

        try (BufferedReader br = new BufferedReader(new FileReader(queryFilePath));
            BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"))) {
            int n = Integer.parseInt(br.readLine().trim());
            String query;
            int count = 0;
            while ((query = br.readLine()) != null) {
                List<Integer> result = searcher.search(query, n);
                for(int docId : result) {
                    writer.write(docId + " ");
                }
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}