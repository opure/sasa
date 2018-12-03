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
/** int i = 1;
 for (Element element : elements) {
 ReviewInfo reviewInfo = new ReviewInfo();
 List<Node> nodes = element.childNodes().get(0).childNodes();
 boolean verified = nodes.get(4).outerHtml().contains("Verifizierter");
 if (verified) {
 reviewInfo.setIsVp(1);
 } else {
 reviewInfo.setIsVp(0);
 }
 //star
 String star = null;
 try {
 star = nodes.get(1).childNodes().get(2).childNodes().get(0).attr("title");
 reviewInfo.setStar(star);
 } catch (Exception e) {
 e.printStackTrace();
 }
 //title
 String title = nodes.get(2).childNodes().get(2).childNodes().get(0).toString();
 reviewInfo.setTitle(title);
 //url
 String reviewAsin = nodes.get(2).childNodes().get(0).attr("href");
 reviewInfo.setReviewUr(reviewAsin);
 reviewInfo.setReviewAsin(reviewAsin.substring(reviewAsin.indexOf("customer-reviews") + 17, reviewAsin.lastIndexOf("/")));
 reviewInfo.setAsin(fetchAsin.getAsin());
 reviewInfo.setSite(fetchAsin.getWebSite());
 //name
 String customerName = ((Element) nodes.get(0)).getElementsByTag("span").first().text();
 reviewInfo.setCustomerName(customerName);
 //date
 String reviewDate = ((Element) nodes.get(2)).getElementsByTag("span").last().text();
 reviewInfo.setReviewTime(reviewDate);
 //content
 String content = ((Element) nodes.get(4)).text();
 reviewInfo.setContent(content);
 //helpful
 String helpfulCount;
 if (nodes.size() == 9) {
 helpfulCount = ((Element) nodes.get(8)).getElementsByTag("span").attr("data-hook", "helpful-vote-statement").get(0).text().substring(0, 3);
 } else if (nodes.size() == 10) {
 helpfulCount = ((Element) nodes.get(9)).getElementsByTag("span").attr("data-hook", "helpful-vote-statement").get(0).text().substring(0, 3);
 } else {
 helpfulCount = ((Element) nodes.get(5)).getElementsByTag("span").attr("data-hook", "helpful-vote-statement").get(0).text().substring(0, 3);
 }
 reviewInfo.setHelpfulCount(helpfulCount);
 reviewInfo.setCreateDate(new Date());
 reviewInfoRepo.save(reviewInfo);
 System.out.println(reviewInfo);
 }
 i++;
 String newUrl = url.substring(0, url.indexOf("?") - 1) + i + url.substring(url.indexOf("?"), url.lastIndexOf("=") + 1) + i;
 FetchReviewInfo fetchReviewInfo = new FetchReviewInfo();
 fetchReviewInfo.setFetchAsin(fetchAsin);
 fetchReviewInfo.setHttpUrl(HttpUrl.get(newUrl));
 queue.add(fetchReviewInfo);*/
