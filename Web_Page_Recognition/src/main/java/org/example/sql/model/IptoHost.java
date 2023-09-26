package org.example.sql.model;

/**
 * @Classname IptoHost
 * @Description ip 到 host 的映射
 * @Date 2021/2/19 10:42
 * @Created by shuaif
 */
public class IptoHost {
    private int id;
    private String ip;
    private String host;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
