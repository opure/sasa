package com.baofeng.crawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ReviewCrawlerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReviewCrawlerApplication.class, args);
	}
}
