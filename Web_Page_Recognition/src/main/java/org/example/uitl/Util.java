package org.example.uitl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @CLassname Util
 * @Description TODO
 * @Date 2021/5/31 15:09
 * @Created by lenovo
 */
public class Util {
    public static String getHost(String url) {
        String host = null;
        Pattern p = Pattern.compile("(?<=//|)((\\w)+\\.)+\\w+");
        Matcher matcher = p.matcher(url);
        if (matcher.find()) {
            host = matcher.group();
        }
        return host;
    }

    public static boolean urlHasPath(String url) {
        String path = null; //"(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]"
        Pattern p = Pattern.compile("https?://[^/\n]+/([^/\n]+/?)");
        Matcher matcher = p.matcher(url);
        if (matcher.find()) {
            path = matcher.group();
            System.out.println(path);
        }
//        System.out.println(path);
        return path != null;
    }

    public static String genFpIdByName(String name) {
        return "fp_" + MessageDigest.sha1_encode(name).substring(0,20);
    }

    public static String getPath(String url) {
        Pattern p = Pattern.compile("(?<=//|)((\\w)+\\.)+\\w+");
        Matcher m = p.matcher(url);
        if(m.find()){
            String group = m.group();
            System.out.println(group);
            if (url.startsWith("http://")) {
                url = url.substring(8+group.length());
            } else if (url.startsWith("https://")) {
                url = url.substring(9+group.length());
            } else if (url.startsWith("//")) {
                url = url.substring(3+group.length());
            } else {
                url = url.substring(group.length());
            }
        }
        return url;
    }

    public static String getRealHostBySNI(String host) {
        String[] splits = host.split("\\.");
        StringBuilder result = new StringBuilder(splits[1]);
        for (int i = 2; i < splits.length; i++) {
            result.append(".").append(splits[i]);
        }
        return result.toString();
    }
}
