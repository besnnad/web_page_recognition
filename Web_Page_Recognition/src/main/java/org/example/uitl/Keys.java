package org.example.uitl;

/**
 * @Classname Keys
 * @Description 指纹提取相关的关键字。
 * @Date 2020/11/3 19:10
 * @Created by shuaif
 */
public class Keys {
    // Response header relevant
    public static final String[] RESPONSE_HEADERS = new String[]{
            "Accept-Ranges", "Age", "Allow", "Cache-Control", "Connection", "Content-Encoding", "Content-Language",
            "Content-Length", "Content-Location", "Content-MD5", "Content-Range", "Content-Type", "Date", "ETag",
            "Expires", "Last-Modified", "Location", "Proxy-Authenticate", "Refresh", "Retry-After", "Server",
            "Set-Cookie", "Set-Cookie2", "Trailer", "Transfer-Encoding", "Vary", "Via", "Warning",
            "WWW-Authenticate"
    };
    public static final String[] RESPONSE_KEY_NOT_USED = new String[]{
            "Cache-Control", "Set-Cookie", "Set-Cookie2", "Vary", "Via", "Proxy-Authenticate"
    };
    public static final String[] RESPOSE_VALUE_USED = new String[]{
            "Server", "Content-Location", "Location", "Refresh", "WWW-Authenticate"
    };

    // HTML head relevant
    public static final String[] HEAD_PROPERTIES = new String[]{
            "style", "title", "script", "base", "Charset", "Content-Type", "Default-Style", "Refresh", "Scheme",
            "Application-Name", "Author", "Description", "Generator", "Keywords", "Alternate", "Archives",
            "Bookmark", "External", "First", "Help", "Icon", "Shortcut Icon", "Last", "License", "Next", "Nofollow",
            "Noreferrer", "Pingback", "Prefetch", "Prev", "Search", "Sidebar", "Stylesheet", "Tag", "Up",
    };



}
