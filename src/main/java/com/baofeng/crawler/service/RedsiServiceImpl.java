
package com.baofeng.crawler.service;

import com.baofeng.crawler.domain.FetchAsin;
import okhttp3.HttpUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

@Service
public class RedsiServiceImpl implements RedisService {

    private static final String FETCHURLS = "fetchUrls";

    private static final String QUEUEINFO = "quueInfo";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void saveFetchUrls(HttpUrl httpUrl) {
        redisTemplate.opsForValue().setIfAbsent(FETCHURLS, httpUrl);
    }

    @Override
    public void saveQueueInfo(FetchAsin fetchAsin) {
        redisTemplate.boundZSetOps(QUEUEINFO).add(fetchAsin, 1);


    }
}

