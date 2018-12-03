package com.baofeng.crawler.domain;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by opure on 2018/10/25.
 */
@Entity
public class ReviewInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String star;

    @Column(length = 2000)
    private String title;

    private String asin;

    private String site;

    private String reviewAsin;

    private String reviewUr;

    private String customerName;

    //1 是 0 不是
    private Integer isVp;

    public String getAsin() {
        return asin;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getReviewAsin() {
        return reviewAsin;
    }

    public void setReviewAsin(String reviewAsin) {
        this.reviewAsin = reviewAsin;
    }

    public String getReviewUr() {
        return reviewUr;
    }

    public void setReviewUr(String reviewUr) {
        this.reviewUr = reviewUr;
    }

    @Lob
    private String content;

    @Lob
    private String fullText;

    private String helpfulCount;

    private String reviewTime;

    private Date createDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStar() {
        return star;
    }

    public void setStar(String star) {
        this.star = star;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Integer getIsVp() {
        return isVp;
    }

    public void setIsVp(Integer isVp) {
        this.isVp = isVp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getHelpfulCount() {
        return helpfulCount;
    }

    public void setHelpfulCount(String helpfulCount) {
        this.helpfulCount = helpfulCount;
    }

    public String getReviewTime() {

        return reviewTime;
    }

    public String getFullText() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    public void setReviewTime(String reviewTime) {
        this.reviewTime = reviewTime;
    }

    @Override
    public String toString() {
        return "ReviewInfo{" +
                "id=" + id +
                ", star='" + star + '\'' +
                ", title='" + title + '\'' +
                ", customerName='" + customerName + '\'' +
                ", isVp=" + isVp +
                ", content='" + content + '\'' +
                ", helpfulCount='" + helpfulCount + '\'' +
                ", reviewTime='" + reviewTime + '\'' +
                ", createDate=" + createDate +
                '}';
    }
}
