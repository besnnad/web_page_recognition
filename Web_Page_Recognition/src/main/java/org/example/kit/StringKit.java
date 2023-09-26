package org.example.kit;

import java.io.UnsupportedEncodingException;

/**
 * A collection of commonly used String tools of java
 *
 * @author bonult
 */
public class StringKit {

    /**
     * You can't call the constructor.
     */
    private StringKit(){
    }

    /**
     * Whether the string is empty or null.
     */
    public static boolean isEmpty(String str){
        return null == str || str.length() == 0;
    }

    /**
     * Whether the string is not empty neither null.
     */
    public static boolean isNotEmpty(String str){
        return !isEmpty(str);
    }

    /**
     * If a string is empty or null, then return a null value.
     */
    public static String notEmpty(String str){
        if(isEmpty(str)){
            return null;
        }
        return str;
    }

    /**
     * If a string is null then return a empty value.
     */
    public static String notNull(Object s){
        if(null == s){
            return "";
        }
        return s.toString();
    }

    public static String toIPv4String(byte[] ip){
        StringBuilder sb = new StringBuilder(15);
        if(ip.length != 4)
            throw new IllegalArgumentException();
        for(int i = 0; i < ip.length; i++){
            if(i > 0)
                sb.append('.');
            sb.append(ip[i] & 0x0FF);
        }
        return sb.toString();
    }

    public static void toLowerCase(byte[] str){
        for(int i = 0; i < str.length; i++){
            byte v = str[i];
            if(v >= 'A' && v <= 'Z'){
                v = (byte)(v + 32);
                str[i] = v;
            }
        }
    }

    public static String toLowerCase(String str){
        byte[] bs = str.getBytes();
        toLowerCase(bs);
        return new String(bs);
    }


    /**
     * byte数组转换为Stirng
     * @param s1-数组
     * @param encode-字符集
     * @return 对应字符串,异常返回null
     */
    public static String getString(byte[] s1,String encode) {
        String result;
        try {
            result =  new String(s1, encode);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        return result;
    }
}
