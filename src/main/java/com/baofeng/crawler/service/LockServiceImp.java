package com.baofeng.crawler.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class LockServiceImp implements LockService {

    @Autowired
    private RedisTemplate<String, String> lockRedisTemplate;

    private Logger logger = LoggerFactory.getLogger(LockServiceImp.class);

    @Override
    public boolean getLock(String lockName, Integer expiresIn) {
        Boolean absent = lockRedisTemplate.opsForValue().setIfAbsent(lockName, "true");
        if (absent)
            lockRedisTemplate.opsForValue().set(lockName, "true", expiresIn, TimeUnit.MILLISECONDS);
        else
            logger.warn(String.format("get lock failed, name: '%s'", lockName));
        return absent;
    }


    @Override
    public void unLock(String lockName) {
        lockRedisTemplate.delete(lockName);
    }

    public void getLock(String lockName, Integer expiresIn, Integer millsToRetry) {

        Boolean absent = lockRedisTemplate.opsForValue().setIfAbsent(lockName, "true");
        if (!absent) {
            // 如果发现过期时间为-1，重置
            Long lockExpire = lockRedisTemplate.getExpire(lockName);
            if (lockExpire == -1)
                lockRedisTemplate.opsForValue().set(lockName, "true", expiresIn, TimeUnit.MILLISECONDS);

            logger.warn(String.format("%s is locked, will try latter...", lockName));
            sleep(new Random(47).nextInt(millsToRetry));
            getLock(lockName, expiresIn, millsToRetry);
        } else
            lockRedisTemplate.opsForValue().set(lockName, "true", expiresIn, TimeUnit.MILLISECONDS);
    }

    private static void sleep(Integer mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
