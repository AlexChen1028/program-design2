package hw5;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.lang.ClassNotFoundException;

public class BuildIndex {
    public static void main(String[] args) {
        String file = args[0];

        // 創建 Indexer 實例
        Indexer idx = new Indexer();

        // 序列化 Indexer 實例
        idx.serialize(file);

        // 測試反序列化
        try {
            FileInputStream fis = new FileInputStream(file + ".ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            Indexer deserializedIdx = (Indexer) ois.readObject();
            ois.close();
            fis.close();

            System.out.println("反序列化成功：" + deserializedIdx);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException c) {
            c.printStackTrace();
        }
    }
}