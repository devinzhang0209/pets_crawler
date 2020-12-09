package com.zhuayinline.pets.crawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.zhuayinline.pets.crawler.*"})
@MapperScan("com.zhuayinline.pets.crawler.dao")
@EnableSwagger2
@EnableScheduling
public class PetsCrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetsCrawlerApplication.class, args);
    }

}
