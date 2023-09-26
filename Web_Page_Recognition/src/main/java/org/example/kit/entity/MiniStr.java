package org.example.kit.entity;

import java.util.Arrays;

/**
 * Only support UTF-8.
 */
public class MiniStr {
    public static final int ORIGIN = 0;
    public static final int LOWER_CASE = 1;
    public static final int UPPER_CASE = 2;

    private byte[] value;
    private int hashcode;

    public MiniStr(byte[] value, int _case){
        int hash = 1;
        if(_case == LOWER_CASE){
            for(int i = 0; i < value.length; i++){
                byte v = value[i];
                if(v >= 'A' && v <= 'Z'){
                    v = (byte)(v + 32);
                    value[i] = v;
                }
                hash = 31 * hash + v;
            }
        }else if(_case == UPPER_CASE){
            for(int i = 0; i < value.length; i++){
                byte v = value[i];
                if(v >= 'a' && v <= 'z'){
                    v = (byte)(v - 32);
                    value[i] = v;
                }
                hash = 31 * hash + v;
            }
        }else
            this.hashcode = Arrays.hashCode(value);
        this.value = value;
    }

    public MiniStr(String str, int _case){
        this(str.getBytes(), _case);
    }

    @Override
    public boolean equals(Object o){
        if(o == null || getClass() != o.getClass())
            return false;
        MiniStr that = (MiniStr)o;
        int n = value.length;
        if(n == that.value.length){
            byte v1[] = value;
            byte v2[] = that.value;
            while(--n > -1){
                if(v1[n] != v2[n])
                    return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode(){
        return hashcode;
    }

    @Override
    public String toString(){
        return new String(value);
    }

    public byte[] getBytes(){return value;}
}
