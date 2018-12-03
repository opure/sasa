package com.baofeng.crawler;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.ResourceUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnableScheduling
public class ReviewCrawlerApplication {

    public static final List<String> userAgents = new ArrayList<>();


    public static void main(String[] args) {
        SpringApplication.run(ReviewCrawlerApplication.class, args);
    }

    @Bean
    CommandLineRunner init() {

        return args -> {
            BufferedReader fileInputStream = new BufferedReader(new FileReader(ResourceUtils.getFile("classpath:user-agent.txt")));
            for (; ; ) {
                String line = fileInputStream.readLine();
                if (line == null)
                    break;
                userAgents.add(line);
            }
        };

    }
}
