package org.example.util;

import org.example.uitl.Util;
import org.junit.jupiter.api.Test;

/**
 * @CLassname UtilTest
 * @Description TODO
 * @Date 2021/5/31 15:20
 * @Created by lenovo
 */
public class UtilTest {
    @Test
    public void testExtractHostAndPath() {
        String url = "www.hit.edu.cn/asdasd";
        String host = Util.getHost(url);
        System.out.println(host);
        System.out.println(Util.urlHasPath(url));
        System.out.println(Util.getPath(url));
        System.out.println(Util.getRealHostBySNI(host));
    }
}
