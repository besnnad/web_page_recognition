package org.example.sql.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.sql.model.*;

import java.util.List;
public interface MatchMapper {

    List<IndexResult> getCandidateSetByWords(@Param("list") List<Long> words, @Param("threshold") Integer threshold);

    List<Fingerprint> selectFingerprintsByPageIds(@Param("list") List<Integer> pageIds);

    Fingerprint selectFingetprintByPageId(int page_id);

    List<InvertedIndex> selectFeatureWordsByPageID(int page_id);

    String selectHostByIp(@Param("ip") String ip);

    String selectUrlByPageID(@Param("id") int pageId);

    List<PagetoUrl> selectAllPagetoUrl();

    Website selectWebsiteByName(String name);

    void insertFingerprints(@Param("list") List<Fingerprint> fps);

    void insertFeatureWords(@Param("list") List<InvertedIndex> fps);

    void insertIptoHost(IptoHost iptoHost);

    void insertPagetoUrl(@Param("list") List<PagetoUrl> pagetoUrls);

    void insertWebsite(@Param("list") List<Website> websites);

    void insertOneWebsite(Website website);

    // 一次读入内存
    List<Fingerprint> selectFingerprint();

    List<InvertedIndex> selectFeatureWords();

    void deleteFpById(int page_id);

    void deleteFeatureWordById(int page_id);

    void deletePagetoUrlById(int page_id);

    // 统计
    List<IndexResult> selectFeatureWordsCount();
}
