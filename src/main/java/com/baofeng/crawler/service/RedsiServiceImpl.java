package com.baofeng.crawler.service;

import okhttp3.HttpUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedsiServiceImpl implements RedisService {

    private static final String HTTPURL = "httpUrl";
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void saveHttpUrl(HttpUrl httpUrl) {
        redisTemplate.opsForValue().setIfAbsent(HTTPURL, httpUrl);
    }
}
