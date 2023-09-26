package org.example.work.parse;

import org.example.kit.entity.ByteArray;
import org.example.kit.StringKit;

/**
 * 标签属性定义
 */
public class Attribute {
    byte[] key;
    ByteArray value;

    public Attribute(byte[] key, ByteArray value){
        StringKit.toLowerCase(key);
        this.key = key;
        this.value = value;
    }
    Attribute(){ }

    public byte[] getKey(){
        return key;
    }
    public ByteArray getValue(){
        return value;
    }
    @Override
    public String toString(){
        return new String(key) + (value == null ? "" : ("=\"" + value.toStr() + "\""));
    }

    public boolean isKey(String str){
        if(str == null || key.length != str.length())
            return false;
        for(int i = 0; i < key.length; i++){
            if((key[i] & 0x0FF) != str.charAt(i))
                return false;
        }
        return true;
    }

    public boolean isValue(String str){
        return value == null ? str == null : value.equals(str);
    }
}
