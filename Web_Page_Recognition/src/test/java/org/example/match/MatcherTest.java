package org.example.match;

import org.example.uitl.FilePath;
import org.example.kit.FileKit;
import org.example.sql.conn.ConnectToMySql;
import org.example.work.match.Extract;
import org.example.work.match.MatchResult;
import org.example.work.match.MatchTask;
import org.example.work.match.Matcher;
import org.example.work.flow.TrafficAnalysis;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * @CLassname MatcherTest
 * @Description TODO
 * @Date 2021/3/9 12:35
 * @Created by lenovo
 */
public class MatcherTest {
    @Test
    public void testMatcher() {
        MatchTask matchTask = new MatchTask();
        matchTask.setHost("jpg2pdf.org");
//        matchTask.setPath("/hotels-disneyland-paris/");
        Extract.crawl(matchTask);
        Matcher matcher = new Matcher();
        MatchResult matchResult = matcher.match(matchTask);
        System.out.println("match result : " + matchResult.isSuccess());
        if (matchResult.isSuccess()) {
            System.out.println("Page id : " + matchResult.getWebPageId());
            ConnectToMySql conn = new ConnectToMySql();
            String url = conn.getMatchMapper().selectUrlByPageID(matchResult.getWebPageId());
            System.out.println("query result : " + url);
        }
    }

    @Test
    public void test() {
        byte[] asd = new byte[]{0x01,0x01};
//        System.out.println(TrafficAnalysis.getShort(asd));
        byte test = 0x66;
//        System.out.println((char) test);
        char res = (char) test;
        System.out.println((int) test);
    }

    @Test
    public void testParseClientHello() {
        try {
            byte[] packet = FileKit.getAllBytes(new File(FilePath.ROOT_PATH + "test\\data.pcap"));
//            System.out.println(new String(packet));
            for (int i = 0; i < packet.length; i++) {
                if (i % 16 == 0) System.out.println();
                System.out.printf("%02x ",packet[i]);
            }
            System.out.println();
            MatchTask matchTask = new MatchTask();
            TrafficAnalysis.clientHelloAnalysis(packet,matchTask);
            System.out.println(matchTask.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
