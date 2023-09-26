package org.example.sql;

import net.sf.json.JSONObject;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.example.uitl.FilePath;
import org.example.kit.FileKit;
import org.example.sql.mapper.MatchMapper;
import org.example.sql.model.*;
import org.example.work.thread.ThreadToCrawlPages;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Classname MyBatisTest
 * @Description
 * @Date 2020/11/15 11:38
 * @Created by shuaif
 */
public class  MyBatisTest {
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
    public void insertFingerprintTest() {
        Fingerprint fingerprint = new Fingerprint();
        fingerprint.setLastUpdate(new Timestamp(System.currentTimeMillis()));
        fingerprint.setPageId(1);
        fingerprint.setFpdata(new byte[]{});
        fingerprint.setSimilarity(1.0);
        matchMapper.insertFingerprints(Collections.singletonList(fingerprint));
    }

    @Test
    public void insertEigenword() {
        InvertedIndex eigenWord = new InvertedIndex();
        eigenWord.setIndex(55);
        eigenWord.setPageId(0);
        eigenWord.setFrequency(10);
        eigenWord.setWord(100000);
        matchMapper.insertFeatureWords(Collections.singletonList(eigenWord));
    }

    @Test
    public void insertPagetoUrl() {
        PagetoUrl pagetoUrl = new PagetoUrl();
        pagetoUrl.setPageId(0);
        pagetoUrl.setUrl("http://sdfsdfsdf.sdfsdf/");
        matchMapper.insertPagetoUrl(Collections.singletonList(pagetoUrl));
    }

    @Test
    public void getCandidateword() {
        int threshold = 5;
//        long long word = 2377900603251621120;
//        long[] words = { 2377900603251621120, 2377900603251620352,
//                6989586621670960384,
//                2377900603251613696,
//                2377900603251034368,
//                6989586621670960384,
//                2377900603248680704,
//                2377900603244552960,
//                2377900603244681216,
//                2377900601393890560,
//                6989586621670960384};
        List<Long> wordsTarget = new ArrayList<>();
//        wordsTarget.add()
        List<IndexResult> candidate = matchMapper.getCandidateSetByWords(wordsTarget, wordsTarget.size() > 2 ? wordsTarget.size() / 2 : null);
    }

    @Test
    public void readDataAndInsert() {
        List<JSONObject> jsonList = new ArrayList<>();
        String filePath = FilePath.ROOT_PATH + "index2.data";
        FileKit.readPacket(jsonList,filePath,0,1);
        ThreadToCrawlPages test = new ThreadToCrawlPages();
        JSONObject jo = jsonList.get(0);
        String url = jo.getString("url");
        byte[] data = new byte[0];
        data = jo.getString("data").getBytes();
        System.out.println(jo.getString("data"));
        System.out.println(data.length);
        test.doParseAndExtract(url,data,0);
    }

    @Test
    public void buildFpAndWordsLib_new() {
        ThreadToCrawlPages myThread = new ThreadToCrawlPages();
        myThread.buildFpAndWordsLib_new();
    }


    @Test
    public void buildIptoHostLib() {
        ThreadToCrawlPages myThread = new ThreadToCrawlPages();
        myThread.buildIpAndHostLib();
    }

    @Test
    public void insertIptoHost() {
        IptoHost iptoHost = new IptoHost();
        iptoHost.setIp("0.0.0.1");
        iptoHost.setHost("www.shuai.com");
        matchMapper.insertIptoHost(iptoHost);
    }

    @Test
    public void findHostByIp() {
        String ip = "0.0.0.1";
        System.out.println(matchMapper.selectHostByIp(ip));
    }

    @Test
    public void selectFeatureWordsbyPageIDTest() {
        List<InvertedIndex> result = matchMapper.selectFeatureWordsByPageID(13);
        for (InvertedIndex invertedIndex : result) {
            System.out.println(invertedIndex.toString());
        }
    }


    @Test
    public void ReadAndPrintFp() {
        List<Fingerprint> fingerprints = matchMapper.selectFingerprint();
        List<Integer> page_ids = new ArrayList<>();
        for (Fingerprint fingerprint : fingerprints) {
            if (fingerprint.getFpdata().length < 100) {
                String url = matchMapper.selectUrlByPageID(fingerprint.getPageId());
                byte[] fp = fingerprint.getFpdata();
                int index = 0;
                int count = 0;
                boolean flag = false;
                while (count < 3 && index < fp.length)  {
                    count ++;

                    byte first_byte = fp[index];
                    byte second_byte = fp[index+1];
                    int length = ((first_byte & 0x0F) << 8) + (second_byte & 0xFF);
                    int begin = index + 2;
                    int end = index + length + 2;
                    index = end;
                    if (length == 0) {
                        flag = true;
                    }
                }

                if (flag || count < 3) {
                    for (byte b : fingerprint.getFpdata()) {
                        System.out.printf("%02x ", b);
                    }
                    System.out.println();
                    System.out.println("URL : " + url);
                    page_ids.add(fingerprint.getPageId());
                }
            }
        }
        System.out.println(page_ids.size());
        System.out.println(page_ids);

    }

    @Test
    public void deleteById(){
        int page_id = 0;
        this.matchMapper.deleteFpById(page_id);
        this.matchMapper.deleteFeatureWordById(page_id);
    }

    @Test
    public void insertHost() {
        String filepath = FilePath.ALL_WEBSITE;
        List<String> result = new ArrayList<>();
        try {
            List<String> all_lines = FileKit.getAllLines(filepath);
            for (String line : all_lines) {
                result.add(line.split(",")[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Website> websites = new ArrayList<>();
        for (String s : result) {
            Website website = new Website();
            website.setName(s);
            this.matchMapper.insertOneWebsite(website);
            websites.add(website);
        }
//        this.matchMapper.insertWebsite(websites);

    }
}
