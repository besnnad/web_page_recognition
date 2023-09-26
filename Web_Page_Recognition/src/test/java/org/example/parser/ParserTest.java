package org.example.parser;

import org.example.kit.entity.BiSupplier;
import org.example.kit.entity.ByteArray;
import org.example.kit.io.ByteBuilder;
import org.example.work.crawl.WebCrawl;
import org.example.work.fingerprint.ExtractFingerprint;
import org.example.work.thread.Before;
import org.example.work.thread.ThreadToCrawlPages;
import org.example.work.match.Extract;
import org.example.work.match.MatchTask;
import org.example.work.parse.Parser;
import org.example.work.parse.nodes.Document;
import org.junit.Test;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

/**
 * @Classname ParserTest
 * @Description
 * @Date 2020/10/30 10:05
 * @Created by shuaif
 */
public class ParserTest {
    private final String host = "google.com";

    @Test
    public void testParser() {
//        Document document = WebCrawl.webCrawl("http://google.com");
        try {
            Parser parser = new Parser(Objects.requireNonNull(WebCrawl.getHttpPacketLoadedWithHTML(host)).second());
            Document doc = parser.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test_extract_fingerprint() {
        String url = "http://" + host;
        String url_news = "http://today.hit.edu.cn/article/2019/02/28/64283";
        try {
            BiSupplier<URL,byte[]> response = Objects.requireNonNull(WebCrawl.getHttpPacketLoadedWithHTML(url_news));
            byte[] data = response.second(); //未解码的响应报文，头部已分配。
            System.out.println(new String(data));
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
            URI uri = new URI(url_news);
            int spIndex = resp.indexOf(new byte[]{'\r', '\n', '\r', '\n'});
            Assert.isTrue(spIndex >= 0, "错误的 HTTP 报文格式");
            ByteArray responseHeader = resp.subByteArray(0, spIndex);
            ByteArray responseBody = resp.subByteArray(spIndex + 4);
            Before before = new Before(responseBody,url_news,content_encoding);
            Document document = before.getDocument();
            byte[] fingerprint = ExtractFingerprint.extractFingerprint(null,responseHeader,document);
            for (byte b : fingerprint) {
                System.out.printf("%02x ",b);
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test_extract_fingerprint_and_eigenword() {
        String url = "http://" + host;
        String url_news = "https://torfhaus-harzresort.de/";
        try {
            BiSupplier<URL,byte[]> response = Objects.requireNonNull(WebCrawl.getHttpPacketLoadedWithHTML(url_news));
            byte[] data = response.second(); //未解码的响应报文，头部已分配。
            System.out.println(new String(data));
            System.out.println(data.length);
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
            URI uri = new URI(url_news);
            int spIndex = resp.indexOf(new byte[]{'\r', '\n', '\r', '\n'});
            Assert.isTrue(spIndex >= 0, "错误的 HTTP 报文格式");
            ByteArray responseHeader = resp.subByteArray(0, spIndex);
            ByteArray responseBody = resp.subByteArray(spIndex + 4);
            System.out.println(content_encoding.toStr());
            Before before = new Before(responseBody,url_news,content_encoding);
            Document document = before.getDocument();
            new ThreadToCrawlPages(0,null).extractFingerprintAndEigenWord(null,responseHeader,before,0);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void new_test() {
//        List<JSONObject> jsonList = new ArrayList<>();
//        String filePath = FilePath.ROOT_PATH + "index2.data";
//        FileKit.readPacket(jsonList,filePath,0,1);
//        MyThread test = new MyThread();
//        JSONObject jo = jsonList.get(0);
//        String url = jo.getString("url");
//        byte[] _data = new byte[0];
//        _data = jo.getString("data").getBytes();
//        System.out.println(jo.getString("data"));
        String url_news = "https://www.hit.edu.cn/";
        try {
            BiSupplier<URL,byte[]> response = Objects.requireNonNull(WebCrawl.getHttpPacketLoadedWithHTML(url_news));
            byte[] data = response.second(); //未解码的响应报文，头部已分配。
            ByteArray content_encoding = null;
            if (WebCrawl.content_encoding != null) {
                content_encoding = WebCrawl.content_encoding;
            }
//            System.out.println(new String(data));
            int count = 2;
            while (count++==2){
                String head = "HTTP/1.1 200 OK\r\n";
                ByteBuilder builder = new ByteBuilder(data.length + head.length());
                builder.write(head.getBytes());
                builder.write(data);
                ByteArray resp = new ByteArray(builder.getBytes());
                URI uri = new URI(url_news);
                int spIndex = resp.indexOf(new byte[]{'\r', '\n', '\r', '\n'});
                Assert.isTrue(spIndex >= 0, "错误的 HTTP 报文格式");
                ByteArray responseHeader = resp.subByteArray(0, spIndex);
                ByteArray responseBody = resp.subByteArray(spIndex + 4);
//                System.out.println(content_encoding.toStr());
                Before before = new Before(responseBody, url_news, content_encoding);
//                Before before = new Before(responseBody, url_news, content_encoding,count);
//                System.out.println("current M :" + count);
                Extract.extractFingerprintAndEigenWord(null, responseHeader, before, new MatchTask());
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWrongPages() {
        String url_news = "https://stranamam.ru/";//"https://www.hit.edu.cn/";
        try {
            BiSupplier<URL,byte[]> response = Objects.requireNonNull(WebCrawl.getHttpPacketLoadedWithHTML(url_news));
            byte[] data = response.second(); //未解码的响应报文，头部已分配。
            ByteArray content_encoding = null;
            if (WebCrawl.content_encoding != null) {
                content_encoding = WebCrawl.content_encoding;
            }
//            System.out.println(new String(data));
            int count = 2;
            while (count++==2){
                String head = "HTTP/1.1 200 OK\r\n";
                ByteBuilder builder = new ByteBuilder(data.length + head.length());
                builder.write(head.getBytes());
                builder.write(data);
                ByteArray resp = new ByteArray(builder.getBytes());
                URI uri = new URI(url_news);
                int spIndex = resp.indexOf(new byte[]{'\r', '\n', '\r', '\n'});
                Assert.isTrue(spIndex >= 0, "错误的 HTTP 报文格式");
                ByteArray responseHeader = resp.subByteArray(0, spIndex);
                ByteArray responseBody = resp.subByteArray(spIndex + 4);
                System.out.println(content_encoding.toStr());
                Before before = new Before(responseBody, url_news, content_encoding);
                Extract.extractFingerprintAndEigenWord(null, responseHeader, before, new MatchTask());
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
