package org.example.controller;

import org.example.sql.conn.ConnectToMySql;
import org.example.sql.model.Website;
import org.example.uitl.Util;
import org.example.work.flow.TrafficAnalysis;
import org.example.work.match.Extract;
import org.example.work.match.MatchResult;
import org.example.work.match.MatchTask;
import org.example.work.match.Matcher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class MatcherController {
    // 映射"/"请求
    @RequestMapping("/index")
    public String index(){
        // 根据Thymeleaf默认模板，将返回resources/templates/index.html
        return "index";
    }
    @RequestMapping("/handshake")
    public String handshake(){
        // 根据Thymeleaf默认模板，将返回resources/templates/handshake.html
        return "handshake";
    }

    @RequestMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file, Model model){
        if (file.isEmpty()){
            model.addAttribute("message", "The file is empty!");
            return "/uploadStatus";
        }
        try{
            byte[] packet = file.getBytes();
//            Path path = Paths.get("E:\\fileUpload/" + file.getOriginalFilename());
//            Files.write(path, bytes);
            for (int i = 0; i < packet.length; i++) {
                if (i % 16 == 0) System.out.println();
                System.out.printf("%02x ",packet[i]);
            }
            System.out.println();
            MatchTask matchTask = new MatchTask();
            TrafficAnalysis.clientHelloAnalysis(packet,matchTask);
            System.out.println(matchTask.toString());
            model.addAttribute("SNI",matchTask.getHost());
            String real_host = Util.getRealHostBySNI(matchTask.getHost());
            model.addAttribute("host",real_host);
            ConnectToMySql conn = new ConnectToMySql();
            Website website = conn.getMatchMapper().selectWebsiteByName(real_host);
            if (website !=null) {
                System.out.println(website.getId());
                model.addAttribute("valid","True");
            } else {
                model.addAttribute("valid","False");
            }

//            Extract.crawl(matchTask);
//            Matcher matcher = new Matcher();
//            MatchResult matchResult = matcher.match(matchTask);
//            System.out.println("match result : " + matchResult.isSuccess());
//            if (matchResult.isSuccess()) {
//                System.out.println("Page id : " + matchResult.getWebPageId());
//                ConnectToMySql conn = new ConnectToMySql();
//                String url = conn.getMatchMapper().selectUrlByPageID(matchResult.getWebPageId());
//                System.out.println("query result : " + url);
//            }
            model.addAttribute("message", "success");

        }catch (Exception e){
            e.printStackTrace();
            model.addAttribute("message","fail");
        }
        return "/analysis";
    }

    @RequestMapping("/match")
    public String match(@RequestParam("file") MultipartFile file, @RequestParam("url") String target_url, Model model){
        if (target_url.isEmpty()) {
            model.addAttribute("message", "url is empty!");
            return "/result";
        }
        try{
            MatchTask matchTask = new MatchTask();
            matchTask.setHost(Util.getHost(target_url));
            matchTask.setPath(Util.getPath(target_url));
            Extract.crawl(matchTask);
            Matcher matcher = new Matcher();
            MatchResult matchResult = matcher.match(matchTask);
            System.out.println("match result : " + matchResult.isSuccess());
            if (matchResult.isSuccess()) {
                System.out.println("Page id : " + matchResult.getWebPageId());
                ConnectToMySql conn = new ConnectToMySql();
                String url = conn.getMatchMapper().selectUrlByPageID(matchResult.getWebPageId());
                System.out.println("query result : " + url);
                String host = Util.getHost(url);
                if (matchTask.getHost().equals(host)) {
                    model.addAttribute("message", "success");
                    model.addAttribute("target_url",target_url);
                    model.addAttribute("url", url);
                    model.addAttribute("page_id",matchResult.getWebPageId());
                    model.addAttribute("sim",matchResult.getSim());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            model.addAttribute("message","fail");
        }
        return "/result";
    }
}
