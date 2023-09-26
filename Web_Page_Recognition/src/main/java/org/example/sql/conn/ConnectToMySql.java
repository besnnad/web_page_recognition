package org.example.sql.conn;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.example.sql.mapper.MatchMapper;
import org.example.sql.model.Fingerprint;
import org.example.sql.model.InvertedIndex;
import org.example.sql.model.IptoHost;
import org.example.sql.model.PagetoUrl;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @Classname ConnectToMySql
 * @Description Mybatis 总配置+插入数据前预处理， 用于连接数据库
 * @Date 2020/11/15 14:07
 * @Created by shuaif
 */
public class ConnectToMySql {
    // 连接数据库，Mybatis 相关配置
    private InputStream in ;
    private SqlSessionFactory factory;
    private SqlSession session;

    public MatchMapper getMatchMapper() {
        return matchMapper;
    }

    private MatchMapper matchMapper;

    public ConnectToMySql() {
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void init() throws IOException {
        //1.读取配置文件
        in = Resources.getResourceAsStream("MybatisConfig.xml");
        //2.创建 SqlSessionFactory 的构建者对象
        SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
        //3.使用构建者创建工厂对象 SqlSessionFactory
        factory = builder.build(in);
        //4.使用 SqlSessionFactory 生产 SqlSession 对象
        session = factory.openSession();
        //5.使用 SqlSession 创建 dao 接口的代理对象
        matchMapper = session.getMapper(MatchMapper.class);
    }

    public void commit() {
        session.commit();
    }

    public void dispose() {
        //释放资源
        session.close();
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void insertFingerprint(List<Fingerprint> fingerprints) {
        if (fingerprints == null || fingerprints.size() == 0) {
            return ;
        }
        matchMapper.insertFingerprints(fingerprints);
        commit();
    }

    public void insertEigenWord(List<InvertedIndex> words) {
        if (words == null || words.size() == 0) {
            return ;
        }
        matchMapper.insertFeatureWords(words);
        commit();
    }

    public void insertIptoHost(List<IptoHost> iptoHosts) {
        if (iptoHosts == null || iptoHosts.size() == 0) {
            return ;
        }
        for (IptoHost iptoHost : iptoHosts)
            matchMapper.insertIptoHost(iptoHost);
        commit();
    }

    public void insertPagetoUrl(List<PagetoUrl> pagetoUrls) {
        if (pagetoUrls == null || pagetoUrls.size() == 0) {
            return ;
        }
        matchMapper.insertPagetoUrl(pagetoUrls);
        commit();
    }


    public String findHostbyIp(String ip) {
        return matchMapper.selectHostByIp(ip);
    }

}
