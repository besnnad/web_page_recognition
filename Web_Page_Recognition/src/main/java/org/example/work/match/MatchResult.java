package org.example.work.match;


/**
 * @Classname MatchResult
 * @Description 匹配结果
 * @Date 2021/3/3 18:52
 * @Created by shuaif
 */
public class MatchResult {
    private MatchTask target;
    private boolean success;
    private double sim;

    private int webPageId; // 识别成功网页ID
    private PageRecord webPage;

    public MatchTask getTarget() {
        return target;
    }

    public void setTarget(MatchTask target) {
        this.target = target;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setWebPageId(Integer pageId) {
        this.webPageId = pageId;
    }
    public int getWebPageId() {
        return webPageId;
    }

    public void setWebPageId(int webPageId) {
        this.webPageId = webPageId;
    }

    public PageRecord getWebPage() {
        return webPage;
    }

    public void setWebPage(PageRecord webPage) {
        this.webPage = webPage;
    }

    public double getSim() {
        return sim;
    }

    public void setSim(double sim) {
        this.sim = sim;
    }
}
