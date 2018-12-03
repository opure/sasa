
package com.baofeng.crawler;

import com.baofeng.crawler.config.UserAgentInterceptor;
import com.baofeng.crawler.domain.FetchAsin;
import com.baofeng.crawler.domain.FetchReviewInfo;
import com.baofeng.crawler.service.FetchAsinRepo;
import com.baofeng.crawler.service.ParseHtmlService;
import com.baofeng.crawler.service.TaskService;
import okhttp3.*;
import okhttp3.internal.NamedRunnable;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
public class CrawlerController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    private ParseHtmlService parseHtmlService;

    @Autowired
    private FetchAsinRepo fetchAsinRepo;

    @Autowired
    private TaskService taskService;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new UserAgentInterceptor())
            .build();
    private final Set<HttpUrl> fetchedUrls = Collections.synchronizedSet(
            new LinkedHashSet<HttpUrl>());
    private final LinkedBlockingQueue<FetchReviewInfo> queue = new LinkedBlockingQueue<>();
    private final ConcurrentHashMap<String, AtomicInteger> hostnames = new ConcurrentHashMap<>();


    private void parallelDrainQueue(int threadCount) {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < 1; i++) {
            executor.execute(new NamedRunnable("Review Crawler %s", i) {
                @Override
                protected void execute() {
                    try {
                        drainQueue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        executor.shutdown();
    }

    private void drainQueue() throws Exception {

        for (FetchReviewInfo reviewInfo; (reviewInfo = queue.take()) != null; ) {
            HttpUrl url = reviewInfo.getHttpUrl();
            if (!fetchedUrls.add(url)) {
                continue;
            }

            Thread currentThread = Thread.currentThread();
            String originalName = currentThread.getName();
            currentThread.setName("Crawler " + url.toString());
            try {
                fetch(reviewInfo);
            } catch (IOException e) {
                System.out.printf("XXX: %s %s%n", url, e);
            } finally {
                currentThread.setName(originalName);
            }
        }
    }

    public void fetch(FetchReviewInfo fetchReviewInfo) throws IOException {
        // Skip hosts that we've visited many times.
        AtomicInteger hostnameCount = new AtomicInteger();
        AtomicInteger previous = hostnames.putIfAbsent(fetchReviewInfo.getHttpUrl().toString(), hostnameCount);
        if (previous != null) hostnameCount = previous;
        if (hostnameCount.incrementAndGet() > 2) return;

        Request request = new Request.Builder()
                .url(fetchReviewInfo.getHttpUrl())
                .build();
        try (Response response = client.newCall(request).execute()) {
            String responseSource = response.networkResponse() != null ? ("(network: "
                    + response.networkResponse().code()
                    + " over "
                    + response.protocol()
                    + ")") : "(cache)";
            int responseCode = response.code();

            System.out.printf("%03d: %s %s%n", responseCode, fetchReviewInfo.getHttpUrl(), responseSource);

            String contentType = response.header("Content-Type");
            if (responseCode != 200 || contentType == null) {
                this.queue.add(fetchReviewInfo);
                return;
            }

            MediaType mediaType = MediaType.parse(contentType);
            if (mediaType == null || !mediaType.subtype().equalsIgnoreCase("html")) {
                return;
            }
            if (!StringUtils.isEmpty(response)) {
                parseHtmlData(response, fetchReviewInfo.getHttpUrl().toString(), fetchReviewInfo.getFetchAsin());
            }
        }
    }


    private void parseHtmlData(Response response, String url, FetchAsin fetchAsin) {
        List<Element> elements = null;
        try {
            elements = parseHtmlService.parseHtmlDate(response.body().string(), url, fetchAsin);
        } catch (IOException e) {
            logger.error("get html data fail", e);
            e.printStackTrace();
        }
        //自动增加下一页的翻页
        if (elements.size() > 0) {
            String pageNumber = url.substring(url.lastIndexOf("=") + 1, url.length());
            int finalNumber = Integer.parseInt(pageNumber) + 1;
            String newUrl = url.substring(0, url.indexOf("cm_cr_arp_d_paging_btm_")) + "cm_cr_arp_d_paging_btm_" + finalNumber + url.substring(url.indexOf("?"), url.lastIndexOf("=") + 1) + finalNumber;
            FetchReviewInfo fetchReviewInfo = new FetchReviewInfo();
            fetchReviewInfo.setFetchAsin(fetchAsin);
            fetchReviewInfo.setHttpUrl(HttpUrl.get(newUrl));
            queue.add(fetchReviewInfo);
        }
    }

    @Scheduled(initialDelay = -1L, fixedDelay = 100000000000000000L)
    public void startFetch() {
        FetchAsin oneFetchAsin = taskService.getOneFetchAsin();
        if (null == oneFetchAsin) {
            return;
        }
        FetchReviewInfo fetchReviewInfo = new FetchReviewInfo();
        fetchReviewInfo.setHttpUrl(HttpUrl.get((oneFetchAsin.getUrl())));
        fetchReviewInfo.setFetchAsin(oneFetchAsin);
        this.queue.add(fetchReviewInfo);
        this.parallelDrainQueue(5);
    }
}
