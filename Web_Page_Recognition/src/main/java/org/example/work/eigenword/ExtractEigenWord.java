package org.example.work.eigenword;

import org.example.kit.entity.ByteArray;
import org.example.kit.io.ByteBuilder;
import org.example.kit.security.MD5;
import org.example.work.thread.Before;
import org.example.work.parse.Tag;
import org.example.work.parse.nodes.Element;
import org.example.work.parse.nodes.Node;

import java.util.*;
import java.util.function.Predicate;

/**
 * @Classname ExtractEigenWord
 * @Description 提取网页特征词相关
 * @Date 2020/11/11 16:10
 * @Created by shuaif
 */
public class ExtractEigenWord {
    public static final long REQUEST_HEADER_TAG = 1L;
    public static final long RESPONSE_HEADER_TAG = 2L;
    public static final long HEAD_HTML_TAG = 3L;
    public static final long BODY_HTML_TAG = 4L;

    public static final long BODY_LEVEL_TAG = 5L;
    public static final long BODY_PATH_TAG = 6L;
    public static final long JS_IN_HEAD = 7L;
    public static final long JS_IN_BODY = 8L;
    private static final long IMG_TAG = 9L;
    private static final long FORM_TAG = 10L;
    private static final long HYPER_LINK = 11L;


    /**
     * 计算子序列的hash值 基于MD5设计 【辅助】
     * @param arr 字节数组
     * @return long
     */
    public static long hashTo60Bits(byte[] arr) {
        long hash = 0;
        MD5 md5 = new MD5();
        md5.update(arr,0,arr.length);
        byte[] hash_bytes = md5.digest();
        int half_length = hash_bytes.length / 2;
        for (int i = 0; i < half_length ; i++) {
            hash = hash | (hash_bytes[i] ^ hash_bytes[i + half_length]);
            hash = hash << 8;
        }
        hash = hash ^ ((hash >> 60 ) << 56);
        return hash;
    }

    /**
     * 构造特征词 [4bits tag][60bits content]
     * @param arr 序列
     * @param tag 标志位
     */
    public static EigenWord constructEigenWord(byte[] arr ,long tag) {
        return new EigenWord(hashTo60Bits(arr) ^ (tag << 60));
    }

    /**
     * 构造特征词 [4bits tag][60bits content]
     * @param arr 序列
     * @param index index
     * @param tag 标志位
     */
    public static EigenWord constructEigenWord(byte[] arr , int index,long tag) {
        return new EigenWord((hashTo60Bits(arr) ^ index) ^ (tag << 60));
    }

    /**
     * Q-gram算法，针对线性指纹序列，提取特征词，【辅助】
     * @param seq 指纹序列
     * @param tag 标志
     * @return 特征词列表
     */
    public static List<EigenWord> qGram(byte[] seq, long tag) {
        if (seq.length < 8) {
            return new ArrayList<>();
        }
        int win_size,step; // 定义滑动窗口的大小和窗口移动步长
        if (seq.length < 14) {
            step = 3;
            win_size = 10;
        } else {
            step = 4;
            win_size = 12;
        }
        List<EigenWord> words = new ArrayList<>();
        int start_index = --step;
        byte[] son_seq = null;
        for (int i = (seq.length - win_size) / step; i >= 0; i--) {
            start_index += step;
            son_seq = new ByteArray(seq,start_index,start_index + win_size - 1).getBytes();
            words.add(constructEigenWord(son_seq,tag));
        }
        if (start_index + win_size < seq.length) {
            son_seq = new ByteArray(seq, seq.length - win_size,seq.length - 1).getBytes();
            words.add(constructEigenWord(son_seq,tag));
        }
        return words;
    }

    /**
     * 提取线性指纹序列的特征词
     * @param linear_seq 线性指纹序列
     * @param tag 特征词来源标志 ， 此间传入指纹标志
     * @return 特征词列表
     */
    public static List<EigenWord> getLinearFingerprintEigenWord(byte[] linear_seq, long tag) {
        // TODO
        return qGram(linear_seq,tag);
    }

    /**
     * 提取HTML 网页body路径部分的特征词，
     * 由于body树部分的指纹不是线性序列，因此此处的特征词提取从两个方面进行，层次和路径
     * 对于每一层的节点，提取前n个节点的指纹序列进行特征词（在层次指纹提取的过程中进行特征词提取 in ExtractFingerprint）
     * 对于最后一层节点，选取六条指纹不同的路径，对每一条路径指纹序列，进行特征词的提取。
     * @param html_body body树的根节点
     * @param leaf_nodes 最大解析层的全部节点
     * @param max_parse_depth 最大解析深度
     * @return 特征词序列
     */
    public static List<EigenWord> getBodyTreeEigenWord(Element html_body, Node[] leaf_nodes, int max_parse_depth) {
        if (leaf_nodes == null || leaf_nodes.length == 0) {
            for(int i = max_parse_depth - 1; i > 3; i--){
                leaf_nodes = getNodesByLevel(html_body, i);
                if(leaf_nodes != null && leaf_nodes.length > 0)
                    break;
            }
            if(leaf_nodes == null || leaf_nodes.length == 0)
                return null;
        }

        Node cur_parent = null; // 父节点，用于判断是否是一个节点的子节点
        List<EigenWord> words = new ArrayList<>();
        int count = 0;
        for (Node node : leaf_nodes) { // 删除指纹重复的兄弟节点
            if (node.getParent() == cur_parent) {
                words.get(count-1).addFrequency();
                continue;
            }
            if (count >= 6)
                break;
            cur_parent = node.getParent();
            ByteBuilder builder = new ByteBuilder((node.getDepth() - html_body.getDepth()) * 3);
            while (node.getDepth() > html_body.getDepth()) { // 提取路径指纹，存于builder下
                int hash = node.hashCode();
                builder.write(hash >> 16);
                builder.write(hash >> 8);
                if ((hash & 0xFE) != 0) {
                    builder.write(hash);
                }
                node = node.getParent();
            }
            words.add(constructEigenWord(builder.getBytes(),BODY_PATH_TAG)); // 对每一条路径提取一个特征词
            count++;
        }
        // 选取的路径阈值为6
        return words;
    }

    /**
     * 提取网页静态特征，包括网页的logo图标（*此处需要注意），图片文字和超链接等
     * @param before 网页解析阶段获取的静态特征相关信息
     * @param vector 网页特征向量
     */
    public static void getStaticFeatureEigenWord(Before before,List<EigenWord> vector) {
        getEigenWordFromResources(before.getResources(), vector,before.getMax_parse_depth());
        getEigenWordFromLink(before.getHyper_links(), vector, before.getMax_parse_depth());
        getEigenWordFromForm(before.getForms(), vector);
    }

    /**
     * 从网页URL中去除host
     * @param url URL
     */
    public static byte[] removeHostFromURL(byte[] url) {
        String URL = new String(url);
        StringBuilder result = new StringBuilder();
        if (URL.startsWith("/")) {
            return url;
        }
        if (URL.contains("http")) {
            String[] splits = URL.split("//");
            splits = splits[1].split("/");
            for (int i = 1 ; i < splits.length; i++) { // http:_ _host_path
                result.append("/").append(splits[i]);
            }
            System.out.println("result : " + result.toString());
            return result.toString().getBytes();
        }
        return url;

    }

    /**
     * 从script和img中提取特征词
     * @param resources_nodes
     * @param words
     */
    public static void getEigenWordFromResources(List<Node> resources_nodes, List<EigenWord> words, int max_parse_depth) {
        int script_count = 0, img_count = 0;
        for (Node node : resources_nodes) {
            Element element = (Element)node;
            Tag tag = element.getTag();
            switch (tag.getName()) {
                case "script":
                    ByteArray src = element.attr("src");
                    // Body 中的script
                    if (src != null && src.length() > 0 && !element.getParent().getTag().is("head")) {
                        words.add(constructEigenWord(src.getBytes(),script_count++,JS_IN_BODY));
                    }
                    break;
                case "img":
                    ByteArray img = element.attr("img");
                    if (element.getDepth() <= max_parse_depth && img != null && img_count <= 5) {
                        long hash = hashTo60Bits(removeHostFromURL(img.getBytes())) ^ (hashTo60Bits(element.attr("class").getBytes()) & 0x0F0L);
                        words.add(new EigenWord((hash ^ img_count++) ^ (IMG_TAG << 60)));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 从超链接中提取特征词
     * @param hyper_links_nodes
     * @param words
     */
    public static void getEigenWordFromLink(List<Node> hyper_links_nodes, List<EigenWord> words, int max_parse_depth) {
        int words_to_get = 5;
        for (Node node : hyper_links_nodes) {
            if (node.getDepth() > max_parse_depth) {
                continue;
            }
            Element element = (Element)node;
            ByteArray url = element.attr("href");
            if (url == null || url.get(0) == '#')
                continue;
            if ((url.get(0) == '/' && url.length() > 15) || url.startWith("http".getBytes())) {
                System.out.println("做特征词提取：" + url.toStr());
                byte[] temp = element.attr("class") == null ? new byte[0] : element.attr("class").getBytes();
                long hash = hashTo60Bits(removeHostFromURL(url.getBytes())) ^ (hashTo60Bits(temp) & 0x0F0L);
                words.add(new EigenWord((hash ^ (words_to_get-- & 0x0FL)) ^ (HYPER_LINK << 60)));
            }
            if (words_to_get == 0)
                break;
        }
    }

    /**
     * 表单处理：class，action.input and name
     * @param forms_nodes
     * @param words
     */
    public static void getEigenWordFromForm(List<Node> forms_nodes, List<EigenWord> words) {
        Set<String> form_tags = new HashSet<>(Arrays.asList("input", "select", "textarea"));
        for (Node node : forms_nodes) {
            Element form = (Element)node;
            // 从body树中的FORM节点的子树中获取所有需要的属性节点
            List<Node> inputs = findAllNodes(form,a -> a instanceof Element && form_tags.contains(((Element)a).getTagName()));
            ByteArray action = form.attr("action");
            if (action == null || inputs.size() < 2) {
                continue;
            }
            ByteArray _class = form.attr("class");
            // ????????
            long word = _class == null ? 31 : _class.hashCode() + 31;
            long hash = hashTo60Bits(removeHostFromURL(action.getBytes()));
            for (Node input : inputs) {
                Element element = (Element) input;
                if (element.attr("type") == null || element.attr("name") == null) continue;
                word = word * 31 + hashTo60Bits(element.attr("type").getBytes());
                word = word * 31 + hashTo60Bits(element.attr("name").getBytes());
            }
            words.add(new EigenWord((word ^ hash) ^ (FORM_TAG << 60)));
        }
    }
    /**
     * 获取一条路径上面的指纹的特征词列表
     * @param html_body body 元素
     * @param leaf_nodes 叶子节点列表
     * @param max_parse_depth 最大解析深度
     * @return 特征词列表
     */
    private static List<EigenWord> getPathWords(Element html_body, Node[] leaf_nodes, int max_parse_depth) {
        List<EigenWord> words = new ArrayList<>();
        for (Node leaf_node : leaf_nodes) {
            ByteBuilder ser = new ByteBuilder(1024);
            int index = 0;
            while (!leaf_node.getParent().equals(html_body)) {
                ser.write(leaf_node.toFpString().getBytes());
                index += leaf_node.toFpString().getBytes().length;
                leaf_node = leaf_node.getParent();
            }
            long word = hashTo60Bits(ser.subBytes(0,index));
            words.add(new EigenWord(word));
        }
        return words;
    }

    /**
     * 获取指定层的全部指纹相关节点
     * @param root 树的根节点
     * @param level 层数
     */
    private static Node[] getNodesByLevel(Element root, int level) {
        if (root == null || level < 1) {
            return null;
        }
        if (level == 1) {
            return new Node[]{root};
        }
        Queue<Node> queue = new LinkedList<>();
        queue.offer(root);
        int next_level_children = 0,to_be_print = 1;
        List<Node> nodes = new ArrayList<>();
        while (!queue.isEmpty()) {
            Node node = queue.poll();
            if (node.getDepth() == level) {
                nodes.add(node);
                continue;
            }
            if (node instanceof Element) {
                Element element = (Element)node;
                for (Node child : element.children()) {
                    queue.offer(child);
                    next_level_children++;
                }
            }
            to_be_print--;
            if (to_be_print == 0) {
                to_be_print = next_level_children;
                next_level_children = 0;
            }
        }
        return nodes.toArray(new Node[]{});
    }

    /**
     * 使用层序遍历，获取指定节点的所有子树节点
     *
     * @param parent 父节点
     * @return 以指定节点为根的子树的所有节点
     */
    private static List<Node> findAllNodes(Element parent, Predicate<Node> predicate){
        List<Node> result = new ArrayList<>();
        if(parent == null)
            return result;
        Queue<Node> queue = new LinkedList<>();
        queue.offer(parent);
        int nextLevel = 0, toBePrinted = 1;
        // 层序遍历...
        while(queue.peek() != null){
            Node node = queue.remove();
            if(predicate == null || predicate.test(node))
                result.add(node);

            if(node instanceof Element){
                for(Node child : ((Element)node).children()){
                    queue.offer(child);
                    nextLevel++;
                }
            }
            --toBePrinted;
            if(toBePrinted == 0){
                toBePrinted = nextLevel;
                nextLevel = 0;
            }
        }
        return result;
    }
}
