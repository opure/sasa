package com.baofeng.crawler.service;

public interface LockService {

    /**
     * 获取锁
     *
     * @param lockName         名称
     * @param expiresIn    过期时间, ms
     * @param millsToRetry 重试时间, ms
     */
    void getLock(String lockName, Integer expiresIn, Integer millsToRetry);

    /**
     * 获取锁
     *
     * @param lockName      名称
     * @param expiresIn 过期时间, ms
     * @return 是否得到
     */
    boolean getLock(String lockName, Integer expiresIn);

    /**
     * 解锁
     *
     * @param lockName 名称
     */
    void unLock(String lockName);
}
