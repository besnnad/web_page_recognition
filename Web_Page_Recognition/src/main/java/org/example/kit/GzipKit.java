package org.example.kit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipKit {
    private static final String DEFAULT_ENCODING = "UTF-8";

    public static byte[] compress(String str, String encoding){
        if(str == null || str.length() == 0){
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip;
        try{
            gzip = new GZIPOutputStream(out);
            gzip.write(str.getBytes(encoding));
            gzip.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    public static byte[] compress(String str){
        return compress(str, DEFAULT_ENCODING);
    }

    public static byte[] decompress(byte[] bytes){
        Assert.notNull(bytes);
        return decomp(bytes).toByteArray();
    }

    public static String decompressToString(byte[] bytes, String encoding) throws UnsupportedEncodingException{
        Assert.notNull(bytes);
        Assert.notNull(encoding);
        return decomp(bytes).toString(encoding);
    }

    private static ByteArrayOutputStream decomp(byte[] bytes){
        ByteArrayOutputStream out = new ByteArrayOutputStream((bytes.length << 1) + (bytes.length >>> 1));
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try{
            GZIPInputStream ungzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while((n = ungzip.read(buffer)) >= 0){
                out.write(buffer, 0, n);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return out;
    }

    public static String decompressToString(byte[] bytes){
        try{
            return decompressToString(bytes, DEFAULT_ENCODING);
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }
        return null;
    }
}
