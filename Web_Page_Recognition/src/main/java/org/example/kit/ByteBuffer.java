package org.example.kit;


import org.example.kit.entity.ByteArray;

import java.util.Arrays;

public class ByteBuffer {

    private final byte[] buffer;
    private int position;
    private final int offset;
    private final int limit;

    private int mark;

    private ByteBuffer(byte[] buffer, int offset, int limit){
        this.buffer = buffer;
        this.position = offset;
        this.offset = offset;
        this.limit = limit;
    }

    public int offset(){
        return offset;
    }

    public int limit(){
        return limit;
    }

    public boolean isLeftBound(){
        return position == offset;
    }

    public boolean isLeftBound(int position){
        return position == offset;
    }

    public boolean isRightBound(){
        return position + 1 == limit;
    }

    public boolean isRightBound(int position){
        return position + 1 == limit;
    }

    public int position(){
        return position;
    }

    /**
     * 获取当前字节
     */
    public byte get(){
        return buffer[position];
    }

    private void indexOutOfBound(int index){
//        System.out.println("this in ByteBuffer\n" + new String(this.buffer));
        throw new ArrayIndexOutOfBoundsException("index " + index + " is out of [" + offset + ", " + limit + ")");
//        System.out.println("index " + index + " is out of [" + offset + ", " + limit + ")");

    }

    public byte get(int position){
        checkIndex(position);
        return buffer[position];
    }

    public byte getNext(){
        if(position + 1 >= limit)
            indexOutOfBound(position + 1);
        return buffer[position + 1];
    }

    public boolean hasNext(){
        return position + 1 < limit;
    }

    public void next(){
        if(++position >= limit){
            position--;
            indexOutOfBound(position + 1);
        }
    }

    /**
     * 可以继续解析
     * @return
     */
    public boolean canGo(){
        if(++position >= limit){
            position--;
            return false;
        }
        return true;
    }

    public byte pre(){
        if(--position < offset){
            position++;
            indexOutOfBound(position - 1);
        }
        return buffer[position];
    }

    public void position(int pos){
        checkIndex(pos);
        position = pos;
    }

    public byte[] array(){
        return buffer;
    }

    public void reset(){
        position = offset;
        mark = 0;
    }

    /**
     * 判断是否有下一个tag符号
     * @param tag
     * @return
     */
    public boolean hasNext(byte tag){
        while(position < limit && buffer[position] != tag)
            position++;
        return position < limit;
    }

    public boolean noNext(byte tag){
        while(position < limit && buffer[position] != tag)
            position++;
        if(++position >= limit){
            position--;
            return true;
        }
        return false;
    }

    public int moveTo(byte tag){
        while(position < limit && buffer[position] != tag)
            position++;
        if(position >= limit){
            position--;
            indexOutOfBound(limit);
        }
        return position;
    }

    /**
     * 移动到Tag之后的第一个位置
     * @param tag
     */
    public void moveAfter(byte tag){
        while(position < limit && buffer[position] != tag)
            position++;
        if(++position >= limit){
            position--;
            indexOutOfBound(limit);
        }
    }

    public void moveTo(byte... tag){
        while(position < limit){
            int t = moveTo(tag[0]);
            if(limit - t < tag.length){
                position = limit - 1;
                indexOutOfBound(position + 1);
            }
            int i = 1;
            while(i < tag.length && tag[i] == buffer[position + i])
                ++i;
            if(i == tag.length)
                break;
            position++;
        }
    }

    /**
     * 获取下一个非空白字符的下标
     */
    public int moveToUnblankChar(){
        byte c;
        while(position < limit && ((c = buffer[position]) < 33 && c >= 0))
            position++;
        if(position >= limit){
            position--;
            indexOutOfBound(position + 1);
        }
        return position;
    }

    /**
     * 移动到空白ASCII字符
      */
    public int moveToBlankChar(){
        byte c;
        while(position < limit && ((c = buffer[position]) > 32 || c < 0))
            position++;
        if(position >= limit){
            position--;
            indexOutOfBound(position + 1);
        }
        return position;
    }

    /**
     * 移动到空白字符或者下一个tag
     */
    public int moveUntilBlankCharOr(byte tag){
        byte c;
        while(position < limit && ((c = buffer[position]) > 32 || c < 0) && c != tag)
            position++;
        if(position >= limit){
            position--;
            indexOutOfBound(position + 1);
        }
        return position;
    }

    private void checkIndex(int index){
        if(index < offset || index >= limit)
            indexOutOfBound(index);
    }

    public void mark(){
        mark = position;
    }

    public void mark(int index){
        checkIndex(index);
        mark = index;
    }

    public int getMark(){
        return mark;
    }

    /**
     * 拷贝从当前位置到目标tag的字节数组
     * @param tag
     * @return
     */
    public byte[] copyFromCurrPosTo(byte tag){
        int start = position;
        while(position < limit && buffer[position] != tag)
            position++;
        int end = position;
        if(position >= limit){
            position--;
        }
        return Arrays.copyOfRange(buffer, start, end);
    }

    public byte[] copyOfRange(int from, int to){
        if(from < offset || to > limit)
            throw new IndexOutOfBoundsException();
        if(from == 0 && to == buffer.length)
            return buffer;
        return Arrays.copyOfRange(buffer, from, to);
    }

    public ByteArray wrapToByteArray(int from, int to){
        if(from < offset || to > limit)
            throw new IndexOutOfBoundsException();
        return new ByteArray(buffer, from, to);
    }

    public ByteArray wrapToByteArray(){
        return new ByteArray(buffer, offset, limit);
    }

    //    public ByteBuffer asReadOnlyBuffer(){
//        return null;
//    }
//    public byte set(byte b){
//        return 0;
//    }
//    public byte set(int position, byte b){
//        return 0;
//    }
//    public boolean isReadOnly(){
//        return false;
//    }
//
    public String nearbyString(){
        int from = Math.max(position - 1, 0), to = position + 1;
        while(buffer[from] != '\n' && from > 0)
            from--;
        while(to < limit && buffer[to] != '\n')
            to++;
        String left = from + 1 > position ? "" : new String(Arrays.copyOfRange(buffer, from == 0 ? 0 : from + 1, position));
        String right = position + 1 > to ? "" : new String(Arrays.copyOfRange(buffer, position + 1, to));
        return (left + ("\033[31;4m" + (buffer[position] == '\n' ? "\\n\n" : new String(new byte[]{
                buffer[position]
        })) + "\033[0m") + right);
    }

    @Override
    public String toString(){
        return nearbyString();
    }
    /**
     * Wraps a byte array into a buffer.
     *
     * <p> The new buffer will be backed by the given byte array;
     * that is, modifications to the buffer will cause the array to be modified
     * and vice versa.  The new buffer's position will be <tt>offset</tt>, its limit
     * will be <tt>offset + length</tt>. </p>
     *
     * @param array  The array that will back the new buffer
     * @param offset The offset of the subarray to be used; must be non-negative and
     *               no larger than <tt>array.length</tt>.  The new buffer's position
     *               will be set to this value.
     * @param length The length of the subarray to be used;
     *               must be non-negative and no larger than
     *               <tt>array.length - offset</tt>.
     *               The new buffer's limit will be set to <tt>offset + length</tt>.
     * @return The new byte buffer
     * @throws IndexOutOfBoundsException If the preconditions on the <tt>offset</tt> and <tt>length</tt>
     *                                   parameters do not hold
     */
    public static ByteBuffer wrap(byte[] array, int offset, int length){
        if(offset < 0 || length > array.length - offset || length < 0)
            throw new IndexOutOfBoundsException();
        return new ByteBuffer(array, offset, offset + length);
    }

    /**
     * Wraps a byte array into a buffer.
     *
     * <p> The new buffer will be backed by the given byte array;
     * that is, modifications to the buffer will cause the array to be modified
     * and vice versa.  The new buffer's limit will be
     * <tt>array.length</tt>, its position will be zero. </p>
     *
     * @param array The array that will back this buffer
     * @return The new byte buffer
     */
    public static ByteBuffer wrap(byte[] array){
        return wrap(array, 0, array.length);
    }

    public static ByteBuffer wrap(ByteArray array){
        return wrap(array.getParentBytes(), array.from(), array.length());
    }
}
