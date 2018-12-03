package com.baofeng.crawler.domain;

import okhttp3.HttpUrl;

/**
 * Created by opure on 2018/12/1.
 */
public class FetchReviewInfo {

    private HttpUrl httpUrl;

    private FetchAsin fetchAsin;

    public HttpUrl getHttpUrl() {
        return httpUrl;
    }

    public void setHttpUrl(HttpUrl httpUrl) {
        this.httpUrl = httpUrl;
    }

    public FetchAsin getFetchAsin() {
        return fetchAsin;
    }

    public void setFetchAsin(FetchAsin fetchAsin) {
        this.fetchAsin = fetchAsin;
    }
}
