package org.example.statistics;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.example.kit.FileKit;
import org.example.sql.mapper.MatchMapper;
import org.example.sql.model.IndexResult;
import org.example.uitl.FilePath;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @CLassname countEigenword
 * @Description TODO
 * @Date 2021/6/2 18:57
 * @Created by lenovo
 */
public class countEigenwordTest {
    private InputStream in ;
    private SqlSessionFactory factory;
    private SqlSession session;
    private MatchMapper matchMapper;

    @Before
    public void init() throws Exception{
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

    @After // 在测试方法之哦户执行资源释放
    public void destroy() throws Exception{
        session.commit();
        //释放资源
        session.close();
        in.close();
    }

    @Test
    public void testGetEigenword() throws IOException {
        List<IndexResult> results = this.matchMapper.selectFeatureWordsCount();
        List<String> strings = new ArrayList<>();
        for (IndexResult result : results) {
            strings.add(String.valueOf(result.getCount()));
        }
        FileKit.writeAllLines(strings, FilePath.ROOT_PATH+"count_feature");
    }

}
