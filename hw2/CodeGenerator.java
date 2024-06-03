package hw2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeGenerator {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("請輸入mermaid檔案名稱");           
        }
        else {
            // get input
            String fileName = args[0];
            //創建FileReader物件並讀檔
            MyFileReader mermaidCodeReader = new MyFileReader();
            List<String> lines = mermaidCodeReader.readLines(fileName);

            // 使用HashMap儲存每個類的内容
            HashMap<String, List<String>> classContentMap = new HashMap<>();
            List<String> classNames = Parser.findClass(lines);
            for (String className : classNames) {
                classContentMap.put(className, new ArrayList<>());
            }
            List<String>returnClassName = Parser.content(classNames, lines, classContentMap);
            // 將每個className寫入檔案並將其對應的content放入檔案中
            for (Map.Entry<String, List<String>> entry : classContentMap.entrySet()) {
                String className = entry.getKey();
                List<String> content = entry.getValue();
                writeToFile(content, className, returnClassName);
            }
        }
    }
    private static void writeToFile(List<String> content, String className, List<String> returnClassName) {
        String fileName = className + ".java";
        try (FileWriter writer = new FileWriter(fileName)) {
            if(returnClassName.isEmpty()){
                for (String line : content) {
                    writer.write(line + "\n"); // 寫入每一行內容，並換行
                }
            }else{
                writer.write("public class " + className + " {" + "\n"); // 寫入開頭
                for(int i = 0;i < returnClassName.size();i++){
                    String parts[] = returnClassName.get(i).split(":");
                    String checkClassName = parts[0];
                    String afterCheckContent = parts[1];
                    if(checkClassName.contains(className)){
                        writer.write(afterCheckContent + "\n"); // 寫入每一行內容，並換行
                    }
                }
                writer.write("}"); // 寫入結尾
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
class MyFileReader {
    public List<String> readLines(String fileName) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line); // 將每一行内容添加到 ArrayList 中
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }
}
class Parser {
	public static List<String> findClass(List<String> lines) {
        //定義正則表達式找到class後的類名
        String regex = "\\bclass\\s+(\\w+)\\b";
        Pattern pattern = Pattern.compile(regex);

        List<String> classNames = new ArrayList<>();

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find() && !line.contains("{")) {
                String className = matcher.group(1);
                if (!classNames.contains(className)) { // 檢查是否已經存在相同的class name
                    classNames.add(className); // 如果不存在，則加入到classNames列表中
                }
            }else if (matcher.find() && line.contains("{")){
                String className = matcher.group(1);
                if (!classNames.contains(className)) { // 檢查是否已經存在相同的class name
                    classNames.add(className); // 如果不存在，則加入到classNames列表中
                }
            }
        }
        return classNames;
    }
    public static List<String> content(List<String> classNames, List<String> lines, HashMap<String, List<String>> classContentMap) {
        List<String> contentList = new ArrayList<>();
        List<String> returnClassName = new ArrayList<>();
        for (String className : classNames) {
            classContentMap.put(className, new ArrayList<>());
            contentList.add("public class " + className + " {"); // 添加第一行内容
            classContentMap.put(className, contentList);
        }

        for (String line1 : lines) {
            for (String className : classNames) {
                String line = line1.trim();
                if (line.startsWith(className)) {
                    // 解析屬性
                    if (line.contains(":")){
                        String[] parts = line.split(":");
                        String attribute = parts[1].trim(); // 獲取屬性或方法定義部分(冒號後部分)
                        className = parts[0].trim();

                        if(attribute.contains("int") || attribute.contains("void") || attribute.contains("String") || attribute.contains("boolean")){
                            String afterClass[] = attribute.split("\\s+");
                            String accessModifier = parts[1];//type or name
                            if(!accessModifier.contains("(")){
                                if(accessModifier.contains("+")){
                                    String attributeName = afterClass[1];
                                    String attributeType = afterClass[0].replaceAll("[^a-zA-Z\\[\\]]", "");
                                    attribute = "    public " + attributeType + " " + attributeName + ";";
                                    classContentMap.get(className).add(attribute);
                                    returnClassName.add(className + ":" + attribute);
                                }else if(accessModifier.contains("-")){
                                    String attributeName = afterClass[1];
                                    String attributeType = afterClass[0].replaceAll("[^a-zA-Z\\[\\]]", "");
                                    attribute = "    private " + attributeType + " " + attributeName + ";";
                                    classContentMap.get(className).add(attribute);
                                    returnClassName.add(className + ":" + attribute);
                                }
                            }else if(accessModifier.contains("(")){
                                if(accessModifier.contains("+")){
                                    //找到()裡面的東西
                                    Pattern pattern = Pattern.compile("\\((.*?)\\)");
                                    Matcher matcher = pattern.matcher(line);
        
                                    if (matcher.find()) {//如果()裡面有東西
                                        String inside = matcher.group(1);//抓出()裡面的東西
                                        if(!inside.isEmpty()){
                                            String attributeType = attribute.split("\\)")[1].trim();
                                            String attributeNameBefore = afterClass[0].replaceAll("[^a-zA-Z\\[\\] ()]", "");
                                            String attributeName = attributeNameBefore.split("\\(")[0];
                                            if(attributeType.contains("int") && !accessModifier.contains("get") && !accessModifier.contains("set")){
                                                String returnType = "{return 0;}";
                                                attribute = "    public " + attributeType + " " + attributeName + "(" + inside + ") " +returnType;
                                                classContentMap.get(className).add(attribute);
                                                returnClassName.add(className + ":" + attribute);
                                            }else if(attributeType.contains("String") && !accessModifier.contains("get") && !accessModifier.contains("set")){
                                                String returnType = "{return \"\";}";
                                                attribute = "    public " + attributeType + " " + attributeName + "(" + inside + ") " +returnType;
                                                classContentMap.get(className).add(attribute);
                                                returnClassName.add(className + ":" + attribute);
                                            }else if(attributeType.contains("boolean") && !accessModifier.contains("get") && !accessModifier.contains("set")){
                                                String returnType = "{return false;}";
                                                attribute = "    public " + attributeType + " " + attributeName + "(" + inside + ") " +returnType;
                                                classContentMap.get(className).add(attribute);
                                                returnClassName.add(className + ":" + attribute);
                                            }else if(attributeType.contains("void") && !accessModifier.contains("get") && !accessModifier.contains("set")){
                                                String returnType = "{;}";
                                                attribute = "    public " + attributeType + " " + attributeName + "(" + inside + ") " +returnType;
                                                classContentMap.get(className).add(attribute);
                                                returnClassName.add(className + ":" + attribute);
                                            }else if(accessModifier.contains("get")){
                                                int startIndex = line.indexOf("g") + 3;
                                                int endIndex = line.indexOf("(", startIndex);
                                                String returnString = line.substring(startIndex,endIndex);
                                                String returnStringLower =returnString.toLowerCase();
                                                attribute = "    public " + attributeType + " " + attributeName + " {\n        return " + returnStringLower + ";\n    }";
                                                classContentMap.get(className).add(attribute);
                                                returnClassName.add(className + ":" + attribute);
                                            }else if(accessModifier.contains("set")){
                                                int startIndex = attributeNameBefore.indexOf("s") + 3;
                                                int endIndex = attributeNameBefore.indexOf("(", startIndex);
                                                String thisString = attributeNameBefore.substring(startIndex,endIndex);
                                                String thisStringLower = thisString.toLowerCase();
                                                attribute = "    public " + attributeType + " " + attributeName + "(" + inside + ")" + " {\n        this." + thisStringLower + " = " + thisStringLower + ";\n    }";
                                                classContentMap.get(className).add(attribute);
                                                returnClassName.add(className + ":" + attribute);
                                            }
                                        }else if(inside.isEmpty()){//如果()裡面沒有東西
                                            String attributeType = afterClass[1];
                                            String attributeName = afterClass[0].replaceAll("[^a-zA-Z\\[\\]()]", "");
                                            if(attributeType.contains("int") && !accessModifier.contains("get") && !accessModifier.contains("set")){
                                                String returnType = "{return 0;}";
                                                attribute = "    public " + attributeType + " " + attributeName + " " + returnType;
                                                classContentMap.get(className).add(attribute);
                                                returnClassName.add(className + ":" + attribute);
                                            }else if(attributeType.contains("String") && !accessModifier.contains("get") && !accessModifier.contains("set")){
                                                String returnType = "{return \"\";}";
                                                attribute = "    public " + attributeType + " " + attributeName + " " + returnType;
                                                classContentMap.get(className).add(attribute);
                                                returnClassName.add(className + ":" + attribute);
                                            }else if(attributeType.contains("boolean") && !accessModifier.contains("get") && !accessModifier.contains("set")){
                                                String returnType = "{return false;}";
                                                attribute = "    public " + attributeType + " " + attributeName + " " + returnType;
                                                classContentMap.get(className).add(attribute);
                                                returnClassName.add(className + ":" + attribute);
                                            }else if(attributeType.contains("void") && !accessModifier.contains("get") && !accessModifier.contains("set")){
                                                String returnType = "{;}";
                                                attribute = "    public " + attributeType + " " + attributeName + " " + returnType;
                                                classContentMap.get(className).add(attribute);
                                                returnClassName.add(className + ":" + attribute);
                                            }else if(accessModifier.contains("get")){
                                                int startIndex = line.indexOf("g") + 3;
                                                int endIndex = line.indexOf("(", startIndex);
                                                String returnString = line.substring(startIndex,endIndex);
                                                String returnStringLower =returnString.toLowerCase();
                                                attribute = "    public " + attributeType + " " + attributeName + " {\n        return " + returnStringLower + ";\n    }";
                                                classContentMap.get(className).add(attribute);
                                                returnClassName.add(className + ":" + attribute);
                                            }else if(accessModifier.contains("set")){
                                                int startIndex = attributeName.indexOf("s") + 3;
                                                int endIndex = attributeName.indexOf("(", startIndex);
                                                String thisString = attributeName.substring(startIndex,endIndex);
                                                String thisStringLower = thisString.toLowerCase();
                                                attribute = "    public " + attributeType + " " + attributeName + " {\n        this." + thisStringLower + " = " + thisStringLower + ";\n    }";
                                                classContentMap.get(className).add(attribute);
                                                returnClassName.add(className + ":" + attribute);
                                            }
                                        }
                                    } 
                                }else if(accessModifier.contains("-")){
                                    //找到()裡面的東西
                                    Pattern pattern = Pattern.compile("\\((.*?)\\)");
                                    Matcher matcher = pattern.matcher(line);
        
                                    if (matcher.find()) {//如果()裡面有東西
                                        String inside = matcher.group(1);//抓出()裡面的東西
                                        if(!inside.isEmpty()){
                                            String attributeType = attribute.split("\\)")[1].trim();
                                            String attributeNameBefore = afterClass[0].replaceAll("[^a-zA-Z\\[\\] ()]", "");
                                            String attributeName = attributeNameBefore.split("\\(")[0];
                                            if(attributeType.contains("int") && !accessModifier.contains("get") && !accessModifier.contains("set")){
                                                String returnType = "{return 0;}";
                                                attribute = "    private " + attributeType + " " + attributeName + "(" + inside + ") " +returnType;
                                                classContentMap.get(className).add(attribute);
                                                returnClassName.add(className + ":" + attribute);
                                            }else if(attributeType.contains("String") && !accessModifier.contains("get") && !accessModifier.contains("set")){
                                                String returnType = "{return \"\";}";
                                                attribute = "    private " + attributeType + " " + attributeName + "(" + inside + ") " +returnType;
                                                classContentMap.get(className).add(attribute);
                                                returnClassName.add(className + ":" + attribute);
                                            }else if(attributeType.contains("boolean") && !accessModifier.contains("get") && !accessModifier.contains("set")){
                                                String returnType = "{return false;}";
                                                attribute = "    private " + attributeType + " " + attributeName + "(" + inside + ") " +returnType;
                                                classContentMap.get(className).add(attribute);
                                                returnClassName.add(className + ":" + attribute);
                                            }else if(attributeType.contains("void") && !accessModifier.contains("get") && !accessModifier.contains("set")){
                                                String returnType = "{;}";
                                                attribute = "    private " + attributeType + " " + attributeName + "(" + inside + ") " +returnType;
                                                classContentMap.get(className).add(attribute);
                                                returnClassName.add(className + ":" + attribute);
                                            }else if(accessModifier.contains("get")){
                                                int startIndex = line.indexOf("g") + 3;
                                                int endIndex = line.indexOf("(", startIndex);
                                                String returnString = line.substring(startIndex,endIndex);
                                                String returnStringLower =returnString.toLowerCase();
                                                attribute = "    private " + attributeType + " " + attributeName + " {\n        return " + returnStringLower + ";\n    }";
                                                classContentMap.get(className).add(attribute);
                                                returnClassName.add(className + ":" + attribute);
                                            }else if(accessModifier.contains("set")){
                                                int startIndex = attributeNameBefore.indexOf("s") + 3;
                                                int endIndex = attributeNameBefore.indexOf("(", startIndex);
                                                String thisString = attributeNameBefore.substring(startIndex,endIndex);
                                                String thisStringLower = thisString.toLowerCase();
                                                attribute = "    private " + attributeType + " " + attributeName + "(" + inside + ")" + " {\n        this." + thisStringLower + " = " + thisStringLower + ";\n    }";
                                                classContentMap.get(className).add(attribute);
                                                returnClassName.add(className + ":" + attribute);
                                            }
                                        }else if(inside.isEmpty()){//如果()裡面沒有東西
                                            String attributeType = afterClass[1];
                                            String attributeName = afterClass[0].replaceAll("[^a-zA-Z\\[\\]()]", "");
                                            if(attributeType.contains("int") && !accessModifier.contains("get") && !accessModifier.contains("set")){
                                                String returnType = "{return 0;}";
                                                attribute = "    private " + attributeType + " " + attributeName + " " + returnType;
                                                classContentMap.get(className).add(attribute);
                                                returnClassName.add(className + ":" + attribute);
                                            }else if(attributeType.contains("String") && !accessModifier.contains("get") && !accessModifier.contains("set")){
                                                String returnType = "{return \"\";}";
                                                attribute = "    private " + attributeType + " " + attributeName + " " + returnType;
                                                classContentMap.get(className).add(attribute);
                                                returnClassName.add(className + ":" + attribute);
                                            }else if(attributeType.contains("boolean") && !accessModifier.contains("get") && !accessModifier.contains("set")){
                                                String returnType = "{return false;}";
                                                attribute = "    private " + attributeType + " " + attributeName + " " + returnType;
                                                classContentMap.get(className).add(attribute);
                                                returnClassName.add(className + ":" + attribute);
                                            }else if(attributeType.contains("void") && !accessModifier.contains("get") && !accessModifier.contains("set")){
                                                String returnType = "{;}";
                                                attribute = "    private " + attributeType + " " + attributeName + " " + returnType;
                                                classContentMap.get(className).add(attribute);
                                                returnClassName.add(className + ":" + attribute);
                                            }else if(attributeName.contains("get")){
                                                int startIndex = line.indexOf("g") + 3;
                                                int endIndex = line.indexOf("(", startIndex);
                                                String returnString = line.substring(startIndex,endIndex);
                                                String returnStringLower =returnString.toLowerCase();
                                                attribute = "    private " + attributeType + " " + attributeName + " {\n        return " + returnStringLower + ";\n    }";
                                                classContentMap.get(className).add(attribute);
                                                returnClassName.add(className + ":" + attribute);
                                            }else if(accessModifier.contains("set")){
                                                int startIndex = attributeName.indexOf("s") + 3;
                                                int endIndex = attributeName.indexOf("(", startIndex);
                                                String thisString = attributeName.substring(startIndex,endIndex);
                                                String thisStringLower = thisString.toLowerCase();
                                                System.out.println(thisStringLower);
                                                attribute = "    private " + attributeType + " " + attributeName + " {\n        this." + thisStringLower + " = " + thisStringLower + ";\n    }";
                                                classContentMap.get(className).add(attribute);
                                                returnClassName.add(className + ":" + attribute);
                                            }
                                        }
                                    } 
                                }
                            }
                        }
                    }
                }
            }
        }
        contentList.add("}");
        return returnClassName;
    }
}