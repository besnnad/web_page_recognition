package org.example.work.thread;

import net.sf.json.JSONObject;
import org.example.uitl.FilePath;
import org.example.kit.FileKit;
import org.example.kit.entity.BiSupplier;
import org.example.kit.entity.ByteArray;
import org.example.kit.io.ByteBuilder;
import org.example.sql.conn.ConnectToMySql;
import org.example.sql.model.Fingerprint;
import org.example.sql.model.InvertedIndex;
import org.example.sql.model.IptoHost;
import org.example.sql.model.PagetoUrl;
import org.example.work.crawl.WebCrawl;
import org.example.work.eigenword.EigenWord;
import org.example.work.eigenword.ExtractEigenWord;
import org.example.work.fingerprint.ExtractFingerprint;
import org.example.work.parse.nodes.Document;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.*;
import java.sql.Timestamp;
import java.util.*;

/**
 * @Classname
 * @Description 线程，
 * @Date 2020/11/10 20:26
 * @Created by shuaif
 */
public class ThreadToCrawlPages extends Thread{
    private final int serial_number;
    private final String website;
    private Queue<String> urls;
    private final int threshold = 100; // 指定一个网站爬取网页的最大数量。
    private int count = 0;
    private static final ConnectToMySql conn = new ConnectToMySql();

//    public static int page_id = 50417;

    public ThreadToCrawlPages() {
        this.serial_number = 0;
        this.website = "0.0";
    }
    public ThreadToCrawlPages(int serial_number, String website) {
        this.serial_number = serial_number;
        this.website = website;
        this.urls = new LinkedList<>();
    }

    /**
     * 单独处理一个URL：爬取+储存原始报文+解析+提取指纹特征（爬数据）
     * @param url 网页 URL
     */
    private void crawl(String url) {
        if (url == null) return ;
        System.out.println(url);
        try {
            BiSupplier<URL,byte[]> response = Objects.requireNonNull(WebCrawl.getHttpPacketLoadedWithHTML(url));
//            FileKit.writePacket(url,response.second());
            count++ ;
            byte[] data = response.second();

            ByteArray content_encoding = null;
            if (WebCrawl.content_encoding != null) { // 压缩格式，
                content_encoding = WebCrawl.content_encoding;
            }

            String head = "HTTP/1.1 200 OK\r\n";
            ByteBuilder builder = new ByteBuilder(data.length + head.length());
            builder.write(head.getBytes());
            builder.write(data);
            ByteArray resp = new ByteArray(builder.getBytes());
            URI uri = new URI(url);
            int spIndex = resp.indexOf(new byte[]{'\r', '\n', '\r', '\n'});
            Assert.isTrue(spIndex >= 0, "错误的 HTTP 报文格式");
            ByteArray responseHeader = resp.subByteArray(0, spIndex);
            ByteArray responseBody = resp.subByteArray(spIndex + 4);

            Before before = new Before(responseBody, url, content_encoding);
//            extractFingerprintAndEigenWord(null,responseHeader,before);
            for (String new_url : before.getParser().getUrls()) {
                if (new_url.startsWith("/")) {
                    new_url = "https://" + website + new_url;
                }
                if (new_url.startsWith("http")) {
                    urls.offer(new_url);
                }
            }
        } catch (IOException e) {
            System.out.println("爬取返回结果为空");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * 爬取指定URL网页进行指纹特征提取，并插入数据库
     * @param url -
     * @param page_id -网页URL
     */
    public void crawl_new(String url, int page_id) {
        if (url == null) return ;
        System.out.println(url);
        try {
            BiSupplier<URL,byte[]> response = WebCrawl.getHttpPacketLoadedWithHTML(url);
            if (response == null) return ;
            byte[] data = response.second(); //未解码的响应报文，头部已分配。
//            System.out.println(new String(data));
            ByteArray content_encoding = null;
            if (WebCrawl.content_encoding != null) {
                content_encoding = WebCrawl.content_encoding;
            }
//            System.out.println(new String(data));
            String head = "HTTP/1.1 200 OK\r\n";
            ByteBuilder builder = new ByteBuilder(data.length + head.length());
            builder.write(head.getBytes());
            builder.write(data);
            ByteArray resp = new ByteArray(builder.getBytes());
            int spIndex = resp.indexOf(new byte[]{'\r', '\n', '\r', '\n'});
            Assert.isTrue(spIndex >= 0, "错误的 HTTP 报文格式");
            ByteArray responseHeader = resp.subByteArray(0, spIndex);
            ByteArray responseBody = resp.subByteArray(spIndex + 4);
//            System.out.println(content_encoding.toStr());
            Before before = new Before(responseBody,url,content_encoding);
            extractFingerprintAndEigenWord(null,responseHeader,before,page_id);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从文件中读取网页源数据建立指纹库
     */
    public void buildFpAndWordsLib_new() {
        List<JSONObject> jsonList = new ArrayList<>(); // json列表
        String filePath = FilePath.ROOT_PATH + "index-finish.data"; // 读取源文件
        int start_line = Integer.parseInt(FileKit.readALineFromFile(FilePath.LAST_READ_LINE)); // 上一次读到行数
        int threshold = 20000 ; // 限定一次读入内存最大数据
        int page_id = Integer.parseInt(FileKit.readALineFromFile(FilePath.LAST_PAGE_ID));
        do {
            jsonList.clear();
            start_line = FileKit.readPacket(jsonList,filePath,start_line,threshold);
            for (int i = 500; i < jsonList.size(); i++) {
                JSONObject jo = jsonList.get(i);
                String url = jo.getString("url");
                try {
                    FileKit.writeAllLines(Collections.singletonList(url), FilePath.URL_LIST);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                crawl_new(url,page_id++);
            }
            FileKit.writeALineToFile(start_line + "",FilePath.LAST_READ_LINE);
            FileKit.writeALineToFile(String.valueOf(page_id),FilePath.LAST_PAGE_ID);
        } while (jsonList.size() >= threshold);
    }

    /**
     * 从文件中读取DNS相关信息并建立IP——HOST库
     */
    public void buildIpAndHostLib() {
        try {
            for (String line : FileKit.getAllLines(FilePath.ALL_IPS)) {
                IptoHost iptoHost = new IptoHost();
                iptoHost.setIp(line.split(",")[0]);
                iptoHost.setHost(line.split(",")[1]);
                conn.getMatchMapper().insertIptoHost(iptoHost);
            }
            conn.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 做报文解析和特征提取
     * @param url -
     * @param data -响应报文
     */
    public void doParseAndExtract(String url, byte[] data, int page_id) {
        System.out.println("URL : " + url);
        String head = "HTTP/1.1 200 OK\r\n";
        ByteBuilder builder = new ByteBuilder(data.length + head.length());
        builder.write(head.getBytes());
        builder.write(data);
        ByteArray resp = new ByteArray(builder.getBytes());
        int spIndex = resp.indexOf(new byte[]{'\r', '\n', '\r', '\n'});
        Assert.isTrue(spIndex >= 0, "错误的 HTTP 报文格式");
        ByteArray responseHeader = resp.subByteArray(0, spIndex);
        ByteArray responseBody = resp.subByteArray(spIndex + 4);
        ByteArray content_encoding = new ByteArray("gzip".getBytes());

//        System.out.println(new String(responseBody.getBytes()));

        Before before = new Before(responseBody, url, content_encoding);
        extractFingerprintAndEigenWord(null,responseHeader,before,page_id);
    }


    /**
     * 提取指纹以及网页特征向量,插入数据库
     * @param requestHeader 请求头部
     * @param responseHeader 响应头部
     * @param before 网页预处理结果
     */
    public void extractFingerprintAndEigenWord(ByteArray requestHeader, ByteArray responseHeader, Before before , int page_id) {
        Document document = before.getDocument();
        ByteBuilder fingerprint;
        List<EigenWord> vector = new ArrayList<>();
        byte[] request_fingerprint = new byte[0], response_fingerprint = new byte[0], html_head_fingerprint = new byte[0], html_body_fingerprint = new byte[0];
        if (requestHeader != null) {
            // TODO 提取cookie字段
        }
        if (responseHeader != null) {
            response_fingerprint = ExtractFingerprint.handleResponseHeader(new String(responseHeader.getBytes()));
            vector.addAll(ExtractEigenWord.getLinearFingerprintEigenWord(response_fingerprint,ExtractEigenWord.RESPONSE_HEADER_TAG));
        }
        if (document != null) {
            html_head_fingerprint = ExtractFingerprint.handleHtmlHeader(document.getHtml().childElement("head"));
            vector.addAll(ExtractEigenWord.getLinearFingerprintEigenWord(html_head_fingerprint,ExtractEigenWord.HEAD_HTML_TAG));
            html_body_fingerprint = ExtractFingerprint.handleHtmlBody(document.getHtml().childElement("body"),vector);
        }
        // 提取静态特征
        ExtractEigenWord.getStaticFeatureEigenWord(before,vector);
        // 拼接指纹
        int length = request_fingerprint.length + response_fingerprint.length + html_head_fingerprint.length + html_body_fingerprint.length;
        fingerprint = new ByteBuilder(length);
        fingerprint.write(request_fingerprint);
        fingerprint.write(response_fingerprint);
        fingerprint.write(html_head_fingerprint);
        fingerprint.write(html_body_fingerprint);

        for (int i = 0; i < vector.size(); i++) {
            vector.get(i).setIndex(i);
        }

        for (byte b : fingerprint.getBytes()) {
            System.out.printf("%02x ",b);
        }
        System.out.println();
        System.out.println("words size : " + vector.size());
        List<InvertedIndex> words = new ArrayList<>();

        for (EigenWord eigenWord : vector) {
//            System.out.printf(" %x , %d\n",eigenWord.getWord(), eigenWord.getFrequency());
            InvertedIndex invertedIndex = new InvertedIndex();
            invertedIndex.setPageId(page_id);
            invertedIndex.setWord(eigenWord.getWord());
            invertedIndex.setFrequency(eigenWord.getFrequency());
            invertedIndex.setIndex(eigenWord.getIndex());
            words.add(invertedIndex);
        }

        Fingerprint fp = new Fingerprint();
        fp.setFpdata(fingerprint.getBytes());
        fp.setLastUpdate(new Timestamp(System.currentTimeMillis()));
        fp.setPageId(page_id);

        PagetoUrl pagetoUrl = new PagetoUrl();
        pagetoUrl.setPageId(page_id);
        pagetoUrl.setUrl(before.getUrl());
        page_id++;
        insertDatabase(pagetoUrl,fp,words);

//        fp.setLastUpdate(new Timestamp(System.currentTimeMillis()));
//
//        fp.setFpdata(fingerprint.getBytes());
    }

    synchronized private void insertDatabase(PagetoUrl pagetoUrl, Fingerprint fp, List<InvertedIndex> words) {
        synchronized (conn) {
            conn.insertPagetoUrl(Collections.singletonList(pagetoUrl));

            conn.insertFingerprint(Collections.singletonList(fp));
            conn.insertEigenWord(words);
        }
    }


    @Override
    public void run() {
        String host_url = "https://" + this.website + "/";
        this.urls.offer(host_url);
        String url = this.urls.poll();
        long start = System.currentTimeMillis();
        crawl_new(url, serial_number + 500);
        long end = System.currentTimeMillis();
        System.out.println("count = " + count + ", timespan = " + (end - start) + ", website = " + website + ", url = " + url);
//        while (this.urls.size()!= 0 && count < 100) {
//            String url = this.urls.poll();
//            long start = System.currentTimeMillis();
//            crawl(url);
//            long end = System.currentTimeMillis();
//            System.out.println("count = " + count + ", timespan = " + (end - start) + ", website = " + website + ", url = " + url);
//            // ip -> host 记录
//            if (count == 1) {
//                try {
//                    URL current_url = new URL(host_url);
//                    String host = current_url.getHost();
//                    InetAddress address = InetAddress.getByName(host);
//                    String ip = address.getHostAddress();
//                    InetAddress[] IP = InetAddress.getAllByName(host);
//                    List<String> ipList = new ArrayList<>();
//                    for (InetAddress inetAddress : IP) {
//                        ipList.add(inetAddress.getHostAddress() + "," + host);
//                    }
//                    FileKit.writeAllLines(ipList, FilePath.ALL_IPS);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
        System.out.println("serial:" + serial_number + " is over.");
    }
}
