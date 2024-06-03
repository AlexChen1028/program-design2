package hw3;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.text.DecimalFormat;

public class HtmlParser {
    public static void main(String[] args) {
        int mode = Integer.parseInt(args[0]);
        // 定義網站URL和CSV檔案路徑
        String url = "https://pd2-hw3.netdb.csie.ncku.edu.tw/";
        String csvFilePath = "output.csv";
        if(mode == 0){
            try {
                // 取得日期
                Document doc = Jsoup.connect("https://pd2-hw3.netdb.csie.ncku.edu.tw/").get();
                String title = doc.title();
                // 切割獲取日期部分
                String[] parts = title.split("y");
                int day = Integer.parseInt(parts[1]);
                //System.out.println(day);

                // 使用jsoup連接到網站並獲取HTML內容
                Document document = Jsoup.connect(url).get();
                // 找到包含股票資料的元素
                Elements thElements = document.getElementsByTag("th");
                // 找到包含股票價格的元素
                Elements tdElements = document.getElementsByTag("td");

                writerToData(thElements,tdElements);
                CSVReaderWriter("data.csv", "output.csv");
            } catch (IOException e) {
                System.err.println("爬取股票資料時發生錯誤：" + e.getMessage());
            }
        }else if(mode == 1){
            try {
                int task = Integer.parseInt(args[1]);
                // 取得日期
                Document doc = Jsoup.connect("https://pd2-hw3.netdb.csie.ncku.edu.tw/").get();
                String title = doc.title();
                // 切割獲取日期部分
                String[] parts = title.split("y");
                int day = Integer.parseInt(parts[1]);
                //System.out.println(day);

                // 使用jsoup連接到網站並獲取HTML內容
                Document document = Jsoup.connect(url).get();
                // 找到包含股票資料的元素
                Elements thElements = document.getElementsByTag("th");
                // 找到包含股票價格的元素
                Elements tdElements = document.getElementsByTag("td");

                //writerToData(thElements,tdElements);
                if(task == 0){
                    writerToData(thElements, tdElements);
                    CSVReaderWriter("data.csv", "output.csv");
                }else if(task == 1 && args.length >= 5){
                    int start = Integer.parseInt(args[3]);
                    int end = Integer.parseInt(args[4]);
                    String stock = args[2];
                    List<Double> stockPrice;
                    stockPrice = readStockPrice("data.csv", stock, start, end);
                    List<Double> movingAverage;
                    movingAverage = calculateMovingAverage(stockPrice, 5);
                    writeToOutputTask(stock, start, end, movingAverage);
                }else if(task == 2 && args.length >= 5){
                    int start = Integer.parseInt(args[3]);
                    int end = Integer.parseInt(args[4]);
                    String stock = args[2];
                    List<Double> stockPrice;
                    stockPrice = readStockPrice("data.csv", stock, start, end);
                    List<Double> movingAverage = calculateMovingAverage(stockPrice, end - start + 1);
                    List<Double> stdDev = calculateStdDev(stockPrice, movingAverage);
                    writeToOutputTask(stock, start, end, stdDev);
                }else if(task == 3 && args.length >= 5){
                    int start = Integer.parseInt(args[3]);
                    int end = Integer.parseInt(args[4]);
                    List<String> thString = new ArrayList<>();
                    for(Element th : thElements){
                        thString.add(th.text());
                    }
                    List<Double> stdDevsList = new ArrayList<>();
                    for(int i = 0;i < thString.size(); i++){
                        String stock = thString.get(i);
                        List<Double> stockPrice;
                        stockPrice = readStockPrice("data.csv", stock, start, end);
                        List<Double> movingAverage = calculateMovingAverage(stockPrice, end - start + 1);
                        List<Double> stdDevs = calculateStdDev(stockPrice, movingAverage);
                        stdDevsList.addAll(stdDevs);
                    }
                    // 取得前三大的標準差索引
                    List<Integer> topThreeIndices = getTopThreeIndices(stdDevsList);
                    // 取得前三大標準差對應的股票名稱
                    List<String> topThreeStocks = getTopThreeStocks(thElements, topThreeIndices);
                    writeToTask3("output.csv", topThreeStocks, topThreeIndices, stdDevsList, start, end);
                }else if (task == 4 && args.length >= 5){
                    String stock = args[2];
                    int start = Integer.parseInt(args[3]);
                    int end = Integer.parseInt(args[4]);
                    
                }
            } catch (IOException e) {
                System.err.println("爬取股票資料時發生錯誤：" + e.getMessage());
            }
        }
    }
    public static void writerToData(Elements thElements, Elements tdElements) {
        // 打開CSV檔案並將股票資料寫入
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("data.csv", true))) {
            File file = new File("data.csv");
            int line = 0;
            BufferedReader reader = new BufferedReader(new FileReader("data.csv"));
            while (reader.readLine() != null){
                line++;
            }
            // 檢查檔案是否存在並為空
            if (line == 0) {
                // 寫入標頭行
                int thCount = thElements.size();
                int j = 0;
                for (Element th : thElements) {
                    writer.append(th.text());
                    if (j < thCount - 1) {
                        writer.append(",");
                    }
                    j++;
                }
                writer.newLine();  // 換行
                int tdCount = tdElements.size();
                int i = 0;
                for(Element td : tdElements){
                    writer.append(td.text());
                    if (i < tdCount - 1) {
                        writer.append(",");
                    }
                    i++;
                }
                writer.newLine();  // 換行
            }
            if (file.length() != 0 && line <= 30) {
                // 寫入當天的資料
                System.out.println("1");
                int tdCount = tdElements.size();
                int i = 0;
                for(Element td : tdElements){
                    writer.append(td.text());
                    if (i < tdCount - 1) {
                        writer.append(",");
                    }
                    i++;
                }
                if(line < 30){
                    writer.newLine();  // 換行
                }
            }
        } catch (IOException e) {
            System.err.println("寫入檔案時發生錯誤：" + e.getMessage());
        }
    }
    // 讀取股價資料
    public static List<Double> readStockPrice(String filename, String stock, int start, int end) {
        List<Double> stockPrice = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;// 讀取 CSV 檔案的標頭行（如果有的話）
            String headerLine = br.readLine();
            String[] headers = headerLine.split(",");

            // 打印標頭資訊
            for (String header : headers) {
                if(header.contains(stock)){
                    //System.out.print(header + "\t");
                }
            }
            //System.out.println();  // 換行
            int index = findStockIndex(headers, stock);
            String[] stockPriceStr;
            int currentLine = 1;

            while ((line = br.readLine()) != null) {
                if (currentLine >= start && currentLine <= end) {
                    // 在這裡處理你要的資料
                    stockPriceStr = line.split(","); // 假設每個數字之間使用逗號分隔
                    stockPrice.add(Double.parseDouble(stockPriceStr[index]));
                    currentLine++;
                }else if(currentLine < start){
                    currentLine++;
                }else if (currentLine > end) {
                    break; // 讀取到結束行後停止
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stockPrice;
    }
    public static int findStockIndex(String[] stocks, String stock) {
        for (int i = 0; i < stocks.length; i++) {
            if (stocks[i].equals(stock)) {
                return i;
            }
        }
        return -1;  // 如果找不到目標股票，回傳-1
    }
    // 計算移動平均
    public static List<Double> calculateMovingAverage(List<Double> prices, int windowSize) {
        List<Double> movingAverage = new ArrayList<>();
        for (int i = 0; i < prices.size() - windowSize + 1; i++) {
            double sum = 0;
            for (int j = i; j < i + windowSize; j++) {
                sum += prices.get(j);
            }
            double average = sum / windowSize;
            movingAverage.add(average);
        }
        return movingAverage;
    }
    public static void writeToOutputTask(String stock, int start, int end, List<Double> dealt) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.csv", true))) {
            //寫入股票名、開始日、結束日
            writer.write(stock + "," + start + "," + end + "\n");
            //String formattedSMA = " ";
            // 寫入移動平均數據
            int lastIndex = dealt.size() - 1;
            for (int i = 0; i < dealt.size(); i++) {
                double sma = dealt.get(i);
                //System.out.print(sma + ",");
                DecimalFormat df = new DecimalFormat("#.##");
                String formatted = df.format(sma);

                writer.write(formatted);
                if (i != lastIndex) {
                    writer.write(",");
                }
            }
            writer.newLine();  // 換行
            //System.out.println("數據已成功寫入到文件 Output.csv");
        } catch (IOException e) {
            System.err.println("寫入檔案時發生錯誤：" + e.getMessage());
        }
    }
    public static List<Double> calculateStdDev(List<Double> prices, List<Double> movingAverage) {
        List<Double> stdDevList = new ArrayList<>();
        double sumOfSquaredDeviations = 0;
        for (int i = 0; i < prices.size(); i++) {
            double deviation = prices.get(i) - movingAverage.get(0);
            sumOfSquaredDeviations += deviation * deviation;
        }
        double variance = sumOfSquaredDeviations / (prices.size() - 1);
        double stdDev = Math.sqrt(variance);
        stdDevList.add(stdDev);
        //System.out.println(stdDev);
        return stdDevList;
    }
    public static List<Integer> getTopThreeIndices(List<Double> stdDevs) {
        List<Double> sortedStdDevs = new ArrayList<>(stdDevs);
        Collections.sort(sortedStdDevs);
        Collections.reverse(sortedStdDevs); // 變更排序順序為由大到小
        List<Integer> topThreeIndices = new ArrayList<>();
        for (int i = 0; i < stdDevs.size(); i++) {
            double maxStdDev = sortedStdDevs.get(i);
            int index = stdDevs.indexOf(maxStdDev);
            topThreeIndices.add(index);
        }
        return topThreeIndices;
    }
    public static List<String> getTopThreeStocks(Elements thElements, List<Integer> indices) {
        List<String> topThreeStocks = new ArrayList<>();
        for (Integer index : indices) {
            Element th = thElements.get(index);
            topThreeStocks.add(th.text());
        }
        return topThreeStocks;
    }
    public static void writeToTask3(String filename, List<String> topThreeStocks, List<Integer> indices, List<Double> stdDevs, int start, int end) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            DecimalFormat df = new DecimalFormat("#.##");
            String format1 = df.format(stdDevs.get(indices.get(0)));
            String format2 = df.format(stdDevs.get(indices.get(1)));
            String format3 = df.format(stdDevs.get(indices.get(2)));
            String stock1 = topThreeStocks.get(0);
            String stock2 = topThreeStocks.get(1);
            String stock3 = topThreeStocks.get(2);
            writer.append(stock1 + "," + stock2 + "," + stock3 + "," + start + "," + end + "\n");
            writer.append(format1 + "," + format2 + "," + format3);
            writer.newLine();
            //System.out.println("Top 3 stocks with highest standard deviation written to output.csv.");
        } catch (IOException e) {
            System.err.println("寫入檔案時發生錯誤：" + e.getMessage());
        }
    }
    public static void CSVReaderWriter(String inputFile, String outputFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            FileWriter writer = new FileWriter(outputFile)) {

            String line;
            while ((line = reader.readLine()) != null) {
                // 處理每一行的資料，這裡假設直接將每一行的內容寫入 output.csv 中
                writer.write(line);
                writer.write("\n"); // 換行
            }

            //System.out.println("成功將 data.csv 的內容寫入 output.csv");
        } catch (IOException e) {
            System.err.println("讀取或寫入檔案時發生錯誤：" + e.getMessage());
        }
    }
}