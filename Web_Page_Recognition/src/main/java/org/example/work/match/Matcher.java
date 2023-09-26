package org.example.work.match;

import org.example.kit.entity.ByteArray;
import org.example.sql.conn.ConnectToMySql;
import org.example.sql.mapper.MatchMapper;
import org.example.sql.model.Fingerprint;
import org.example.sql.model.IndexResult;
import org.example.sql.model.InvertedIndex;

import java.util.*;

/**
 * @Classname Matcher
 * @Description 网页匹配
 * @Date 2021/3/2 18:16
 * @Created by shuaif
 */
public class Matcher {
    private ConnectToMySql conn = new ConnectToMySql();
    private MatchMapper matchMapper = conn.getMatchMapper();

    private Map<Integer,List<InvertedIndex>> candidate_words = new HashMap<>(); // Page_id -> words

    /**
     * 执行匹配。
     * @param identifiedPage -待识别网页
     * @return 反馈匹配结果
     */
    public MatchResult match(MatchTask identifiedPage) {
        List<Long> wordsTarget = new ArrayList<>();
        for (InvertedIndex eigenWord : identifiedPage.getEigenWords()) {
            wordsTarget.add(eigenWord.getWord());
        }
        // 倒排索引，根据特征词获取出现该词的网页
        // 查询完成后统计每个网页包含目标特征词的个数，并过滤掉次数在阈值(目标特征词个数的一半)及以下的网页
        List<IndexResult> candidate = matchMapper.getCandidateSetByWords(wordsTarget, wordsTarget.size() > 2 ? wordsTarget.size() / 2 : null);
        System.out.println("执行匹配...");
        MatchResult matchResult = new MatchResult();
        matchResult.setTarget(identifiedPage);
        if (candidate.size() == 0) { // 无匹配项目，
            matchResult.setSuccess(false);
            return matchResult;
        }
        // 网页候选集按特征词数量排序
        Collections.sort(candidate);
        candidate.removeIf(indexResult -> indexResult.getCount() != wordsTarget.size());
        System.out.println("候选网页集大小 ：" + candidate.size());
        // 过滤候选网页集。
        filterByTargetWords(wordsTarget,candidate);
        //根据特征向量筛选候选集
        filterByFeatureVector(identifiedPage.getEigenWords(),candidate);

        List<Integer> pageIds = new ArrayList<>();
        for (IndexResult indexResult : candidate) {
            pageIds.add(indexResult.getPageId());
        }
        List<Fingerprint> fps = matchMapper.selectFingerprintsByPageIds(pageIds);

        computeSimilarityAndSort(identifiedPage.getFingerprint(),fps);

        Fingerprint target = fps.get(0);
        if(target.getSimilarity() > 0.80){ // 阈值
            matchResult.setSuccess(true);
            matchResult.setWebPageId(target.getPageId());
            matchResult.setSim(target.getSimilarity());
        }

        System.out.println("Target: host-> " + identifiedPage.getHost() + ", path-> " + identifiedPage.getPath());
        System.out.println("\n匹配详情");
        System.out.println( "URL = "+matchMapper.selectUrlByPageID(target.getPageId()));
        System.out.println(target.toString());

//        System.out.println("匹配详情 : " + fps.size() + " 个匹配项");
//        for (Fingerprint fp : fps) {
//            System.out.println( "URL = "+matchMapper.selectUrlByPageID(fp.getPageId()));
//            System.out.println(fp.toString());
//        }

        return matchResult;
    }

    /**
     * （一轮过滤）根据目标特征词过滤网页候选集：根据网页候选集中目标特征词排序过滤掉明显不符合的网页
     * @param words_target -目标网页特征词列表
     * @param candidate_page - 候选网页集
     */
    private void filterByTargetWords(List<Long> words_target, List<IndexResult> candidate_page) {
        List<List<InvertedIndex>> target_contained_candidate_words = new ArrayList<>(); // 候选集所有特征词列表，二维数组，D{p1，p2，，，} ，p{w1，w2，，，}
        for (IndexResult indexResult : candidate_page) {
            int page_id = indexResult.getPageId();
            List<InvertedIndex> words = matchMapper.selectFeatureWordsByPageID(page_id);
            // 根据目标特征词对候选网页集中的向量进行排序
            List<InvertedIndex> result_words = new ArrayList<>();
            for (Long target_word : words_target) {
                for (InvertedIndex candidate_word : words) {
                    if (candidate_word.getWord() == target_word) {
                        result_words.add(candidate_word);
                        break;
                    }
                }
            }
            // 将含有目标特征词的向量，按照索引大小重新排序，向量长度即为网页中含有目标特征词的数量。aj
//            Collections.sort(result_words);
            // 加入网页候选集
            this.candidate_words.put(page_id,words);
            target_contained_candidate_words.add(result_words);
        }
        // 对网页候选集中的网页求最长递增子序列，过滤阈值不符的网页集
        for (int i = 0; i < target_contained_candidate_words.size(); i++) {
            List<InvertedIndex> page = target_contained_candidate_words.get(i); // 对其提取最长递增子序列
            int[] tails = new int[page.size()];
            int length = 0; // 动态规划+二分查找
            for (InvertedIndex word : page) {
                int low = 0, high = length;
                while (low < high) {
                    int m = (low + high) / 2;
                    if (tails[m] < word.getIndex()) low = m + 1;
                    else high = m;
                }
                tails[low] = word.getIndex();
                if (length == high) length++;
            }
            // 限定阈值，排除明显不符合的网页，
            if (length < (3 * page.size()) / 4)
                candidate_page.remove(i);
        }
    }

    /**
     * （二轮过滤） 以权重做向量建立网页特征向量,计算网页候选集和 目标网页的特征向量之间的余弦相似度，选出相似度最大的网页，
     * @param target_words-目标网页特征向量。
     * @param candidate_page -网页候选集
     */
    private void filterByFeatureVector(List<InvertedIndex> target_words,List<IndexResult> candidate_page) {
        int n = target_words.size();
        List<List<InvertedIndex>> candidate_words = new ArrayList<>();
        for (IndexResult indexResult : candidate_page) {
            int page_id = indexResult.getPageId();
            List<InvertedIndex> words = this.candidate_words.get(page_id);
            // sort by index
            Collections.sort(words);
            candidate_words.add(words);
        }
        List<List<Double>> candidate_vectors = new ArrayList<>(); // 候选特征向量。
        List<List<InvertedIndex>> current_pages_words = new ArrayList<>(candidate_words);
        current_pages_words.add(target_words);

        // 计算目标向量
        List<Double> target_vector = new ArrayList<>();
        for (InvertedIndex target_word : target_words) {
            double w = ((double) target_word.getFrequency() / getFrequencyS(target_words)) * getLog(n,target_word,current_pages_words);
            target_vector.add(w);
        }
        // 提取网页候选集的特征向量，
        for (List<InvertedIndex> page : candidate_words) {
            List<Double> vector = new ArrayList<>();
            for (InvertedIndex word : page) {
                double w = ((double) word.getFrequency() / getFrequencyS(target_words)) * getLog(n,word,current_pages_words);
                vector.add(w);
            }
            candidate_vectors.add(vector);
        }
        // 计算余弦相似度
        List<Double> page_sims = new ArrayList<>(); // 记录向量相似度值
        double max_sim = 0.0;
        for (List<Double> candidate_vector : candidate_vectors) {
            double sim = sim(candidate_vector,target_vector);
            if (max_sim < sim) max_sim = sim;
            page_sims.add(sim);
        }
        // 保留相似度最大的选项
        for (int i = 0; i < page_sims.size(); i++) {
            if (page_sims.get(i) < max_sim) {
                candidate_page.remove(i);
            }
        }
    }

    /**
     * 获取商的对数。
     * @param n -
     * @param word -
     * @param all_pages -
     */
    private double getLog(int n, InvertedIndex word, List<List<InvertedIndex>> all_pages) {
        int cw = 0;
        for (List<InvertedIndex> page : all_pages) {
            for (InvertedIndex invertedIndex : page) {
                if (invertedIndex.getWord() == word.getWord()) {
                    cw++;
                }
            }
        }
        return Math.log((double) n/cw);
    }

    /**
     * 获取频率和
     * @param words - 一组特征向量
     * @return
     */
    private int getFrequencyS(List<InvertedIndex> words) {
        int fre = 0;
        for (InvertedIndex word : words) {
            fre += word.getFrequency();
        }
        return fre;
    }

    /**
     * 计算两个向量的余弦相似度
     * @param source -
     * @param target -
     */
    private double sim(List<Double> source, List<Double> target) {
        if (source.size() != target.size()) {
            System.out.println("向量维度不同");
            return 0.0;
        }
        int n = source.size();
        int wiwj = 0;
        int logwi = 0,logwj = 0;
        for (int i = 0; i < n; i++) {
            wiwj += source.get(i) * target.get(i);
            logwi += source.get(i) * source.get(i);
            logwj += target.get(i) * target.get(i);
        }
        return wiwj / (Math.sqrt(logwi) * Math.sqrt(logwj));
    }

    /**
     * 获取网页指纹各部分指纹: [4bits tag][12bits length][content]
     * @param fingerprint - 网页指纹
     * @return part of FP
     */
    public List<ByteArray> getPartOfFingerprint(byte[] fingerprint) {
        List<ByteArray> result = new ArrayList<>();
        int index = 0;
        int count = 0;
        while (count < 3)  {
            count ++;

            byte first_byte = fingerprint[index];
            byte second_byte = fingerprint[index+1];
            int length = ((first_byte & 0x0F) << 8) + (second_byte & 0xFF);
//            System.out.println("index : "+ index +",片段指纹长度 ：" + length);
            int begin = index + 2;
            int end = index + length + 2;
            if (count == (first_byte >> 4))
                result.add(new ByteArray(fingerprint, begin, end));
            else {
                System.out.println("标志位 ‘" + count + "’ 的指纹不存在。" );
                result.add(new ByteArray(new byte[0]));
            }
            index = end;
        }
        return result;
    }

    /**
     * 计算线性指纹相似度，LCS算法
     * @param source -
     * @param target -
     * @return 相似度
     */
    private double linerSimilarity(byte[] source, byte[] target) {
        if (source == null || target == null)
            return 0.0;
        if (source.length == 0 || target.length == 0)
            return 0.0;
        int[][] c = new int[source.length][target.length];
        for (int i = 0; i < source.length; i++) {
            for (int j = 0; j < target.length; j++) {
                if (i == 0 || j == 0) c[i][j] = 0;
                else if (source[i] == target[j]) {
                    c[i][j] = c[i-1][j-1] + 1;
                } else {
                    c[i][j] = Math.max(c[i-1][j],c[i][j-1]);
                }
            }
        }
        return c[source.length - 1][target.length - 1]/(double)(Math.max(source.length,target.length));
    }
    /**
     * 计算指纹相似度并排序，
     * @param fingerprint -目标网页指纹
     * @param fps -网页指纹候选集
     */
    private void computeSimilarityAndSort(byte[] fingerprint, List<Fingerprint> fps) {
        List<ByteArray> target_fp_parts = getPartOfFingerprint(fingerprint);
        for (Fingerprint fp : fps) {
            List<ByteArray> source_fp_parts = getPartOfFingerprint(fp.getFpdata());
            double resp_head_sim,html_head_sim,html_body_sim;
            resp_head_sim = linerSimilarity(source_fp_parts.get(0).getBytes(),target_fp_parts.get(0).getBytes());
            html_head_sim = linerSimilarity(source_fp_parts.get(1).getBytes(),target_fp_parts.get(1).getBytes());
            html_body_sim = linerSimilarity(source_fp_parts.get(2).getBytes(),target_fp_parts.get(2).getBytes());
            double sim = 0.5 * resp_head_sim + 0.3 * html_head_sim + 0.2 * html_body_sim;
            fp.setSimilarity(sim);
        }
        Collections.sort(fps);
        Collections.reverse(fps);
    }
}
