package hw5;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class Indexer implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private int counter;

    public Indexer() {
        this.name = "DefaultIndexer";
        this.counter = 0;
    }

    // 假設添加一些索引的功能
    public void addWord(String word, int documentId) {
        // 這裡添加索引的邏輯
    }

    // 添加序列化方法
    public void serialize(String fileName) {
        try {
            FileOutputStream fos = new FileOutputStream(fileName + ".ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}