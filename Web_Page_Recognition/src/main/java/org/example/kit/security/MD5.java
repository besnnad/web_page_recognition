package org.example.kit.security;

public class MD5 {

    private static final int[] K = {
            0xd76aa478, 0xe8c7b756, 0x242070db, 0xc1bdceee, 0xf57c0faf, 0x4787c62a, 0xa8304613, 0xfd469501, 0x698098d8,
            0x8b44f7af, 0xffff5bb1, 0x895cd7be, 0x6b901122, 0xfd987193, 0xa679438e, 0x49b40821, 0xf61e2562, 0xc040b340,
            0x265e5a51, 0xe9b6c7aa, 0xd62f105d, 0x02441453, 0xd8a1e681, 0xe7d3fbc8, 0x21e1cde6, 0xc33707d6, 0xf4d50d87,
            0x455a14ed, 0xa9e3e905, 0xfcefa3f8, 0x676f02d9, 0x8d2a4c8a, 0xfffa3942, 0x8771f681, 0x6d9d6122, 0xfde5380c,
            0xa4beea44, 0x4bdecfa9, 0xf6bb4b60, 0xbebfbc70, 0x289b7ec6, 0xeaa127fa, 0xd4ef3085, 0x04881d05, 0xd9d4d039,
            0xe6db99e5, 0x1fa27cf8, 0xc4ac5665, 0xf4292244, 0x432aff97, 0xab9423a7, 0xfc93a039, 0x655b59c3, 0x8f0ccc92,
            0xffeff47d, 0x85845dd1, 0x6fa87e4f, 0xfe2ce6e0, 0xa3014314, 0x4e0811a1, 0xf7537e82, 0xbd3af235, 0x2ad7d2bb,
            0xeb86d391
    };

    private static final int[] SHIFT = {
            7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22, 5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20, 5, 9,
            14, 20, 4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23, 6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15,
            21, 6, 10, 15, 21
    };

    private int varA = 0x67452301;
    private int varB = 0xefcdab89;
    private int varC = 0x98badcfe;
    private int varD = 0x10325476;

    public void reset(){
        varA = 0x67452301;
        varB = 0xefcdab89;
        varC = 0x98badcfe;
        varD = 0x10325476;
    }

    public static int[] getK() {
        return K;
    }

    private int shift(int num, int shift){
        return (num << shift) | (num >>> (32 - shift));
    }

    private void mainLoop(int[] M){
        int F, g, tmp;
        int a = varA;
        int b = varB;
        int c = varC;
        int d = varD;
        for(int i = 0; i < 64; i++){
            if(i < 16){
                F = (b & c) | ((~b) & d);
                g = i;
            }else if(i < 32){
                F = (d & b) | ((~d) & c);
                g = (5 * i + 1) & 0x0F;
            }else if(i < 48){
                F = b ^ c ^ d;
                g = (3 * i + 5) & 0x0F;
            }else{
                F = c ^ (b | (~d));
                g = (7 * i) & 0x0F;
            }
            tmp = d;
            d = c;
            c = b;
            b = b + shift(a + F + K[i] + M[g], SHIFT[i]);
            a = tmp;
        }
        varA += a;
        varB += b;
        varC += c;
        varD += d;
    }

    public void update(byte[] bytes, int offset, int length){
        // 填充函数
        // 处理后应满足bytes≡56（mode64)，填充方式为先加一个字节，其它位补零
        // 最后加上64位的原来长度
        int num = ((length + 8) >> 6) + 1; // 64个字节为一组
        int[] ints = new int[num << 4]; // 64个字节对应16个整数

        //添加原数据位的长度，由于小端序放在倒数第二个，这里长度只用了32位
        ints[ints.length - 2] = length << 3;

        int i;
        for(i = 0; i < length; i++)
            ints[i >> 2] |= (bytes[i + offset] & 0x0FF) << ((i & 0x03) << 3); // 一个整数存储四个字节，小端序
        ints[i >> 2] |= 0x80 << ((i & 0x03) << 3); // 尾部添加1

        int[] nums = new int[16];
        for(i = 0; i < num; i++){
            System.arraycopy(ints, i << 4, nums, 0, 16);
            mainLoop(nums);
        }
    }

    public byte[] digest(){
        byte[] hash = new byte[16];
        wrapIntToBytes(varA, hash, 0);
        wrapIntToBytes(varB, hash, 4);
        wrapIntToBytes(varC, hash, 8);
        wrapIntToBytes(varD, hash, 12);
        return hash;
    }

    private void wrapIntToBytes(int num, byte[] target, int offset){
        target[offset] = (byte)num;
        target[offset + 1] = (byte)(num >> 8);
        target[offset + 2] = (byte)(num >> 16);
        target[offset + 3] = (byte)(num >> 24);
    }

    public int digestInt(){
        return varA ^ varB ^ varC ^ varD;
    }

    public long digestLong(){
        long hash = ((varA & 0x0FFFFFFFFL) << 32) | (varB & 0x0FFFFFFFFL);
        long hash1 = ((varC & 0x0FFFFFFFFL) << 32) | (varD & 0x0FFFFFFFFL);
        return hash ^ hash1;
    }
}