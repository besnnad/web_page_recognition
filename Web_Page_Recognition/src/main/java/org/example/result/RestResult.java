package org.example.result;

import org.example.sql.model.Fingerprint;
import org.example.sql.model.InvertedIndex;

import java.util.List;

/**
 * @CLassname RestResult
 * @Description TODO
 * @Date 2021/5/27 11:12
 * @Created by lenovo
 */
public class RestResult {
    private String url;
    private byte[] content;
    private List<InvertedIndex> words;
    private Fingerprint fingerprint;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public List<InvertedIndex> getWords() {
        return words;
    }

    public void setWords(List<InvertedIndex> words) {
        this.words = words;
    }

    public Fingerprint getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(Fingerprint fingerprint) {
        this.fingerprint = fingerprint;
    }
}
