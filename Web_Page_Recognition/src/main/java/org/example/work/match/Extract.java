package org.example.work.match;

import org.example.kit.entity.BiSupplier;
import org.example.kit.entity.ByteArray;
import org.example.kit.io.ByteBuilder;
import org.example.sql.model.InvertedIndex;
import org.example.work.crawl.WebCrawl;
import org.example.work.eigenword.EigenWord;
import org.example.work.eigenword.ExtractEigenWord;
import org.example.work.fingerprint.ExtractFingerprint;
import org.example.work.thread.Before;
import org.example.work.parse.nodes.Document;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @CLassname Extract
 * @Description 提取
 * @Date 2021/3/12 10:41
 * @Created by lenovo
 */
public class Extract {
    /**
     * 提取目标网页指纹特征
     * @param requestHeader -
     * @param responseHeader -
     * @param before -
     * @param matchTask -
     */
    public static void extractFingerprintAndEigenWord(ByteArray requestHeader, ByteArray responseHeader, Before before , MatchTask matchTask) {
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
        System.out.println("fingerprint size :" + fingerprint.size() );
        System.out.println("words size : " + vector.size());
        List<InvertedIndex> words = new ArrayList<>();

        for (EigenWord eigenWord : vector) {
            System.out.printf(" %x , %d\n",eigenWord.getWord(), eigenWord.getFrequency());
            InvertedIndex invertedIndex = new InvertedIndex();
            invertedIndex.setWord(eigenWord.getWord());
            invertedIndex.setFrequency(eigenWord.getFrequency());
            invertedIndex.setIndex(eigenWord.getIndex());
            words.add(invertedIndex);
        }

//        Fingerprint fp = new Fingerprint();
//        fp.setFpdata(fingerprint.getBytes());
//        fp.setLastUpdate(new Timestamp(System.currentTimeMillis()));

        matchTask.setEigenWords(words);
        matchTask.setFingerprint(fingerprint.getBytes());
    }

    /**
     * 爬取指定URL网页进行指纹特征提取，并插入数据库
     * @param matchTask - 待识别网页。
     */
    public static void crawl(MatchTask matchTask) {
        String url = null;
        if (matchTask.getHost() != null)
            url = "https://" + matchTask.getHost();
        if (url != null)
            if (matchTask.getPath() != null) url += "/" + matchTask.getPath();
            else url += "/";
        else {
            System.out.println("目标网页URL未知。");
            return;
        }
        System.out.println(url);
        try {
            BiSupplier<URL,byte[]> response = WebCrawl.getHttpPacketLoadedWithHTML(url);
            if (response == null) return ;
            byte[] data = response.second(); //未解码的响应报文，头部已分配。
            ByteArray content_encoding = null;
            if (WebCrawl.content_encoding != null) {
                content_encoding = WebCrawl.content_encoding;
            }
            String head = "HTTP/1.1 200 OK\r\n";
            ByteBuilder builder = new ByteBuilder(data.length + head.length());
            builder.write(head.getBytes());
            builder.write(data);
            ByteArray resp = new ByteArray(builder.getBytes());
            int spIndex = resp.indexOf(new byte[]{'\r', '\n', '\r', '\n'});
            Assert.isTrue(spIndex >= 0, "错误的 HTTP 报文格式");
            ByteArray responseHeader = resp.subByteArray(0, spIndex);
            ByteArray responseBody = resp.subByteArray(spIndex + 4);
            Before before = new Before(responseBody,url,content_encoding);
            extractFingerprintAndEigenWord(null,responseHeader,before,matchTask);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
