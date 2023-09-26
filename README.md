# Web_Page_Recognition
网页识别系统构建

## 介绍
该项目为maven项目，IDE为Intellij IDEA
系统实现，爬取互联网上的排名靠前的网站的所有有效网页，进行网页解析与指纹提取，建立指纹和特征库用于网页相似度的比较

## 项目说明
项目源码存放于src.main.java.org.example文件夹下
auxiliary包存储辅助文件，类似文件存储路径，网页指纹提取相关键值
data包存放原始数据，Alexa网站排名
kit包为工具包，存储一些相关工具类（非本人）
sql包存储Mybatis相关配置，用于连接数据库
work包存储网页爬虫，预处理，网页解析，指纹提取，特征提取，网页快照等相关类

## 初级数据储备阶段
从文件中读取要爬取的网站host，构造初始URL并创建线程（MyThread），执行网页爬取（WebCrawl）和网页预处理（Before，包括解析网页获取DOM树以及获取网页上的超链接），
对预处理结果进行指纹（ExtractFingerprint）和特征词的提取（ExtractEigenword），并入库

## 演示
集成springboot框架，前端简单用thymeleaf写了几个网页（可以实际测，就几行应该能看懂），数据库脚本sdmpi.sql 部分实体类对应sql/model
项目结构：
 src
    main
        java
            org.example
                controller - 控制层
                data - 网站
                kit - 工具包（别动）
                result - REST前端返回结果
                service - 服务层
                sql - 数据库相关， model为实体层 （我这块没改成标准的springboot形式， 无法用@Autowired注解mapper，所以保留了ConnectToMySQL类）
                util - 提取相关的辅助类
                work - 工作中心
                    crawl - 爬虫结构
                    eigenword - 特征词结构定义和提取（区分Eigenword和InvertIndex倒排索引）
                    fingerprint - 指纹提取
                    flow - 流量解析 定义各种头部字段
                    match - 匹配中心 主要关注Matcher 二轮过滤和指纹匹配
                    parse - 网页解析 类比jsoup定义的数据结构
                    thread - 多线程爬取网站数据
        resource
            mapper - match-mapper.xml 对应 MatchMapper的数据库配置文件（参考Mybatis）
            META-INF - 自己生成的。
            properties - 定义jdbc相关配置，（springboot用不到）
            static - 网页相关静态资源 （没用到没写）
            templates - 前端网页
            application.yml - springboot标准配置文件
            log4j - 日志相关
            Mybatis - SpringMVC中的配置。
    test - 测试类

## git 

你可以找到源码在 https://github.com/1170301027/Web_Page_Recognition