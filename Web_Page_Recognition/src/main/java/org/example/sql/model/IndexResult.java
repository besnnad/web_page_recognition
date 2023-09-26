package org.example.sql.model;

public class IndexResult implements Comparable<IndexResult> {
    private int pageId;
    private int count; // 特征词数量

    public int getPageId(){
        return pageId;
    }
    public void setPageId(int pageId){
        this.pageId = pageId;
    }
    public int getCount(){
        return count;
    }
    public void setCount(int count){
        this.count = count;
    }

    @Override
    public int compareTo(IndexResult o){
        return Integer.compare(o.count, count);
    }
}
