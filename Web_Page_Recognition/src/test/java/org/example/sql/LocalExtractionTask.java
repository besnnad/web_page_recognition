package org.example.sql;

import org.example.uitl.FilePath;
import org.example.kit.FileKit;
import org.example.kit.entity.BiSupplier;
import org.example.kit.entity.ByteArray;
import org.example.sql.model.Fingerprint;
import org.example.sql.model.InvertedIndex;
import org.example.work.eigenword.EigenWord;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.concurrent.Callable;

public class LocalExtractionTask implements Callable<Object[]> {

    private String site;
    private PrintWriter pw;
    private int id;

    LocalExtractionTask(String site, int id, PrintWriter pw){
        this.site = site;
        this.id = id;
        this.pw = pw;
    }

    @Override
    public Object[] call(){
        String host = site.substring(0, site.indexOf(','));
        String pathPrefix = FilePath.ROOT_PATH +  "HTTP/http_";
        File file = new File(pathPrefix + Math.abs(host.hashCode() % 500) + "/" + host);
        if(!file.exists()){
            pw.println(host + ",文件不存在");
            return new Object[]{};
        }
        byte[] data;
        try{
            data = FileKit.getAllBytes(file);
        }catch(IOException e){
            pw.println(host + ",读文件异常");
            return new Object[]{};
        }
        if(data.length > 1024 * 1024 * 5){
            pw.println(host + ",文件太大");
            return new Object[]{};
        }
        ByteArray resp = new ByteArray(data);
        try{
            Fingerprint fp = new Fingerprint();
            BiSupplier<byte[], EigenWord[]> d = null; //  TODO 指纹和特征词提取

            fp.setLastUpdate(new Timestamp(System.currentTimeMillis()));
            fp.setPageId(id);
            fp.setFpdata(d.first());
            InvertedIndex[] iiArr = new InvertedIndex[d.second().length];
            int co = 0;
            for(EigenWord l : d.second()){
                InvertedIndex ii = new InvertedIndex();
                ii.setPageId(fp.getPageId());
                ii.setWord(l.getWord());
                iiArr[co++] = ii;
            }
            return new Object[]{fp, iiArr};
        }catch(Exception e){
            pw.println(host + "," + e.getClass().getSimpleName() + "," + e.getMessage());
            pw.flush();
        }catch(Throwable t){
            System.out.println(id + "  " + file.getAbsolutePath());
            System.out.println(host + "," + t.getMessage());
            throw t;
        }
        return new Object[]{};
    }
}
