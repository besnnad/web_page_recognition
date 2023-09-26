package org.example.sql.model;

import java.io.Serializable;

/**
 * 倒排索引，
 */
public class InvertedIndex implements Serializable , Comparable<InvertedIndex>{

    private static final long serialVersionUID = -1L;

    private long word; // 特征词
    private Integer pageId;
    private int wordIndex;
    private int frequency;

    public int getWordIndex() {
        return wordIndex;
    }

    public void setWordIndex(int wordIndex) {
        this.wordIndex = wordIndex;
    }



    public long getWord(){
        return word;
    }
    public void setWord(long word){
        this.word = word;
    }
    public Integer getPageId(){
        return pageId;
    }
    public void setPageId(Integer pageId){
        this.pageId = pageId;
    }
    public int getIndex(){
        return wordIndex;
    }
    public void setIndex(int index){
        this.wordIndex = index;
    }
    public int getFrequency(){
        return frequency;
    }
    public void setFrequency(int frequency){
        this.frequency = frequency;
    }

    @Override
    public String toString() {
        return "[" + word + ", " + pageId + ", " + wordIndex + "," + frequency + "]";
    }

    @Override
    public int compareTo(InvertedIndex o) {
        return Integer.compare(wordIndex,o.getIndex());
    }
}
