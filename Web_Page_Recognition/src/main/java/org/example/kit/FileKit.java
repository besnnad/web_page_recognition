package org.example.kit;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.example.uitl.FilePath;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileKit {
    public static final String TAG_SEPARATOR = "-------"; // json之间的分割符。
    public static String getUserRootPath(){
        return String.class.getResource("/").getPath();
    }

    public static byte[] getAllBytes(File file) throws IOException{
        long len = file.length();
        if(len > Integer.MAX_VALUE){
            throw new IOException("File is too big!");
        }
        InputStream in = new FileInputStream(file);
        byte[] bytes = new byte[(int)len];
        int offset = 0, n = 0;
        while(offset < bytes.length && (n = in.read(bytes, offset, bytes.length - offset)) >= 0){
            offset += n;
        }
        if(offset != bytes.length){
            throw new IOException("Failed to read the whole file!" + file.getName());
        }
        in.close();
        return bytes;
    }

    public static List<String> getAllLines(String fileName) throws IOException{
        return Files.readAllLines(Paths.get(fileName));
    }

    public static List<String> getAllLines(String fileName, String charset) throws IOException{
        return Files.readAllLines(Paths.get(fileName), Charset.forName(charset));
    }

    public static void writeAllLines(List<String> list, String fileName) throws IOException{
        File file = new File(fileName);
        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdir()) {
                System.out.println("创建目标文件目录失败");
                return ;
            }
        }
        file.createNewFile();
        PrintWriter pw = new PrintWriter(new FileOutputStream(fileName,true));
        for(String s : list){
            pw.println(s);
        }
        pw.close();
    }

    /**
     * 响应报文以JSON格式写入文件(误)
     * @param url 网页URL
     * @param data 响应报文数据
     */
    public static void writePacket(String url , byte[] data) {
        File file = new File(FilePath.ALL_PACKAGES);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            Map<String, String> map = new HashMap<String, String>();
            map.put("url", url);
            map.put("data", new String(data));
            JSONObject jsonData = JSONObject.fromObject(map);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
            synchronized (file) {
                bw.write(jsonData.toString());
                bw.write("\n");
                bw.write(TAG_SEPARATOR);
                bw.write("\n");
            }
            bw.close();
        } catch (FileNotFoundException e) {
            System.out.println("写入文件失败");
        } catch (IOException e) {
            System.out.println("创建文件失败");
        }

    }

    /**
     * （误）
     * @param content
     * @param fileName
     */
    public static void writeALineToFile(String content, String fileName) {
        File file = new File(fileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            bw.write(content);
            bw.write("\r\n");
            bw.close();
        } catch (FileNotFoundException e) {
            System.out.println("写入文件失败");
        } catch (IOException e) {
            System.out.println("创建文件失败");
        }
    }

    public static String readALineFromFile(String fileName) {
        File file = new File(fileName);
        String result = null;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            result = br.readLine();
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 从文件中读取数据，写入JSONObject(误)
     * @param jsonList - JSON格式列表
     * @param filePath - 报文存储路径。
     * @param startLine - 开始读取行号
     * @param threshold - 限定最大读入数据
     * @return 结束行。
     */
    public static int readPacket(List<JSONObject> jsonList, String filePath, int startLine, int threshold) {
        File file = new File(filePath);
        int count  = startLine;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while (count > 0) {
                br.readLine();
                count--;
            }
            count = startLine;
            while ((line = br.readLine()) != null) {
                count ++;
                if (line.equals(TAG_SEPARATOR)) {
                    continue;
                }
                try {
                    JSONObject jsonObject = JSONObject.fromObject(line);
                    jsonList.add(jsonObject);
                } catch (JSONException | NumberFormatException e) {
                    System.out.println("json 解析异常。");
                }
                if (jsonList.size() >= threshold) break; // 一次最多读入75000条。
            }
            System.out.println("读取JSON数据 ：" + jsonList.size());
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * （误）
     * @param filePath
     * @param startLine
     * @return
     */
    public static int readPacket2(String filePath, int startLine) {
        File file = new File(filePath);
        int count  = startLine;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while (count > 0) {
                br.readLine();
                count--;
            }
            count = startLine;
            while ((line = br.readLine()) != null) {
                count ++;
                if (line.equals(TAG_SEPARATOR)) {
                    continue;
                }
                try {
                    JSONObject jsonObject = JSONObject.fromObject(line);
                    String url = jsonObject.getString("url");
                    System.out.println("写入 ： "  + url);
                    System.out.println("startline: " + count);
                    writeAllLines(Collections.singletonList(url),FilePath.URL_LIST);
                } catch (JSONException | NumberFormatException e) {
                    System.out.println("json 解析异常。");
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }

    public static void writeAllLines(List<String> list, String fileName, String charset) throws IOException{
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName), charset));
        for(String s : list){
            pw.println(s);
        }
        pw.close();
    }
}
