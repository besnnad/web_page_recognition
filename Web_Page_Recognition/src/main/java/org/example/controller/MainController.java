package org.example.controller;

import jdk.net.SocketFlow;
import org.example.result.Data;
import org.example.result.RestResult;
import org.example.result.Result;
import org.example.service.ServiceImpl;
import org.example.sql.conn.ConnectToMySql;
import org.example.work.flow.TrafficAnalysis;
import org.example.work.match.Extract;
import org.example.work.match.MatchResult;
import org.example.work.match.MatchTask;
import org.example.work.match.Matcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @CLassname MainController
 * @Description TODO
 * @Date 2021/5/27 11:06
 * @Created by lenovo
 */
@RestController
@RequestMapping("/")
@CrossOrigin(origins = {"*","null"})
public class MainController {
    @Autowired
    private ServiceImpl service = null;

    @GetMapping(value = "{id}")
    public Result<RestResult> index(@PathVariable String id) {
        int page_id = Integer.parseInt(id);
        Result<RestResult> result = new Result<>();
        Data<RestResult> data = new Data<>();
        RestResult rest = this.service.details(page_id);
        result.setStatus(SocketFlow.Status.OK);
        data.setObject(rest);
        result.setData(data);
        return result;
    }

    @GetMapping(value = "/delete")
    public String delete() {
        this.service.delete();
        return "success";
    }

}
