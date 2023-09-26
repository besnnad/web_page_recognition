package org.example.work.match;

import org.example.sql.conn.ConnectToMySql;
import org.example.sql.model.Fingerprint;
import org.example.sql.model.InvertedIndex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @CLassname NewMatcher
 * @Description TODO
 * @Date 2021/3/16 11:03
 * @Created by lenovo
 */
public class NewMatcher extends Matcher{
    private static final ConnectToMySql conn = new ConnectToMySql();
    private static final List<Fingerprint> ALL_FINGERPRINTS = conn.getMatchMapper().selectFingerprint();
    private static final List<InvertedIndex> ALL_WORDS = conn.getMatchMapper().selectFeatureWords();
    private static final List<PageRecord> pages = new ArrayList<>();

    static  {
        System.out.println("Sort words");
        ALL_WORDS.sort(Comparator.comparingInt(InvertedIndex::getPageId));
        System.out.println("Sort fingerprint");
        ALL_FINGERPRINTS.sort(Comparator.comparingInt(Fingerprint::getPageId));
        Collections.sort(ALL_FINGERPRINTS);
        for (int i = 0; i < ALL_FINGERPRINTS.size(); i++) {
            int page_id = ALL_FINGERPRINTS.get(i).getPageId();
            for (int j = 0; j < ALL_WORDS.size(); j++) {
                List<InvertedIndex> words = new ArrayList<>();
                if (ALL_WORDS.get(j).getPageId() != page_id) {
                    PageRecord pageRecord = new PageRecord();
                    pageRecord.setPageID(page_id);
                    pageRecord.setFp(ALL_FINGERPRINTS.get(i));
                    pageRecord.setWords(words);
                    pages.add(pageRecord);
                    words = new ArrayList<>();
                    i++;
                    page_id = ALL_FINGERPRINTS.get(i).getPageId();
                }
                words.add(ALL_WORDS.get(j));
            }
        }
    }


    @Override
    public MatchResult match(MatchTask identifiedPage) {
        return null;
    }

    public static void main(String[] args) {
        NewMatcher newMatcher = new NewMatcher();
        MatchTask matchTask = new MatchTask();
        matchTask.setHost("voetbalvlaanderen.be");
//        matchTask.setPath("/hotels-disneyland-paris/");
        Extract.crawl(matchTask);
        Matcher matcher = new Matcher();
        MatchResult matchResult = newMatcher.match(matchTask);
        if (matchResult == null) {
            System.out.println("shibai ");
            return;
        }
        System.out.println("match result : " + matchResult.isSuccess());
        if (matchResult.isSuccess()) {
            System.out.println("Page id : " + matchResult.getWebPageId());
            ConnectToMySql conn = new ConnectToMySql();
            String url = conn.getMatchMapper().selectUrlByPageID(matchResult.getWebPageId());
            System.out.println("query result : " + url);
        }
    }
}
