package org.example.kit;

public class NumKit {

    public static byte[] bigEndBytes(int num){
        return new byte[]{(byte)(num >>> 24), (byte)(num >>> 16), (byte)(num >>> 8), (byte)num};
    }

    public static byte[] bigEndBytes(long num){
        byte[] result = new byte[8];
        for(int i = 7; i >= 0; i--){
            result[i] = (byte)num;
            num >>= 8;
        }
        return result;
    }

    /**
     * 大端字节序
     *
     * @param arr 字节序列
     * @return 字节数组对应的长整数
     */
    public static long bigEndLong(byte[] arr){
        long result = 0;
        for(int i = Math.max(arr.length - 8, 0); i < arr.length; i++){
            result <<= 8;
            result |= arr[i] & 0xFF;
        }
        return result;
    }

    public static int bigEndInt(byte[] arr){
        int result = 0;
        for(int i = Math.max(arr.length - 4, 0); i < arr.length; i++){
            result <<= 8;
            result |= arr[i] & 0xFF;
        }
        return result;
    }
}
