package com.baofeng.crawler.service;

import com.baofeng.crawler.domain.FetchAsin;
import com.baofeng.crawler.domain.ReviewInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.NodeList;

import java.util.Date;
import java.util.List;

import static com.baofeng.crawler.utils.XpathUtil.getNodeList;

/**
 * Created by opure on 2018/12/3.
 */
@Service
public class XpathParseImpl implements ParseHtmlService {

    @Autowired
    private ReviewInfoRepo reviewInfoRepo;

    private static final String VERIFIED = "//div[starts-with(@id,'customer_review-')]/div//span[@data-hook='avp-badge']";
    private static final String STAR = "//div[starts-with(@id,'customer_review-')]/div//i[@data-hook = 'review-star-rating']";
    private static final String TITLE = "//div[starts-with(@id,'customer_review-')]/div//a[@data-hook = 'review-title']";
    private static final String CONTENT = "//div[starts-with(@id,'customer_review-')]/div//span[@data-hook = 'review-body']";
    private static final String REVIEWDATE = "//div[starts-with(@id,'customer_review-')]//span[@data-hook = 'review-date']";
    private static final String CUSTOMERNAME = "//div[starts-with(@id,'customer_review-')]/div//span[@class='a-profile-name']";
    private static final String HELPFULCOUNT = "//div[starts-with(@id,'customer_review-')]/div/div//span[@class='cr-vote']//span[@data-hook='helpful-vote-statement']";


    @Override
    public List<Element> parseHtmlDate(String html, String url, FetchAsin fetchAsin) {
        Document document = Jsoup.parse(html, url);
        List<Element> elements = document.select("#cm_cr-review_list>div[class='a-section review']");
        for (Element element : elements) {
            ReviewInfo reviewInfo = new ReviewInfo();
            String innerHtml = element.html();
            //是否vp
            NodeList nodeList = getNodeList(innerHtml, VERIFIED);
            if (nodeList.getLength() == 0) {
                reviewInfo.setIsVp(0);
            } else {
                reviewInfo.setIsVp(1);
            }
            NodeList start = getNodeList(innerHtml, STAR);
            String text = start.item(0).getAttributes().getNamedItem("class").getTextContent();
            //获取评分
            reviewInfo.setStar(text.substring(text.indexOf("a-star-") + 7, text.indexOf("a-star-") + 8));
            //获取title
            NodeList title = getNodeList(innerHtml, TITLE);
            reviewInfo.setTitle(title.item(0).getTextContent());
            //获取评论链接
            reviewInfo.setReviewUr(title.item(0).getAttributes().getNamedItem("href").getTextContent());
            //获取content
            NodeList content = getNodeList(innerHtml, CONTENT);
            reviewInfo.setContent(content.item(0).getTextContent());
            //获取评论时间
            NodeList reviewDate = getNodeList(innerHtml, REVIEWDATE);
            reviewInfo.setReviewTime(reviewDate.item(0).getTextContent());
            //获取reviewAsin
            reviewInfo.setReviewAsin(element.attr("id"));
            //获取留评姓名
            NodeList customerList = getNodeList(innerHtml, CUSTOMERNAME);
            reviewInfo.setCustomerName(customerList.item(0).getTextContent());
            //获取点赞数
            NodeList helpfulCount = getNodeList(innerHtml, HELPFULCOUNT);
            if (helpfulCount.getLength() > 0) {
                reviewInfo.setHelpfulCount(helpfulCount.item(0).getTextContent());
            }
            reviewInfo.setAsin(fetchAsin.getAsin());
            reviewInfo.setSite(fetchAsin.getWebSite());
            reviewInfo.setCreateDate(new Date());
            reviewInfo.setFullText(element.text());
            reviewInfoRepo.save(reviewInfo);
        }
        return elements;
    }
}
