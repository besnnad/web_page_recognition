package org.example.service;

import org.example.result.RestResult;
import org.example.sql.conn.ConnectToMySql;
import org.example.sql.mapper.MatchMapper;
import org.example.sql.model.Fingerprint;
import org.example.sql.model.InvertedIndex;
import org.example.sql.model.PagetoUrl;
import org.example.uitl.Util;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @CLassname ServiceImpl
 * @Description TODO
 * @Date 2021/5/27 11:53
 * @Created by lenovo
 */
@Service
public class ServiceImpl {
//    @Resource
//    private MatchMapper matchMapper = null;
    private ConnectToMySql conn = new ConnectToMySql();
    private MatchMapper matchMapper = conn.getMatchMapper();

    public RestResult details(int page_id) {
        Fingerprint fp = this.matchMapper.selectFingetprintByPageId(page_id);
        List<InvertedIndex> words = this.matchMapper.selectFeatureWordsByPageID(page_id);
        RestResult rest = new RestResult();
        rest.setFingerprint(fp);
        rest.setContent(fp.getFpdata());
        rest.setUrl(this.matchMapper.selectUrlByPageID(page_id));
        rest.setWords(words);
        return rest;
    }


    private List<PagetoUrl> ptous;
    public void delete() {
        ptous = this.matchMapper.selectAllPagetoUrl();
        for (PagetoUrl pagetoUrl : ptous) {
            String url = pagetoUrl.getUrl();
            if(Util.urlHasPath(url)) {
                System.out.println(url);
                this.matchMapper.deletePagetoUrlById(pagetoUrl.getPageId());
                this.matchMapper.deleteFpById(pagetoUrl.getPageId());
                this.matchMapper.deleteFeatureWordById(pagetoUrl.getPageId());
                ptous.remove(pagetoUrl);
            }
        }
    }

}
