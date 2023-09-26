package org.example.work.match;

import org.example.kit.entity.ByteArray;
import org.example.sql.model.InvertedIndex;

import java.util.Arrays;
import java.util.List;

/**
 * @Classname MatchTask
 * @Description 匹配任务：存储目标网页相关信息
 * @Date 2021/3/3 18:17
 * @Created by shuaif
 */
public class MatchTask {
    // 消息版本，目前有 4 和 6
    private int version;

    // 消息的一些标志位
    private boolean realTimeMatching;
    private boolean saveSnapshot;

    // 设计为字节数组形式以支持IPv6
    private byte[] clientIP;
    private byte[] serverIP;

    private int clientPort;
    private int serverPort;

    private ByteArray requestPacket;
    private ByteArray responsePacket;

    private String path = "/";
    private String host;
    private byte[] fingerprint;
    private List<InvertedIndex> eigenWords;

    public MatchTask(){

    }

    public MatchTask(byte[] bytes){
        int firstByte = bytes[0] & 0xFF;
        version = firstByte & 0x0F;
//        realTimeMatching = ((firstByte >>> 6) & 0x01) == 1;
//        saveSnapshot = ((firstByte >>> 5) & 0x01) == 1;
        if(bytes.length > 1){
            // TODO 扩展标志
        }
    }

    public int getVersion(){
        return version;
    }
    public boolean isRealTimeMatching(){
        return realTimeMatching;
    }
    public boolean isSaveSnapshot(){
        return saveSnapshot;
    }
    public byte[] getClientIP(){
        return clientIP;
    }
    public void setClientIP(byte[] clientIP){
        this.clientIP = clientIP;
    }
    public byte[] getServerIP(){
        return serverIP;
    }
    public void setServerIP(byte[] serverIP){
        this.serverIP = serverIP;
    }
    public int getClientPort(){
        return clientPort;
    }
    public void setClientPort(int clientPort){
        this.clientPort = clientPort;
    }
    public int getServerPort(){
        return serverPort;
    }
    public void setServerPort(int serverPort){
        this.serverPort = serverPort;
    }
    public String getHost(){
        return host;
    }
    public void setHost(String host){
        this.host = host;
    }
    public String getPath(){
        return path;
    }
    public void setPath(String path){
        this.path = path;
    }
    public ByteArray getRequestPacket(){
        return requestPacket;
    }
    public void setRequestPacket(ByteArray requestPacket){
        this.requestPacket = requestPacket;
    }
    public ByteArray getResponsePacket(){
        return responsePacket;
    }
    public void setResponsePacket(ByteArray responsePacket){
        this.responsePacket = responsePacket;
    }
    public byte[] getFingerprint(){
        return fingerprint;
    }
    public void setFingerprint(byte[] fingerprint){
        this.fingerprint = fingerprint;
    }
    public List<InvertedIndex> getEigenWords(){
        return eigenWords;
    }
    public void setEigenWords(List<InvertedIndex> eigenWords){
        this.eigenWords = eigenWords;
    }

    @Override
    public String toString() {
        return "[ client ip : "+ Arrays.toString(clientIP) + "]\n" +
                "[ client port : "+ clientPort + "]\n" +
                "[ server ip : "+ Arrays.toString(serverIP) + "]\n" +
                "[ server port : "+ serverPort + "]\n" +
                "[ host : "+ host + "]\n" +
                "[ path : "+ path + "]\n";
    }
}
