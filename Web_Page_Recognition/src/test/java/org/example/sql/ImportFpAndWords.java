package org.example.sql;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.example.uitl.FilePath;
import org.example.kit.FileKit;
import org.example.sql.mapper.MatchMapper;
import org.example.sql.model.Fingerprint;
import org.example.sql.model.InvertedIndex;
import org.junit.Before;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ImportFpAndWords {
    private SqlSession sqlSession;

    @Before
    public void setUp() throws Exception{
        String resource = "MybatisConfig.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        sqlSession = sqlSessionFactory.openSession();
    }

    @Test
    public void importing() throws Exception{
//        Set<String> errSites = new HashSet<>();
//        List<String> errSitess = TestWebCrawler.getAsStringList("/Users/bonult/Downloads/err.txt");
//        for(String s : errSitess){
//            errSites.add(s.substring(0, s.indexOf(',')));
//        }
        MatchMapper mapper = sqlSession.getMapper(MatchMapper.class);
        PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(FilePath.ROOT_PATH + "err2.txt")));
        List<String> sites = FileKit.getAllLines(FilePath.ROOT_PATH + "websites.txt");

        int start = 0, end = sites.size() - 1;

        ExecutorService service = Executors.newFixedThreadPool(50);
        List<Future<Object[]>> futureList = new ArrayList<>(end - start + 1);
        for(int i = start; i <= end; i++){
//            if(!errSites.contains(sites.get(i).substring(0, sites.get(i).indexOf(','))))
//                continue;
            LocalExtractionTask task = new LocalExtractionTask(sites.get(i), i + 3, pw);
            futureList.add(service.submit(task));
        }

        List<Fingerprint> fps = new ArrayList<>(500);
        List<InvertedIndex> iis = new ArrayList<>(20000);
        for(Future<Object[]> future : futureList){
            Object[] result = future.get();
            pw.flush();
            if(result.length == 0)
                continue;
            fps.add((Fingerprint)result[0]);
            InvertedIndex[] iiis = (InvertedIndex[])result[1];
            Collections.addAll(iis, iiis);
            if(fps.size() >= 500 && mapper != null){
                mapper.insertFingerprints(fps);
                sqlSession.commit();
                fps.clear();
                if(iis.size() > 0){
                    mapper.insertFeatureWords(iis);
                    sqlSession.commit();
                    iis.clear();
                }
            }
        }
        if(fps.size() > 0 && mapper != null){
            mapper.insertFingerprints(fps);
            sqlSession.commit();
            fps.clear();
            if(iis.size() > 0){
                mapper.insertFeatureWords(iis);
                sqlSession.commit();
                iis.clear();
            }
        }
        service.shutdown();
        pw.close();
    }
}
