package com.baofeng.crawler.domain;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by opure on 2018/12/1.
 */
@Entity
public class FetchAsin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String webSite;

    private String asin;

    private String url;

    private Date createDate;

    @Enumerated(EnumType.STRING)
    private HandleStatus handleStatus = HandleStatus.WAITING;

    private int priority = 0;

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    private Date updateDate;


    public HandleStatus getHandleStatus() {
        return handleStatus;
    }

    public void setHandleStatus(HandleStatus handleStatus) {
        this.handleStatus = handleStatus;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getWebSite() {
        return webSite;
    }

    public void setWebSite(String webSite) {
        this.webSite = webSite;
    }

    public String getAsin() {
        return asin;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdaDate() {
        return updaDate;
    }

    public void setUpdaDate(Date updaDate) {
        this.updaDate = updaDate;
    }
}
