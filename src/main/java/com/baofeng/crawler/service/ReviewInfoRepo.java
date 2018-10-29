package com.baofeng.crawler.service;

import com.baofeng.crawler.domain.ReviewInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by opure on 2018/10/25.
 */
@Repository
public interface ReviewInfoRepo extends JpaRepository<ReviewInfo, Integer> {
}
