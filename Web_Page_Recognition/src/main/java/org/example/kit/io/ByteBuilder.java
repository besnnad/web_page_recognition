package org.example.kit.io;


import org.example.kit.Assert;
import org.example.kit.entity.ByteArray;

import java.util.Arrays;

/**
 * @author bonult
 */
public class ByteBuilder {
    private int count;
    private byte[] buf;
    private int zeroIndex = 0;

    public ByteBuilder(byte[] buf){
        Assert.notNull(buf);
        this.buf = buf;
        count = buf.length;
    }

    public ByteBuilder(int capacity){
        if(capacity < 0)
            throw new IllegalArgumentException("Negative initial capacity: " + capacity);
        buf = new byte[capacity];
        count = 0;
    }

    private void ensureCapacity(int minCapacity){
        if(minCapacity > buf.length)
            grow(minCapacity);
    }

    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private void grow(int minCapacity){
        int oldCapacity = buf.length;
        int newCapacity = oldCapacity << 1;
        if(newCapacity < minCapacity)
            newCapacity = minCapacity + (minCapacity >>> 1);
        if(newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        buf = Arrays.copyOf(buf, newCapacity);
    }

    private static int hugeCapacity(int minCapacity){
        if(minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
    }

    public ByteBuilder write(byte[] b){
        if(b == null || b.length == 0)
            return this;
        ensureCapacity(count + b.length);
        System.arraycopy(b, 0, buf, count, b.length);
        count += b.length;
        return this;
    }

    public ByteBuilder write(int b){
        ensureCapacity(count + 1);
        buf[count++] = (byte)b;
        return this;
    }

    public byte replace(int index, byte newByte){
        index += zeroIndex;
        if(index >= count || index < 0)
            indexOutOfBound(index);
        byte old = buf[index];
        buf[index] = newByte;
        return old;
    }

    public short replaceShort(int index, short newShort){
        index += zeroIndex;
        if(index >= count || index < 0)
            indexOutOfBound(index);
        short old = (short)((buf[index] << 8) | buf[index + 1]);
        buf[index] = (byte)(newShort >> 8);
        buf[index + 1] = (byte)newShort;
        return old;
    }

    public byte get(int index){
        index += zeroIndex;
        if(index >= count || index < 0)
            indexOutOfBound(index);
        return buf[index];
    }

    public ByteBuilder write(byte[] b, int offset, int length){
        if(b == null){
            throw new NullPointerException();
        }else if((offset < 0) || (offset > b.length) || (length < 0) || ((offset + length) > b.length) || ((offset + length) < 0)){
            throw new IndexOutOfBoundsException();
        }else if(length == 0){
            return this;
        }
        ensureCapacity(count + length);
        System.arraycopy(b, offset, buf, count, length);
        count += length;
        return this;
    }

    public int size(){
        return count - zeroIndex;
    }

    public int count(){
        return count;
    }

    public ByteBuilder writeInt(int num){
        ensureCapacity(count + 4);
        for(int i = 24; i >= 0; i -= 8)
            buf[count++] = (byte)(num >>> i);
        return this;
    }

    public ByteBuilder writeLong(long num){
        ensureCapacity(count + 8);
        for(int i = 56; i >= 0; i -= 8)
            buf[count++] = (byte)(num >>> i);
        return this;
    }

    public ByteBuilder writeShort(int num){
        ensureCapacity(count + 2);
        buf[count++] = (byte)(num >>> 8);
        buf[count++] = (byte)num;
        return this;
    }

    public ByteBuilder write(byte num){
        ensureCapacity(count + 1);
        buf[count++] = num;
        return this;
    }

    public ByteBuilder setStartTag(){
        zeroIndex = count;
        return this;
    }

    public ByteBuilder setStartTag(int zeroIndex){
        if(zeroIndex > count || zeroIndex < 0)
            indexOutOfBound(zeroIndex);
        this.zeroIndex = zeroIndex;
        return this;
    }

    public ByteBuilder removeStartTag(){
        zeroIndex = 0;
        return this;
    }

    public void reset(){
        count = 0;
        zeroIndex = 0;
    }

    public byte[] getBytes(){
        return count == buf.length && zeroIndex == 0 ? buf : Arrays.copyOfRange(buf, zeroIndex, count);
    }

    public ByteArray toByteArray(){
        return new ByteArray(buf, zeroIndex, count);
    }

    public byte[] subBytes(int beginIndex, int endIndex){
        return Arrays.copyOfRange(buf, beginIndex + zeroIndex, endIndex + zeroIndex);
    }

    public ByteArray subByteArray(int beginIndex, int endIndex){
        return new ByteArray(buf, beginIndex + zeroIndex, endIndex + zeroIndex);
    }

    private void indexOutOfBound(int index){
        throw new ArrayIndexOutOfBoundsException("Array index \"" + (index - zeroIndex) + "\" out of bound [" + 0 + ", " + (count - zeroIndex - 1) + "]");
    }

    public short lastShort(){
        if(count - zeroIndex < 2)
            indexOutOfBound(count - 2);
        return (short)(((buf[count - 2] & 0x0FF) << 8) | (buf[count - 1] & 0x0FF));
    }

    public int lastInt(){
        if(count - zeroIndex < 4)
            indexOutOfBound(count - 4);
        int result = buf[count - 4] << 24;
        result |= (buf[count - 3] & 0x0FF) << 16;
        result |= (buf[count - 2] & 0x0FF) << 8;
        result |= buf[count - 1] & 0x0FF;
        return result;
    }
    public byte[] lastBytes(int byteCount){
        if(count - zeroIndex < byteCount || byteCount < 0)
            indexOutOfBound(count - byteCount);
        return Arrays.copyOfRange(buf, count - byteCount, count);
    }

    public void delete(int beginIndex, int endIndex){
        beginIndex += zeroIndex;
        if(beginIndex > count || beginIndex < 0)
            indexOutOfBound(beginIndex);
        endIndex += zeroIndex;
        if(endIndex > count || endIndex < 0)
            indexOutOfBound(endIndex);
        if(beginIndex >= endIndex)
            return;
        int len = endIndex - beginIndex;
        for(int i = endIndex; i < count; i++){
            buf[i - len] = buf[i];
        }
        count -= len;
        if(zeroIndex >= endIndex)
            zeroIndex -= len;
        else if(zeroIndex > beginIndex)
            zeroIndex -= zeroIndex - beginIndex;
    }
}
