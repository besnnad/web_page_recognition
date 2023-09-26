package org.example.other;

import org.example.work.crawl.WebCrawl;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @CLassname PageTest
 * @Description TODO
 * @Date 2021/3/25 19:13
 * @Created by lenovo
 */
public class PageTest {
    @Test
    public void jsoupTest() {
        String script = "<script type=\"text/javascript\">\n" +
                "        window.search = function (a) {\n" +
                "            var b, c = \"//search.jd.com/Search?keyword={keyword}&enc={enc}{additional}\";\n" +
                "            var d = search.additinal || \"\";\n" +
                "            var e = document.getElementById(a);\n" +
                "            var f = e.value;\n" +
                "            if (f = f.replace(/^\\s*(.*?)\\s*$/, \"$1\"), f.length > 100 && (f = f.substring(0, 100)), \"\" == f) return void (window.location.href = window.location.href);\n" +
                "            var g = 0;\n" +
                "            \"undefined\" != typeof window.pageConfig && \"undefined\" != typeof window.pageConfig.searchType && (g = window.pageConfig.searchType);\n" +
                "            var h = \"&cid{level}={cid}\";\n" +
                "            var i = \"string\" == typeof search.cid ? search.cid : \"\";\n" +
                "            var j = \"string\" == typeof search.cLevel ? search.cLevel : \"\";\n" +
                "            var k = \"string\" == typeof search.ev_val ? search.ev_val : \"\";\n" +
                "            switch (g) {\n" +
                "                case 0:\n" +
                "                    break;\n" +
                "                case 1:\n" +
                "                    j = \"-1\", d += \"&book=y\";\n" +
                "                    break;\n" +
                "                case 2:\n" +
                "                    j = \"-1\", d += \"&mvd=music\";\n" +
                "                    break;\n" +
                "                case 3:\n" +
                "                    j = \"-1\", d += \"&mvd=movie\";\n" +
                "                    break;\n" +
                "                case 4:\n" +
                "                    j = \"-1\", d += \"&mvd=education\";\n" +
                "                    break;\n" +
                "                case 5:\n" +
                "                    var l = \"&other_filters=%3Bcid1%2CL{cid1}M{cid1}[cid2]\";\n" +
                "                    switch (j) {\n" +
                "                        case \"51\":\n" +
                "                            h = l.replace(/\\[cid2]/, \"\"), h = h.replace(/\\{cid1}/g, \"5272\");\n" +
                "                            break;\n" +
                "                        case \"52\":\n" +
                "                            h = l.replace(/\\{cid1}/g, \"5272\"), h = h.replace(/\\[cid2]/, \"%3Bcid2%2CL{cid}M{cid}\");\n" +
                "                            break;\n" +
                "                        case \"61\":\n" +
                "                            h = l.replace(/\\[cid2]/, \"\"), h = h.replace(/\\{cid1}/g, \"5273\");\n" +
                "                            break;\n" +
                "                        case \"62\":\n" +
                "                            h = l.replace(/\\{cid1}/g, \"5273\"), h = h.replace(/\\[cid2]/, \"%3Bcid2%2CL{cid}M{cid}\");\n" +
                "                            break;\n" +
                "                        case \"71\":\n" +
                "                            h = l.replace(/\\[cid2]/, \"\"), h = h.replace(/\\{cid1}/g, \"5274\");\n" +
                "                            break;\n" +
                "                        case \"72\":\n" +
                "                            h = l.replace(/\\{cid1}/g, \"5274\"), h = h.replace(/\\[cid2]/, \"%3Bcid2%2CL{cid}M{cid}\");\n" +
                "                            break;\n" +
                "                        case \"81\":\n" +
                "                            h = l.replace(/\\[cid2]/, \"\"), h = h.replace(/\\{cid1}/g, \"5275\");\n" +
                "                            break;\n" +
                "                        case \"82\":\n" +
                "                            h = l.replace(/\\{cid1}/g, \"5275\"), h = h.replace(/\\[cid2]/, \"%3Bcid2%2CL{cid}M{cid}\")\n" +
                "                    }\n" +
                "                    c = \"//search-e.jd.com/searchDigitalBook?ajaxSearch=0&enc=utf-8&key={keyword}&page=1{additional}\";\n" +
                "                    break;\n" +
                "                case 6:\n" +
                "                    j = \"-1\", c = \"//music.jd.com/8_0_desc_0_0_1_15.html?key={keyword}\";\n" +
                "                    break;\n" +
                "                case 7:\n" +
                "                    c = \"//s-e.jd.com/Search?key={keyword}&enc=utf-8\";\n" +
                "                    break;\n" +
                "                case 8:\n" +
                "                    c = \"//search.jd.hk/Search?keyword={keyword}&enc=utf-8\";\n" +
                "                    break;\n" +
                "                case 9:\n" +
                "                    d += \"&market=1\"\n" +
                "            }\n" +
                "            if (\"string\" == typeof i && \"\" != i && \"string\" == typeof j) {\n" +
                "                var m = /^(?:[1-8])?([1-3])$/;\n" +
                "                j = \"-1\" == j ? \"\" : m.test(j) ? RegExp.$1 : \"\";\n" +
                "                var n = h.replace(/\\{level}/, j);\n" +
                "                n = n.replace(/\\{cid}/g, i), d += n\n" +
                "            }\n" +
                "            if (\"string\" == typeof k && \"\" != k && (d += \"&ev=\" + k), f = encodeURIComponent(f), b = c.replace(/\\{keyword}/, f), b = b.replace(/\\{enc}/, \"utf-8\"), b = b.replace(/\\{additional}/, d), \"object\" == typeof $o && (\"string\" == typeof $o.lastKeyword && (b += \"&wq=\" + encodeURIComponent($o.lastKeyword)), \"string\" == typeof $o.pvid && (b += \"&pvid=\" + $o.pvid)), b.indexOf(\"/search.jd.com/\") > 0) try {\n" +
                "                JA.tracker.ngloader(\"search.000009\", {key: f, posid: a, target: b})\n" +
                "            } catch (o) {\n" +
                "            }\n" +
                "            (\"undefined\" == typeof search.isSubmitted || 0 == search.isSubmitted) && (setTimeout(function () {\n" +
                "                window.location.href = b\n" +
                "            }, 50), search.isSubmitted = !0)\n" +
                "        };\n" +
                "    </script>";
        Document document = Jsoup.parse(script);
    }

    @Test
    public void keywordTest() {
        {
            String URL = "http://www.hit.edu.cn/";
            Document document = WebCrawl.webCrawl(URL);
            Queue<Element> queue = new LinkedList<>();
//            System.out.println(document.html());
            queue.add(document.head());
            queue.add(document.body());
            StringBuilder keywords = new StringBuilder();
            while (!queue.isEmpty()) {
                Element element = queue.poll();
                keywords.append("<");
                keywords.append(element.tagName());
                keywords.append(">");
                keywords.append("</");
                keywords.append(element.tagName());
                keywords.append(">");
//            System.out.println(element.tagName());
                for (Attribute attribute : element.attributes()) {
                    keywords.append(attribute.getKey());
                    keywords.append("=''");
//                System.out.println(attribute.getKey());
                }
                if (element.tagName().equals("script")) {
                    keywords.append(element.data());
//                    System.out.println(element.data());
                }
                queue.addAll(element.children());
            }
            System.out.println(keywords.toString());
            System.out.println(keywords.length());
            System.out.println(document.html().length());
            System.out.println((double) keywords.length() / document.html().length());
        }
    }
}
