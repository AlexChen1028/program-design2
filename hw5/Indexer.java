package hw5;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Indexer implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // 方法：序列化文檔
    public void saveDocuments(Map<Integer, List<String>> documents, String filePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(documents);
            System.out.println("文檔已序列化至 " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
