package com.baofeng.crawler.service;

import com.baofeng.crawler.domain.FetchAsin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Created by opure on 2018/12/1.
 */
@Repository
public interface FetchAsinRepo extends JpaRepository<FetchAsin, Integer> {

    @Query(value = "SELECT * FROM fetch_asin WHERE handle_status = 'WAITING' ORDER BY priority DESC  LIMIT 1", nativeQuery = true)
    FetchAsin getOneTaskExecute();
}
