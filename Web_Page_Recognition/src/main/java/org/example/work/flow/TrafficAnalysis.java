package org.example.work.flow;

import org.example.kit.ByteBuffer;
import org.example.kit.entity.ByteArray;
import org.example.work.match.MatchTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Classname TrafficAnalysis
 * @Description 流量解析
 * @Date 2021/3/17 20:05
 * @Created by shuaif
 */
public class TrafficAnalysis {
    private static final int DATA_PCAP_FILE_HEAD = 24 + 16;
    private static final int DATA_FRAME_HEAD_LENGTH = 14;
    private static final int IP_PACKET_HEAD_LENGTH = 20;
    private static final int TCP_HEAD_LENGTH = 20; // 暂定无扩展

    private mac_header macHeader;
    private ip_header ipHeader;
    private tcp_header tcpHeader;
    private ssl_handshake handshake;

    public static void responseAnalysis(byte[] responsePacket, MatchTask matchTask) {
        System.out.println("正在解析数据包，，，");
        ByteArray buffer = new ByteArray(responsePacket);
//        ByteArray buffer = new ByteArray(packet);
//        int index = 0;
        // 各层头部解析
        int index = DATA_PCAP_FILE_HEAD;
        mac_header macHeader = new mac_header(buffer.subBytes(index,index + DATA_FRAME_HEAD_LENGTH));
        index += DATA_FRAME_HEAD_LENGTH;
        ip_header ipHeader = new ip_header(buffer.subBytes(index,index + IP_PACKET_HEAD_LENGTH));
        index += IP_PACKET_HEAD_LENGTH;
        tcp_header tcpHeader = new tcp_header(buffer.subBytes(index,index + TCP_HEAD_LENGTH));
        index += TCP_HEAD_LENGTH;
        matchTask.setResponsePacket(new ByteArray(buffer.subBytes(index)));
        matchTask.setClientIP(ipHeader.getSource_ip());
        matchTask.setClientPort(tcpHeader.getSrc_port());
        matchTask.setServerIP(ipHeader.getTarget_ip());
        matchTask.setServerPort(tcpHeader.getDst_port());
    }

    public static void clientHelloAnalysis(byte[] packet, MatchTask matchTask) {
        System.out.println("正在解析数据包，，，");
        // 跳过链路层，网络层，以及传输层报文头部
        ByteArray buffer = new ByteArray(packet);
//        ByteArray buffer = new ByteArray(packet);
//        int index = 0;
        // 各层头部解析
        int index = DATA_PCAP_FILE_HEAD;
        mac_header macHeader = new mac_header(buffer.subBytes(index,index + DATA_FRAME_HEAD_LENGTH));
        index += DATA_FRAME_HEAD_LENGTH;
        ip_header ipHeader = new ip_header(buffer.subBytes(index,index + IP_PACKET_HEAD_LENGTH));
        index += IP_PACKET_HEAD_LENGTH;
        tcp_header tcpHeader = new tcp_header(buffer.subBytes(index,index + TCP_HEAD_LENGTH));
        index += TCP_HEAD_LENGTH;
        matchTask.setResponsePacket(new ByteArray(buffer.subBytes(index)));
        // TLS
        byte type = buffer.get(index++);
        if (type != 0x16) {
            System.out.println("非handshake类型报文。");
            return ;
        }
        // 解析数据报
        ssl_handshake handshake = new ssl_handshake(type,help.getShort(buffer.subBytes(index,index + 2)),
                help.getShort(buffer.subBytes(index + 2, index + 4)),buffer.subBytes(index + 4));
        boolean flag = false;
        String host = "";
        if (handshake.isIs_client_hello()) {
            System.out.println("是 client hello 请求报文");
            if (handshake.isIs_get_server_name()) {
                System.out.println("SNI可获得");
                for (ssl_extension extension : handshake.getSsl_hello().getExtensions()) {
                    if (extension.getType() == 0) {
                        ByteArray byteArray = new ByteArray(extension.getData());
                        System.out.println(macHeader.toString());
                        System.out.println(ipHeader.toString());
                        System.out.println(tcpHeader.toString());
                        System.out.println(handshake.toString());
                        System.out.println(handshake.getSsl_hello().toString());
                        System.out.println(extension.toString());
                        System.out.println("host : " + new String(byteArray.subBytes(5)));
                        host = new String(byteArray.subBytes(5));
                        flag = true;
                        break;
                    }
                }
            }
        }

        // MatchTask;
        matchTask.setClientIP(ipHeader.getSource_ip());
        matchTask.setClientPort(tcpHeader.getSrc_port());
        matchTask.setServerIP(ipHeader.getTarget_ip());
        matchTask.setServerPort(tcpHeader.getDst_port());
        matchTask.setHost(host);
        matchTask.setPath("/");
    }

}

// 链路层数据帧头
class mac_header {
    private byte[] dst_mac;
    private byte[] src_mac;
    private short eth_type; // 以太网类型
    public mac_header(byte[] content) {
        ByteArray buffer = new ByteArray(content);
        this.dst_mac = buffer.subBytes(0,6);
        this.src_mac = buffer.subBytes(6,12);
        this.eth_type = help.getShort(buffer.subBytes(12,14));
    }

    public byte[] getDst_mac() {
        return dst_mac;
    }

    public byte[] getSrc_mac() {
        return src_mac;
    }

    public short getEth_type() {
        return eth_type;
    }

    @Override
    public String toString() {
        return "mac_header{" +
                "dst_mac=" + Arrays.toString(dst_mac) +
                ", src_mac=" + Arrays.toString(src_mac) +
                ", eth_type=" + eth_type +
                '}';
    }
}

// IP数据报头部
class ip_header {
    private int version; // 半字节
    private int header_length; // 半字节
    private byte tos; // 服务类型
    private short total_length;
    private short id;
    private short fragment_offset;
    private byte ttl;
    private byte protocol;
    private byte[] source_ip;// 4
    private byte[] target_ip;// 4

    public ip_header(byte[] content) {
        ByteArray buffer = new ByteArray(content);
        int index=0;
        byte temp = buffer.get(index++);
        this.version = (temp >> 4) & 0xF;
        this.header_length = temp & 0xF;
        this.tos = buffer.get(index++);
        this.total_length = help.getShort(buffer.subBytes(index,index + 2));
        index += 2;
        this.id = help.getShort(buffer.subBytes(index,index + 2));
        index += 2;
        this.fragment_offset = help.getShort(buffer.subBytes(index,index + 2));
        index += 2;
        this.ttl = buffer.get(index++);
        this.protocol = buffer.get(index++);
        this.source_ip = buffer.subBytes(index,index + 4);
        index += 4;
        this.target_ip = buffer.subBytes(index,index + 4);
    }

    public int getVersion() {
        return version;
    }

    public int getHeader_length() {
        return header_length;
    }

    public byte getTos() {
        return tos;
    }

    public short getTotal_length() {
        return total_length;
    }

    public short getId() {
        return id;
    }

    public short getFragment_offset() {
        return fragment_offset;
    }

    public byte getTtl() {
        return ttl;
    }

    public byte getProtocol() {
        return protocol;
    }

    public byte[] getSource_ip() {
        return source_ip;
    }

    public byte[] getTarget_ip() {
        return target_ip;
    }

    @Override
    public String toString() {
        return "ip_header{" +
                "version=" + version +
                ", header_length=" + header_length +
                ", tos=" + tos +
                ", total_length=" + total_length +
                ", id=" + id +
                ", fragment_offset=" + fragment_offset +
                ", ttl=" + ttl +
                ", protocol=" + protocol +
                ", source_ip=" + Arrays.toString(source_ip) +
                ", target_ip=" + Arrays.toString(target_ip) +
                '}';
    }
}

// TCP数据报头
class tcp_header {
    private short src_port;
    private short dst_port;
    private int sequence;
    private int acknowledge_number;
    private int header_length; // 半字节
    private byte[] flags; // 一个半字节
    private short window_size;
    private short checksum;
    private short urgent_pointer;

    public tcp_header(byte[] content) {
        ByteArray buffer = new ByteArray(content);
        int index = 0;
        this.src_port = help.getShort(buffer.subBytes(index,index + 2));
        index += 2;
        this.dst_port = help.getShort(buffer.subBytes(index,index + 2));
        index += 2;
        this.sequence = help.getInteger(buffer.subBytes(index, index + 4));
        index += 4;
        this.acknowledge_number = help.getInteger(buffer.subBytes(index,index + 4));
        index += 4;
        this.header_length = (buffer.get(index) >> 4) & 0xF;
        this.flags = buffer.subBytes(index, index + 2);
        this.flags[0] &= 0x0F;
        index += 2;
        this.window_size = help.getShort(buffer.subBytes(index,index + 2));
        index += 2;
        this.checksum = help.getShort(buffer.subBytes(index,index + 2));
        index += 2;
        this.urgent_pointer = help.getShort(buffer.subBytes(index,index + 2));
    }

    public short getSrc_port() {
        return src_port;
    }

    public short getDst_port() {
        return dst_port;
    }

    public int getSequence() {
        return sequence;
    }

    public int getAcknowledge_number() {
        return acknowledge_number;
    }

    public int getHeader_length() {
        return header_length;
    }

    public byte[] getFlags() {
        return flags;
    }

    public short getWindow_size() {
        return window_size;
    }

    public short getChecksum() {
        return checksum;
    }

    public short getUrgent_pointer() {
        return urgent_pointer;
    }

    @Override
    public String toString() {
        return "tcp_header{" +
                "src_port=" + src_port +
                ", dst_port=" + dst_port +
                ", sequence=" + sequence +
                ", acknowledge_number=" + acknowledge_number +
                ", header_length=" + header_length +
                ", flags=" + Arrays.toString(flags) +
                ", window_size=" + window_size +
                ", checksum=" + checksum +
                ", urgent_pointer=" + urgent_pointer +
                '}';
    }
}

// TLS头部 TVL 格式
class ssl_handshake {
    private int type; // handshake = 0x16
    private short version; // TLS 1.0 = 0x0301  1.1 0x0302 类推。
    private short length;
    private byte[] content;
    private ssl_hello ssl_hello;
    private boolean is_client_hello = true;
    private boolean is_get_server_name;

    public ssl_handshake(byte type, short version, short length, byte[] content) {
        this.type = type & 0xFF;
        this.version = version;
        this.length = length;
        this.content = content;
        parseSSLHello();
    }

    private void parseSSLHello() {
        System.out.println("parse ssl hello..");
        ByteArray buffer = new ByteArray(content);
        int index = 0; // skip nothing
        byte handshake_type = buffer.get(index++);
        if (handshake_type != 0x1) { // 非client hello
            is_client_hello = false;
            System.out.println("非 client hello ： " + handshake_type);
            return;
        }
        index++; // skip type
        byte[] length = buffer.subBytes(index, index + 3);
        index += 3; // skip length
        short version = getShort(buffer.subBytes(index, index + 2));
        index += 2; // skip version
        ssl_random random = new ssl_random(buffer.subBytes(index,index+4),buffer.subBytes(index+4,index+32));
        index += 31; // skip random
        char session_id_length = (char) buffer.get(index);
//        System.out.println(((int) session_id_length));
        index ++;
        byte[] session_id = buffer.subBytes(index,index + (int)session_id_length);
        index += session_id_length; // skip session id
        short cipher_suites_length = getShort(buffer.subBytes(index, index + 2));
//        System.out.println(cipher_suites_length);
        index += 2;
        byte[] cipher_suites = buffer.subBytes(index,index + cipher_suites_length);
        index += cipher_suites_length; // skip cipher_suites
        char compression_methods_length = (char) buffer.get(index);
        index ++;
        byte[] compression_methods = buffer.subBytes(index,index + (int) compression_methods_length);
        index += compression_methods_length; // skip compression_methods
//        System.out.println(((int) compression_methods_length));
        short extension_length = getShort(buffer.subBytes(index, index + 2));
        index += 2;
//        System.out.println(extension_length);
        List<ssl_extension> extensions = parseExtensions(buffer.subBytes(index));

        this.ssl_hello = new ssl_hello(handshake_type,length,version,random,session_id_length
                ,session_id,cipher_suites_length,cipher_suites,compression_methods_length,compression_methods,extension_length,extensions);
    }

    private List<ssl_extension> parseExtensions(byte[] content) {
        List<ssl_extension> result = new ArrayList<>();
        ByteArray buffer = new ByteArray(content);
        int index = 0;
        while (index < content.length - 1) {
            short type = getShort(buffer.subBytes(index, index + 2));
            if (type == 0) this.is_get_server_name = true;
            index += 2;
            short length = getShort(buffer.subBytes(index, index + 2));
            index += 2;
            byte[] data = buffer.subBytes(index, index + length);
            index += length;
            result.add(new ssl_extension(type,length,data));
//            System.out.println("type : " + type);
        }
        return result;
    }

    public short getShort(byte[] bytes) {
        if (bytes == null || bytes.length!=2) {
            System.out.println("数据格式错误");
            return 0;
        }
        byte argB1 = bytes[0];
        byte argB2 = bytes[1];
        return (short) ((argB1 << 8)| (argB2 & 0xFF));
    }

    public boolean isIs_client_hello() {
        return is_client_hello;
    }

    public boolean isIs_get_server_name() {
        return  is_get_server_name;
    }

    public int getType() {
        return type;
    }

    public short getVersion() {
        return version;
    }

    public short getLength() {
        return length;
    }

    public byte[] getContent() {
        return content;
    }

    public ssl_hello getSsl_hello() {
        return ssl_hello;
    }

    @Override
    public String toString() {
        return "ssl_handshake{" +
                "type=" + type +
                ", version=" + version +
                ", length=" + length +
                ", content=" + Arrays.toString(content) +
                ", ssl_hello=" + ssl_hello +
                ", is_client_hello=" + is_client_hello +
                ", is_get_server_name=" + is_get_server_name +
                '}';
    }
}

class ssl_hello {
    private int handshake_type; // client = 1
    private byte[] length = new byte[3];
    private short version; // TLS 1.0 = 0x0301  1.1 0x0302 类推。
    private ssl_random random ;   // 跳过32个字节。
    private char session_id_length; // session 长度， 新建立的链接此字段可能为0
    private byte[] session_id;
    private short cipher_suites_length; // 加密套件长度，
    private byte[] cipher_suites; // 加密套件。（跳过）
    private char compression_methods_length; // 压缩算法长度（我这辈子没见过用的）
    private byte[] compression_method;
    private short extensions_length; // 扩展字段，server_name（1） 字段提供了客户端访问服务器时服务器的域名。
    private List<ssl_extension> extensions;

    public void setHandshake_type(char handshake_type) {
        this.handshake_type = handshake_type;
    }

    public void setLength(byte[] length) {
        this.length = length;
    }

    public void setVersion(short version) {
        this.version = version;
    }

    public void setRandom(ssl_random random) {
        this.random = random;
    }

    public void setSession_id_length(char session_id_length) {
        this.session_id_length = session_id_length;
    }

    public void setSession_id(byte[] session_id) {
        this.session_id = session_id;
    }

    public void setCipher_suites_length(short cipher_suites_length) {
        this.cipher_suites_length = cipher_suites_length;
    }

    public void setCipher_suites(byte[] cipher_suites) {
        this.cipher_suites = cipher_suites;
    }

    public void setCompression_methods_length(char compression_methods_length) {
        this.compression_methods_length = compression_methods_length;
    }

    public void setCompression_method(byte[] compression_method) {
        this.compression_method = compression_method;
    }

    public void setExtensions_length(short extensions_length) {
        this.extensions_length = extensions_length;
    }

    public void setExtensions(List<ssl_extension> extensions) {
        this.extensions = extensions;
    }

    public List<ssl_extension> getExtensions() {
        return extensions;
    }

    public ssl_hello(byte handshake_type, byte[] length, short version
            , ssl_random random, char session_id_length, byte[] session_id, short cipher_suites_length, byte[] cipher_suites
            , char compression_methods_length, byte[] compression_method, short extensions_length, List<ssl_extension> extensions) {
        this.handshake_type = handshake_type & 0xFF;
        this.length = length;
        this.version = version;
        this.random = random;
        this.session_id_length = session_id_length;
        this.session_id = session_id;
        this.cipher_suites_length = cipher_suites_length;
        this.cipher_suites = cipher_suites;
        this.compression_methods_length = compression_methods_length;
        this.compression_method = compression_method;
        this.extensions_length = extensions_length;
        this.extensions = extensions;
    }

    @Override
    public String toString() {
        return "ssl_hello{" +
                "handshake_type=" + handshake_type +
                ", length=" + Arrays.toString(length) +
                ", version=" + version +
                ", random=" + random +
                ", session_id_length=" + session_id_length +
                ", session_id=" + Arrays.toString(session_id) +
                ", cipher_suites_length=" + cipher_suites_length +
                ", cipher_suites=" + Arrays.toString(cipher_suites) +
                ", compression_methods_length=" + compression_methods_length +
                ", compression_method=" + Arrays.toString(compression_method) +
                ", extensions_length=" + extensions_length +
                ", extensions=" + extensions +
                '}';
    }
}

// 32bytes
class ssl_random {
    private int timestamp;
    private byte[] random = new byte[28];

    public ssl_random(byte[] timestamp,byte[] random) {
        this.random = random;
    }
}

// server name
class server_name {
    private short list_length;
    private char type; // 0 by host_name
    private short length;
    private byte[] server_name;
    public server_name(byte[] bytes) {
        ByteArray buffer = new ByteArray(bytes);

    }

    public byte[] getServer_name() {
        return server_name;
    }

    @Override
    public String toString() {
        return "server_name{" +
                "list_length=" + list_length +
                ", type=" + type +
                ", length=" + length +
                ", server_name=" + new String(server_name) +
                '}';
    }
}

// TLS 扩展字段
class ssl_extension {
    private short type;
    private short length;
    private byte[] data;

    public ssl_extension(short type, short length, byte[] data) {
        this.type = type;
        this.length = length;
        this.data = data;
    }

    public short getType() {
        return type;
    }

    public short getLength() {
        return length;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "ssl_extension{" +
                "type=" + type +
                ", length=" + length +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}

class help {
    public static short getShort(byte[] bytes) {
        if (bytes == null || bytes.length!=2) {
            System.out.println("数据格式错误，"+ Arrays.toString(bytes));
            return 0;
        }
        byte argB1 = bytes[0];
        byte argB2 = bytes[1];
        return (short) ((argB1 << 8)| (argB2 & 0xFF));
    }

    public static int getInteger(byte[] bytes) {
        if (bytes == null || bytes.length!=4) {
            System.out.println("数据个格式错误，"+ Arrays.toString(bytes));
            return 0;
        }
        return bytes[3] & 0xFF |
                (bytes[2] & 0xFF) << 8 |
                (bytes[1] & 0xFF) << 16 |
                (bytes[0] & 0xFF) << 24;
    }
}

