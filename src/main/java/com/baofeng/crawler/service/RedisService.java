package com.baofeng.crawler.service;

import okhttp3.HttpUrl;

public interface RedisService {

    void saveFetchUrls(HttpUrl httpUrl);

    void saveQueueUrls(HttpUrl httpUrl);
}
