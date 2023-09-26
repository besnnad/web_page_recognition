package org.example.kit.entity;


import org.example.kit.Assert;
import org.example.kit.io.ByteBuilder;

import java.util.Arrays;

/**
 * @author bonult
 */
public class ByteArray {
    private byte[] bytes;
    private int from;
    private int to; // 不包括该位置

    /**
     * 字节数组，请确保 [from, to) 为 [0, bytes.length) 的子区间
     *
     * @param bytes 数组
     * @param from  起始位置
     * @param to    结束位置，数据区间为 [from, to - 1]
     */
    public ByteArray(byte[] bytes, int from, int to){
        if(from > to){
            throw new IllegalArgumentException("from[" + from + "] 大于 to[" + to + "]");
        }
        this.bytes = bytes;
        this.from = from;
        this.to = to;
    }

    public ByteArray(byte[] bytes){
        Assert.notNull(bytes);
        this.bytes = bytes;
        this.from = 0;
        this.to = bytes.length;
    }

    public byte[] getBytes(){
        return Arrays.copyOfRange(bytes, from, to);
    }

    public byte[] getParentBytes(){
        return bytes;
    }

    public byte get(int index){
        return bytes[from + index];
    }

    public int length(){
        return to - from;
    }

    public int from(){
        return from;
    }

    public int to(){
        return to;
    }

    @Override
    public boolean equals(Object o){
        if(o == null || getClass() != o.getClass())
            return false;
        ByteArray that = (ByteArray)o;
        int n = to - from;
        if(n == that.to - that.from){
            byte v1[] = bytes;
            byte v2[] = that.bytes;
            int i = 0;
            while(n-- != 0){
                if(v1[from + i] != v2[that.from + i])
                    return false;
                i++;
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode(){
        int result = 1;
        for(int i = from; i < to; i++)
            result = 31 * result + bytes[i];
        return result;
    }

    public boolean equals(String str){
        if(str == null || to - from != str.length())
            return false;
        for(int i = to - from - 1; i >= 0; i--){
            if((bytes[i + from] & 0x0FF) != str.charAt(i))
                return false;
        }
        return true;
    }

    public String display(int rows){
        if(to == from)
            return "";
        StringBuilder sb = new StringBuilder();
        int rowLen = 16, rowNum = 0;
        for(int i = from, j; i < to; i += rowLen){
            String h = Integer.toHexString(rowNum).toUpperCase();
            for(j = (rowNum > Short.MAX_VALUE ? 8 : 4) - h.length(); j > 0; j--){
                sb.append('0');
            }
            sb.append(h).append("   ");
            for(j = i; j < Math.min(i + rowLen, to); j++){
                String hex = Integer.toHexString(bytes[j] & 0x0FF).toUpperCase();
                sb.append(hex.length() == 1 ? 0 + hex : hex);
                sb.append(' ');
            }
            for(j = rowLen - j + i; j > 0; j--){
                sb.append("   ");
            }
            sb.append("  ");
            for(j = i; j < Math.min(i + rowLen, to); j++){
                int b = bytes[j] & 0x0FF;
                if(b > 32 && b < 127)
                    sb.append((char)b);
                else
                    sb.append('.');
            }
            sb.append("\r\n");
            rowNum++;
            if(rowNum == rows)
                break;
        }
        return sb.toString();
    }

    public String display(){
        return display(Integer.MAX_VALUE);
    }

    public String toStr(){
        return new String(bytes, from, to - from);
    }

    public String subString(int from, int to){
        return new String(bytes, from + this.from, to - from);
    }

    @Override
    public String toString(){
        return toStr();
    }

    public int indexOf(byte target, int fromIndex){
        for(int i = from + fromIndex; i < to; i++){
            if(bytes[i] == target)
                return i - from;
        }
        return -1;
    }

    public static String toStr(ByteArray array){
        return array == null ? null : array.toStr();
    }

    public int indexOf(byte target){
        return indexOf(target, 0);
    }

    /**
     * TODO修改：target 达到一定长度时使用KMP算法
     *
     * @param target    目标数组
     * @param fromIndex 查询起始位置
     * @return 目标第一个元素的下标
     */
    public int indexOf(byte[] target, int fromIndex){
        if(target == null)
            return -1;
        fromIndex += from;
        byte tag = target[0];
        while(fromIndex < to){
            while(fromIndex < to && bytes[fromIndex] != tag)
                fromIndex++;
            if(fromIndex >= to)
                break;
            if(to - fromIndex < target.length)
                break;
            int i = 1;
            while(i < target.length && target[i] == bytes[fromIndex + i])
                ++i;
            if(i == target.length)
                return fromIndex - from;
            fromIndex++;
        }
        return -1;
    }

    public int indexOf(byte[] target){
        return indexOf(target, 0);
    }

    private void indexOutOfBound(int index){
        throw new ArrayIndexOutOfBoundsException("Array index " + (index - from) + " out of bound [0, " + (to - from - 1) + "]");
    }

    public byte[] subBytes(int beginIndex, int endIndex){
        return Arrays.copyOfRange(bytes, beginIndex + from, endIndex + from);
    }

    public byte[] subBytes(int beginIndex){
        return Arrays.copyOfRange(bytes, beginIndex + from, to);
    }

    public ByteArray subByteArray(int beginIndex, int endIndex){
        return new ByteArray(bytes, beginIndex + from, endIndex + from);
    }

    public ByteArray subByteArray(int beginIndex){
        return new ByteArray(bytes, beginIndex + from, to);
    }

    public boolean startWith(byte... startBytes){
        if(startBytes.length > to - from)
            return false;
        for(int i = 0; i < startBytes.length; i++){
            if(startBytes[i] != bytes[from + i])
                return false;
        }
        return true;
    }

    public boolean startWith(String start){
        if(start == null || start.length() > to - from)
            return false;
        for(int i = 0; i < start.length(); i++){
            if(start.charAt(i) != bytes[from + i])
                return false;
        }
        return true;
    }

    public byte[] replace(byte[] oldBytes, byte[] newBytes){
        int index = indexOf(oldBytes, 0);
        if(index < 0)
            return getBytes();
        int count = 1;
        while(true){
            index = indexOf(oldBytes, index + oldBytes.length);
            if(index >= 0)
                count++;
            else
                break;
        }
        byte[] result = new byte[length() - count * (oldBytes.length - newBytes.length)];
        int i = 0;
        count = 0;
        index = indexOf(oldBytes, 0);
        while(true){
            while(i < index)
                result[count++] = bytes[from + i++];
            if(i >= length())
                break;
            System.arraycopy(newBytes, 0, result, count, newBytes.length);
            count += newBytes.length;
            i = i + oldBytes.length;
            index = indexOf(oldBytes, i);
            if(index < 0)
                index = length();
        }
        return result;
    }

    public boolean equalsIgnoreCase(int start, int end, byte[] target){
        if(end - start != target.length)
            return false;
        start += from;
        for(int i = 0; i < target.length; i++, start++){
            byte v = bytes[start];
            if(v == target[i])
                continue;
            if((v >= 'A' && v <= 'Z' && v + 32 == target[i]) || (v >= 'a' && v <= 'z' && v - 32 == target[i])){
                continue;
            }
            return false;
        }
        return true;
    }

    public boolean handleUnicodeIdentifier(){
        if(bytes[from] != -1 || bytes[from + 1] != -2)
            return false;
        ByteBuilder builder = new ByteBuilder(length() - 2);
        for(int i = from + 2; i < to; i++){
            byte b = bytes[i];
            if(b == 0)
                continue;
            builder.write(b);
        }
        bytes = builder.getBytes();
        from = 0;
        to = bytes.length;
        return true;
    }
}
