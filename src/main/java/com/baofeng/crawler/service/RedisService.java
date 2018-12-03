package com.baofeng.crawler.service;

import com.baofeng.crawler.domain.FetchAsin;
import okhttp3.HttpUrl;

public interface RedisService {

    void saveFetchUrls(HttpUrl httpUrl);

    void saveQueueInfo(FetchAsin fetchAsin);
}
