package org.example.work.thread;

import org.example.kit.FileKit;
import org.example.uitl.Util;

/**
 * @CLassname NewThread
 * @Description TODO
 * @Date 2021/3/10 10:29
 * @Created by lenovo
 */
public class NewThread extends ThreadToCrawlPages {
    private String url;
    private int serial;
    public NewThread(String url,int serial) {
        this.url = url;
        this.serial = serial;
    }


    @Override
    public void run() {
        long start = System.currentTimeMillis();
        crawl_new(url, serial);
        long end = System.currentTimeMillis();
        System.out.println("serial = " + serial + ", timespan = " + (end - start) + ", url = " + url);
        FileKit.writeALineToFile(String.valueOf(end - start),"../log/crawl_time_span.txt");
    }
}
