package org.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @CLassname WorkerApplication
 * @Description TODO
 * @Date 2021/5/18 11:47
 * @Created by lenovo
 */
@SpringBootApplication
//@MapperScan(basePackages={"org.example.sql.mapper"})
public class WorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkerApplication.class, args);
    }
}
