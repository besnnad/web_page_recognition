package org.example.work.thread;

import org.example.kit.entity.ByteArray;
import org.example.kit.io.ByteBuilder;
import org.example.sql.conn.ConnectToMySql;
import org.example.work.crawl.WorkerException;
import org.example.work.parse.Parser;
import org.example.work.parse.nodes.Document;
import org.example.work.parse.nodes.Node;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;

/**
 * @Classname Before
 * @Description 预处理，处理指纹提取和特征词提取之前的必要变量，主要负责网页解析相关的配置。
 * @Date 2020/11/14 18:08
 * @Created by shuaif
 */
public class Before {
    private Document document;
    private Parser parser;
    private ByteArray html_source;
    private long gziptime;
    private final ByteArray CONTENT_ENCODING;
    private int max_parse_depth = 0;
    private String url;
    private ConnectToMySql sql;

    private List<Node> resources = new ArrayList<>();                 // 网页引用的资源：CSS文件、JavaScript文件、图片、音频、视频等等，iframe需要特殊分析
    private List<Node> hyper_links = new ArrayList<>(25); // 网页中的超链接：开头为 "http://"、"https://"、"/"、"../"、"//"等
    private List<Node> forms = new ArrayList<>(4);       // 网页中的表单

    public Document getDocument() {
        return document;
    }

    public Parser getParser() {
        return parser;
    }

    public ByteArray getHtml_source() {
        return html_source;
    }

    public int getMax_parse_depth() {
        return max_parse_depth;
    }

    public List<Node> getResources() {
        return resources;
    }

    public List<Node> getHyper_links() {
        return hyper_links;
    }

    public List<Node> getForms() {
        return forms;
    }

    public Before(ByteArray html_source, String url, ByteArray content_encoding) {
        this.html_source = html_source;
        this.url = url;
        this.CONTENT_ENCODING = content_encoding;
        parseHTML();
        this.sql = new ConnectToMySql();
    }

    public Before(ByteArray html_source, String url, ByteArray content_encoding,int max_parse_depth) {
        this.html_source = html_source;
        this.url = url;
        this.CONTENT_ENCODING = content_encoding;
        this.max_parse_depth = max_parse_depth;
        parseHTML();
        this.sql = new ConnectToMySql();
    }

    public String getUrl() {
        return url;
    }

    /**
     * 网页解析期间根据标签做出相应的动作
     */
    private void parseHTML(){
//        System.out.println("before - content_encoding : " + CONTENT_ENCODING.toStr());
        if (this.CONTENT_ENCODING != null) handleContentEncoding(this.CONTENT_ENCODING);
        html_source.handleUnicodeIdentifier();
        parser = new Parser(html_source);

        if (this.max_parse_depth != 0 && this.max_parse_depth > 0) {
            parser.setMaxParsingDepth(this.max_parse_depth);
        }
//        System.out.println("current M :" + parser.getMaxParsingDepth());
//        System.out.print(parser.getMaxParsingDepth());

        parser.addAction("a", hyper_links::add);

        parser.addAction("form", forms::add);

        Consumer<Node> resAct = resources::add;

        parser.addAction("img", resAct);
        parser.addAction("link", resAct);

        parser.addAction("script", a -> {
            resources.add(a);
        });

        document = parser.parse();
        max_parse_depth = parser.getMaxParsingDepth();

        // 静态元素
//        System.out.println("static resources : ");
//        System.out.println(resources.size());
//        System.out.println(forms.size());
//        System.out.println(hyper_links.size());
    }

    /**
     * 处理文档编码以及解压缩的问题
     * @param encoding content-encoding字段value
     */
    void handleContentEncoding(ByteArray encoding){
        long start = System.nanoTime();
        switch(encoding.toStr().toLowerCase()){
            case "gzip":{
                ByteArrayInputStream in = new ByteArrayInputStream(html_source.getParentBytes(), html_source.from(), html_source.length());
                try{
                    html_source = decompress(new GZIPInputStream(in, 128));
                }catch(IOException e){
//                    System.out.println(Arrays.toString(html_source.getBytes()));
//                    throw new WorkerException("HTML 解压缩过程失败 " + e.getMessage(), e);
                }
                gziptime = System.nanoTime() - start;
                break;
            }
            case "deflate":{
                ByteArrayInputStream in = new ByteArrayInputStream(html_source.getParentBytes(), html_source.from(), html_source.length());
                try{
                    html_source = decompress(new DeflaterInputStream(in));
                }catch(IOException e){
                    throw new WorkerException("HTML 解压缩过程失败 " + e.getMessage(), e);
                }
                gziptime = System.nanoTime() - start;
                break;
            }
            default:{
                // 网页中该字段值有 plain identity br x-gzip ISO-8859-1 utf-8 text 空 UTF8 GB2312 xxxxxxxxxxxxx GZIP binary Windows-1251，这些都是错的
//            throw new WorkerException("系统不支持 \"" + encoding.toStr() + "\" 解压缩！");
                break;
            }
        }
    }

    private ByteArray decompress(InputStream decompress) throws IOException{
        ByteBuilder out = new ByteBuilder((html_source.length() << 1) + (html_source.length() >>> 1));
        byte[] buffer = new byte[128];
        int n;
        while((n = decompress.read(buffer)) > 0){
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }

}
