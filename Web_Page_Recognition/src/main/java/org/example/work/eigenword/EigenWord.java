package org.example.work.eigenword;

/**
 * @Classname EigenWord
 * @Description 特征词类W 网页Pi 特征向量（W1，W2,...Wi） 值取词频
 *          [4bits 标志位][60bits 内容]
 * @Date 2020/11/11 15:01
 * @Created by shuaif
 */
public class EigenWord implements Comparable<EigenWord>{
    private final long word; // 特征值
    private byte index; // 特征值在网页特征向量中的索引
    private byte frequency; // 词频

    public EigenWord(long word){
        this(word, 1, 0);
    }

    public EigenWord(long word, int frequency, int index){
        assert index >= 0;
        this.word = word;
        this.frequency = (byte)(Math.min(frequency, 0xFF));
        this.index = (byte)index;
    }

    public long getWord(){
        return word;
    }
    public int getIndex(){
        return index & 0xFF;
    }
    public int getFrequency(){
        return frequency & 0xFF;
    }
    public void addFrequency(int fre){
        fre += frequency & 0xFF;
        if(fre > 0xFF)
            frequency = (byte)0xFF;
        else
            frequency = (byte)fre;
    }
    public void addFrequency(){
        if((frequency & 0xFF) < 0xFF)
            frequency = (byte)((frequency & 0xFF) + 1);
    }

    public void setIndex(int index) {
        this.index = (byte) index;
    }

    @Override
    public int compareTo(EigenWord o) {
        return Integer.compare(index & 0xFF, o.index & 0xFF);
    }

    @Override
    public String toString(){
        return word + "," + (index & 0xFF) + "," + (frequency & 0xFF);
    }
}
