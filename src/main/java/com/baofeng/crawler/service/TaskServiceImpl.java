package com.baofeng.crawler.service;

import com.baofeng.crawler.domain.FetchAsin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.baofeng.crawler.domain.HandleStatus.WAITING;
import static com.baofeng.crawler.utils.Constant.SECOND_IN_MILL;

/**
 * Created by opure on 2018/12/3.
 */
@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private LockService lockService;

    @Autowired
    private FetchAsinRepo fetchAsinRepo;

    private static final String ASINLOCK = "asinLock";

    @Override
    public FetchAsin getOneFetchAsin() {
        lockService.getLock(ASINLOCK, 5 * SECOND_IN_MILL, 2 * SECOND_IN_MILL);
        FetchAsin fetchAsin = null;
        try {
            fetchAsin = fetchAsinRepo.getOneTaskExecute();
        } catch (Exception e) {
            if (fetchAsin != null) {
                fetchAsin.setHandleStatus(WAITING);
                fetchAsinRepo.save(fetchAsin);
            }
        }
        lockService.unLock(ASINLOCK);
        return fetchAsin;
    }
}
