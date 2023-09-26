package org.example.work.parse.nodes;

/**
 * 网页DOM文档结构
 */
public class Document {
    private String charset;
    private Element html;

    public String getCharset(){
        return charset;
    }
    public void setCharset(String charset){
        this.charset = charset;
    }
    public Element getHtml(){
        return html;
    }
    public void setHtml(Element html){
        this.html = html;
    }
}
