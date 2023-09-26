package org.example.work.fingerprint;

import org.example.uitl.Keys;
import org.example.kit.entity.ByteArray;
import org.example.kit.io.ByteBuilder;
import org.example.work.eigenword.EigenWord;
import org.example.work.eigenword.ExtractEigenWord;
import org.example.work.parse.Tag;
import org.example.work.parse.nodes.Document;
import org.example.work.parse.nodes.Element;
import org.example.work.parse.nodes.Node;
import org.example.work.parse.nodes.TextNode;

import java.util.*;

/**
 * @Classname ExtractFingerprint
 * @Description 提取指纹
 * @Date 2020/11/2 19:36
 * @Created by shuaif
 */
public class ExtractFingerprint {
    private static final int REQUEST_TAG = 0;
    private static final int RESPONSE_TAG = 1;
    private static final int HTML_HEAD_TAG = 2;
    private static final int HTML_BODY_TAG = 3;

    public static final int max_child_number_threshold = 200;
    public static final int max_parse_depth = 6;
    /**
     * BKDR算法实现，将字节数组映射为一个一个字节【辅助】
     * @param byteArray 字节数组对应的字符串
     * @return 字节
     */
    private static byte hashTo1Byte(String byteArray){
        int hash = 1;
        for (byte b : byteArray.getBytes()) {
            hash = 31 * hash + b;
        }
        hash ^= hash >> 16;
        hash ^= hash >> 8;
        return (byte)hash;
    }

    /**
     * 构造指纹：指纹头（4bits标志位，12bits指纹体长度） + 指纹体【辅助】
     * @param choice 标志位（0,1,2,3）
     * @param origin_fingerprint 原始指纹
     * @return 指纹
     */
    public static byte[] constructFingerprint(int choice,byte[] origin_fingerprint){
        byte flag = (byte)(choice << 4);
        byte length;
//        System.out.println("part fp size : " + origin_fingerprint.length);
        if (origin_fingerprint.length < 256) {
            length = (byte) (origin_fingerprint.length);
        } else {
            length = (byte) (0xFF & origin_fingerprint.length);
            flag = (byte) (flag ^ (origin_fingerprint.length >> 8));
        }
        byte[] result = new byte[origin_fingerprint.length + 2];
        result[0] = flag;
        result[1] = length;
        System.arraycopy(origin_fingerprint,0,result,2,origin_fingerprint.length);
        return result;
    }

    /**
     * 提取请求报头部指纹，主要针对cookie名字做处理
     * @param cookie 请求头部的COOKIE字段值
     */
    public static byte[] handleRequestHeader(String cookie){
        //TODO 爬虫获取网页响应的时候并没有提供cookie怎么获取。
        byte[] result;
        String[] key_value = cookie.split(";");
        result = new byte[key_value.length];
        int i = 0;
        for (String key : key_value) {
            result[i] = hashTo1Byte(key);
            i++;
        }
        return constructFingerprint(REQUEST_TAG,result);
    }

    /**
     * 提取响应报文头部指纹，针对响应头部的键值对，对需要提取指纹的部分（包括key和value做指纹提取
     * @param response_header 请求头部键值对
     */
    public static byte[] handleResponseHeader(String response_header){
        // 头部字段处理
        List<String> all_keys_list = new ArrayList<>(Arrays.asList(Keys.RESPONSE_HEADERS));
        List<String> value_used_list = new ArrayList<>(Arrays.asList(Keys.RESPOSE_VALUE_USED));
        List<String> key_not_used_list = new ArrayList<>(Arrays.asList(Keys.RESPONSE_KEY_NOT_USED));

        // 获取头部键值对，用“：”分割
        byte[] result = new byte[1024];
        int i = 0; // index索引

        String[] key_value = response_header.split("\r\n");
        for (String s : key_value) {
            if (!s.contains("HTTP/")) {
                String[] split = s.split(":");
                String key = split[0];
                String value = split[1];
                if (all_keys_list.contains(key)) {
                    if (!key_not_used_list.contains(key)) { // 所有不提取key字段的也不提取value字段
                        result[i++] = hashTo1Byte(key);
                        if (value_used_list.contains(key)) { // 判断是否需要提取首部值
                            result[i++] = hashTo1Byte(value);
                        }
                    }
                } else { // 扩展首部使用key
                    result[i++] = hashTo1Byte(key);
                }
            }
        }
        return constructFingerprint(RESPONSE_TAG,new ByteArray(result,0,i).getBytes());
    }

    /**
     * 对url字段去除host子串之后提取一个hash值
     * @param byteArray URL
     * @return hash_to_1_byte
     */
    private static byte urlHashTo1Byte(String byteArray) {
        if (byteArray.contains("http")) {
            String[] splits = byteArray.split("/");
            StringBuilder val = new StringBuilder();
            for (int i = 2; i < splits.length; i++) {
                val.append(splits[i]);
            }
            return hashTo1Byte(val.substring(0));
        }
        return hashTo1Byte(byteArray);
    }

    /**
     * 根据key值获取ID值，针对HTML head部分的指纹提取
     * @param key KEY
     * @return index
     */
    private static int getIDKey(String key) {
        key = key.toLowerCase();
        for (int i = 0; i < Keys.HEAD_PROPERTIES.length; i++) {
            if (key.equals(Keys.HEAD_PROPERTIES[i].toLowerCase())) {
                return i + 1;
            }
        }
        return -1;
    }

    static byte byteHash(ByteArray data){
        if(data == null)
            return 0;
        int hash = data.hashCode();
        hash ^= hash >>> 16;
        hash ^= hash >>> 8;
        return (byte)hash;
    }

    /**
     * 获取网页head部分的指纹，html_head部分类似于request_header部分的键值对 【IDkeyi, hash(value)】
     * @param html_head head部分的DOM结构
     */
    public static byte[] handleHtmlHeader(Element html_head){
        byte[] result = new byte[1024];
        int i = 0; // index索引
//        System.out.println(html_head.childrenSize());
        if (html_head == null) {
            return new byte[0];
        }
        for (Node node : html_head.children()) {
            if (!(node instanceof  Element)) {
                continue;
            }
            Element element = (Element)node;
            Tag tag = element.getTag();
//            System.out.println(tag.getName());
            switch (tag.getName()) {
                case "meta":

                    // 元数据通常以名称/值存在,如果没有name属性值，那么键值对可能以http-equiv的形式存在
                    ByteArray key = element.attr("name");
                    if (key == null) {
                        key = element.attr("http-equiv");
                    }
                    if (key == null) { // !name and !http-equiv 查看charset字段
                        ByteArray charset = element.attr("charset");
                        if (charset == null) {
                            byte[] temp = element.attrs().length > 1 ? element.attrs()[0].getKey() : new byte[]{-1};
                        }
                    } else {
                        ByteArray content = element.attr("content");
                        int id = getIDKey(key.toStr());
                        result[i++] = (byte) (id == -1 ? (byteHash(key) | 0x80) : id);
//                        if (content != null)  // change
//                            result[i++] = hashTo1Byte(content.toStr());
                    }
                case "link":
                    ByteArray rel = element.attr("rel");
                    if (rel == null) {
                        rel = element.attrs().length > 1 ? element.attrs()[0].getValue() : new ByteArray(new byte[]{-1});
                    }
                    int id = getIDKey(rel.toStr().toLowerCase());
                    result[i++] = (byte) (id == -1 ? (byteHash(rel) | 0x80) : id);

                    ByteArray href = element.attr("href");
                    if (href == null) {
                        break;
                    }
                    result[i++] = urlHashTo1Byte(href.toStr());
                case "title":
                    result[i++] = (byte) 2;
                    break;// 不提取
                case "style":
                    result[i++] = (byte) 1;
                    if (element.hasChild()) {
                        Node child = element.child(0);
                        if (child instanceof TextNode) {
                            result[i++] = hashTo1Byte(((TextNode) child).getText().toStr());
                        }
                    }
                case "noscript": //TODO
                    break;
                case "script":
                    result[i++] = (byte) 3;
                    ByteArray val = element.attr("src");
                    if (val != null) {
                        //  标签有src属性，进行指纹提取
                        result[i++] = urlHashTo1Byte(val.toStr());
                    }
                    break;
                default:
                    result[i++] = (byte) element.getTagName().hashCode();
            }
        }
//        System.out.println("i = " + i);
        return constructFingerprint(HTML_HEAD_TAG,new ByteArray(result,0,i).getBytes());

    }

    /**
     * 获取网页body部分的指纹，html_body部分的指纹为树形指纹
     * 标签节点[1bit 7bits tagID][1bit 7bits class hash],第一个1bit表示该节点是被否在其兄弟节点中最大，第二个1bit表示是否有孩子节点
     * 文本节点[1bit 7bit 0x00][8bits text hash]
     * @param html_body body部分的DOM结构
     * @param vector 网页特征向量
     */
    public static byte[] handleHtmlBody(Element html_body, List<EigenWord> vector){
        if (html_body == null) {
            System.out.println("HTML Body is null");
            return new byte[0];
        }
        byte[] result = new byte[4096];
        int i = 0;
        int oldi = i;

        Queue<Node> queue = new LinkedList<>(); // 队列用于层序遍历
        Node[] leaf_nodes = new Node[0]; // 存放解析最大深度时候的所有节点（相当于叶子节点），用于特征词提取
        queue.offer(html_body);
        // next_level存放下层节点数，to_be_print存放当前层待访问节点数，node_count记录总访问节点数，用于判断阈值（此间为200）
        int next_level = 0,to_be_printed = 1, node_count = to_be_printed;
        while (!queue.isEmpty()) {
            Node temp = queue.poll();
            if (temp instanceof Element) { // 对元素节点进行操作
                Element cur_elment = (Element)temp;
                int id = cur_elment.getTag().getId();
                result[i++] = (byte) ((temp == temp.getParent().lastChild() ? 1<<7 : 0) | (id | 0x80));
                // 从高到低前4位为class值的hash, 后4位为父元素在其兄弟节点的索引位置
                ByteArray cssCLass = cur_elment.attr("class");
                result[i++] = (byte) ((cur_elment.childrenSize() == 0 ? 0 : 1<<7) | (byteHash(cssCLass) | 0x80));
                temp.setHashCode(result[i] << 8);

                int child_count = 0;
                boolean add_children = true;
                for (Node child : cur_elment.children()) {
                    if (child instanceof TextNode || ((Element)child).getTag().isContentLevel()) // 记录文本相关节点个数
                        child_count++;
                }
                if (child_count > 3 && child_count > cur_elment.childrenSize() >> 1) { // 文本节点的数量超过三，大量文本后续不做
                    add_children = false;
                }

                if (add_children) {
                    child_count = 0;
                    for (Node child : cur_elment.children()) {
                        queue.offer(child);
                        child_count++;
                        node_count++;
                        if ((child_count > 29 || node_count > max_child_number_threshold) && child_count < cur_elment.childrenSize()) {
                            // 节点个数太多，退出当前循环
                            if (child_count > 1) {
                                queue.offer(cur_elment.lastChild());
                                child_count++;
                            }
                            break;
                        }
                    }
                    next_level += child_count;
                }
            } else if (temp instanceof TextNode) { // 对文本节点提取指纹
                TextNode text = (TextNode)temp;
                result[i++] = (byte) (temp == temp.getParent().lastChild() ? 1 << 7:0);
                result[i++] = hashTo1Byte(text.getText().toStr());
                temp.setHashCode(result[i] << 8);
            }
            to_be_printed--; // 更新待访问节点数目
            if (to_be_printed == 0) {
                to_be_printed = next_level;
                next_level = 0;
                if (i-oldi >= 12) { // 指纹序列长度大于某阈值，进行指纹提取。
                    // TODO 提取一个特征词
                    byte[] seq = new ByteArray(result,oldi,i).getBytes();
                    vector.add(new EigenWord(ExtractEigenWord.hashTo60Bits(seq) ^ (ExtractEigenWord.BODY_LEVEL_TAG << 60)));
                }
                oldi = i;

                if (temp.getDepth() + 1 == max_parse_depth) { // 提取所有最大解析深度处的叶子节点，用于路径特征词提取
                    leaf_nodes = queue.toArray(new Node[0]);
                }
            }
        }
        // TODO 特征词提取
        List<EigenWord> words = ExtractEigenWord.getBodyTreeEigenWord(html_body, leaf_nodes, max_parse_depth);
        if (words != null && words.size() != 0) vector.addAll(words);
        return constructFingerprint(HTML_BODY_TAG,new ByteArray(result,0,i).getBytes());
    }

    /**
     * 提取指纹调度 (只用于测试输出指纹进行比较)
     * @param requestHeader 请求头部
     * @param responseHeader 响应头部
     * @param document 网页文档--包含解析出来的DOM树（HTML元素为根节点）
     */
    public static byte[] extractFingerprint(ByteArray requestHeader, ByteArray responseHeader, Document document) {
        ByteBuilder fingerprint;
        byte[] request_fingerprint = new byte[0], response_fingerprint = new byte[0], html_head_fingerprint = new byte[0], html_body_fingerprint = new byte[0];
        if (requestHeader != null) {
            // TODO 提取cookie字段
        }
        if (responseHeader != null) {
            response_fingerprint = ExtractFingerprint.handleResponseHeader(new String(responseHeader.getBytes()));
        }
        if (document != null) {
            html_head_fingerprint = ExtractFingerprint.handleHtmlHeader(document.getHtml().childElement("head"));
            html_body_fingerprint = ExtractFingerprint.handleHtmlBody(document.getHtml().childElement("body"),new ArrayList<>());
        }
        int length = request_fingerprint.length + response_fingerprint.length + html_head_fingerprint.length + html_body_fingerprint.length;
        fingerprint = new ByteBuilder(length);
        fingerprint.write(request_fingerprint);
        fingerprint.write(response_fingerprint);
        fingerprint.write(html_head_fingerprint);
        fingerprint.write(html_body_fingerprint);

//        Fingerprint fp =  new Fingerprint();
//        fp.setLastUpdate(new Timestamp(System.currentTimeMillis()));
//
//        fp.setFpdata(fingerprint.getBytes());
        return fingerprint.getBytes();
    }


}
