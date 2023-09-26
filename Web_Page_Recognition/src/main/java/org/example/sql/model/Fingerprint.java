package org.example.sql.model;



import java.sql.Timestamp;
import java.util.Arrays;

public class Fingerprint implements Comparable<Fingerprint> {

    private Integer pageId;
    private byte[] fpdata;
    private Timestamp lastUpdate;

    private double similarity;

    public Integer getPageId(){
        return pageId;
    }
    public void setPageId(Integer pageId){
        this.pageId = pageId;
    }
    public byte[] getFpdata(){
        return fpdata;
    }
    public void setFpdata(byte[] fpdata){
        this.fpdata = fpdata;
    }
    public Timestamp getLastUpdate(){
        return lastUpdate;
    }
    public void setLastUpdate(Timestamp lastUpdate){
        this.lastUpdate = lastUpdate;
    }

    public double getSimilarity(){
        return similarity;
    }
    public void setSimilarity(double similarity){
        this.similarity = similarity;
    }

    @Override
    public int compareTo(Fingerprint o){
        return Double.compare(o.similarity, similarity);
    }

    @Override
    public String toString() {
        return "["+ pageId + ",  " + similarity + "]\n" + "fpdata : " + Arrays.toString(fpdata);
    }
}
