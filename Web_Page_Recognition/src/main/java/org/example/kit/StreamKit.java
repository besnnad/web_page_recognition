package org.example.kit;


import org.example.work.crawl.WorkerException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamKit {

    public static byte[] getAllBytes(InputStream inputStream) throws IOException{
        byte[] buffer = new byte[1024];
        int len;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while((len = inputStream.read(buffer)) != -1){
            bos.write(buffer, 0, len);
        }
        bos.flush();
        return bos.toByteArray();
    }

    public static void transform(InputStream inputStream, OutputStream outputStream) throws IOException{
        byte[] buffer = new byte[1024];
        int len, count = 0;
        while((len = inputStream.read(buffer)) != -1){
            outputStream.write(buffer, 0, len);
            count += len;
            if(count > 10 * 1024 * 1024)
                throw new WorkerException("文件太大");
        }
        outputStream.flush();
    }

    public static byte[] readBytes(InputStream in, int byteCount) throws IOException{
        byte[] result = new byte[byteCount];
        int n = 0;
        while(byteCount > 0 && (n = in.read(result, result.length - byteCount, byteCount)) != -1){
            byteCount -= n;
        }
        if(n == -1)
            throw new IOException("Reach end of stream!");
        return result;
    }

    public static void readBytes(InputStream in, byte[] buffer, int byteCount) throws IOException{
        int n = 0, len = byteCount;
        while(byteCount > 0 && (n = in.read(buffer, len - byteCount, byteCount)) != -1){
            byteCount -= n;
        }
        if(n == -1)
            throw new IOException("Reach end of stream!");
    }

    public static void readBytes(InputStream in, byte[] buffer, int offset, int byteCount) throws IOException{
        int n = 0, len = byteCount;
        while(byteCount > 0 && (n = in.read(buffer, offset + len - byteCount, byteCount)) != -1){
            byteCount -= n;
        }
        if(n == -1)
            throw new IOException("Reach end of stream!");
    }

    public static int readInt(InputStream in) throws IOException{
        int result = 0;
        for(int i = 0; i < 4; i++){
            int b = in.read();
            if(b == -1)
                throw new IOException("Reach end of stream!");
            result <<= 8;
            result |= b & 0xFF;
        }
        return result;
    }

    public static byte readByte(InputStream in) throws IOException{
        int result = in.read();
        if(result == -1)
            throw new IllegalArgumentException("无法读取下一字节！");
        return (byte)result;
    }
}
