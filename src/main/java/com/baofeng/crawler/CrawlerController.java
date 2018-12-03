
package com.baofeng.crawler;

import com.baofeng.crawler.config.UserAgentInterceptor;
import com.baofeng.crawler.domain.FetchAsin;
import com.baofeng.crawler.domain.FetchReviewInfo;
import com.baofeng.crawler.domain.ReviewInfo;
import com.baofeng.crawler.service.FetchAsinRepo;
import com.baofeng.crawler.service.ReviewInfoRepo;
import okhttp3.*;
import okhttp3.internal.NamedRunnable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
public class CrawlerController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ReviewInfoRepo reviewInfoRepo;

    @Autowired
    private FetchAsinRepo fetchAsinRepo;

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
        if (hostnameCount.incrementAndGet() > 1) return;

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

            parseHtmlData(response, fetchReviewInfo.getHttpUrl().toString(), fetchReviewInfo.getFetchAsin());

        }
    }

    public void saveReviewInfo(Document document, String webSite, String url, FetchAsin fetchAsin) {

        List<Element> elements = document.select("#cm_cr-review_list>div[class='a-section review']");
        List<String> sites = new ArrayList<>(Arrays.asList("US", "CA", "UK", "IT", "FR"));

        if (sites.contains(webSite)) {
            int i = 1;
            for (Element element : elements) {
                ReviewInfo reviewInfo = new ReviewInfo();
                List<Node> nodes = element.childNodes().get(0).childNodes();
                boolean verified = nodes.get(3).outerHtml().contains("Verif");
                if (verified) {
                    reviewInfo.setIsVp(1);
                } else {
                    reviewInfo.setIsVp(0);
                }
                //star
                String star;
                try {
                    star = nodes.get(1).childNodes().get(0).attr("title");
                    if (StringUtils.isEmpty(star)) {
                        star = nodes.get(1).childNodes().get(2).childNodes().get(0).attr("title");
                    }
                    reviewInfo.setStar(star);
                } catch (Exception e) {
                    logger.error("get review start fail,this url is {},stackTrace is {}", url, e);
                    e.printStackTrace();
                }
                //title
                try {
                    String title = nodes.get(1).childNodes().get(2).childNodes().get(0).toString();
                    if (StringUtils.isEmpty(title)) {
                        title = nodes.get(2).childNodes().get(2).childNodes().get(0).toString();
                    }
                    reviewInfo.setTitle(title);
                } catch (Exception e) {
                    logger.error("get review  fail,this url is {},stackTrace is {}", url, e);
                    e.printStackTrace();
                }

                //url
                String reviewAsin = null;
                try {
                    reviewAsin = nodes.get(1).childNodes().get(2).attr("href");
                    if (StringUtils.isEmpty(reviewAsin)) {
                        reviewAsin = nodes.get(2).childNodes().get(0).attr("href");
                    }
                    reviewInfo.setReviewUr(reviewAsin);
                } catch (Exception e) {
                    logger.error("get reviewUrl fail,this url is {},stackTrace is {}", url, e);
                    e.printStackTrace();
                }
                try {
                    reviewInfo.setReviewAsin(reviewAsin.substring(reviewAsin.indexOf("customer-reviews") + 17, reviewAsin.lastIndexOf("/")));
                    reviewInfo.setAsin(fetchAsin.getAsin());
                } catch (Exception e) {
                    logger.error("get reviewAsin is fail,this url is {},stackTrace is {}", url, e);
                    e.printStackTrace();
                }
                reviewInfo.setSite(fetchAsin.getWebSite());
                //name
                String customerName;
                try {
                    customerName = ((Element) nodes.get(0)).getElementsByTag("span").first().text();
                    reviewInfo.setCustomerName(customerName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //date
                String reviewDate;
                try {
                    reviewDate = ((Element) nodes.get(2)).getElementsByTag("span").last().text();
                    if (StringUtils.isEmpty(reviewDate)) {
                        reviewDate = nodes.get(3).childNodes().get(0).toString();
                    }
                    reviewInfo.setReviewTime(reviewDate);
                } catch (Exception e) {
                    logger.error("get reviewDate is fail,this url is {},stackTrace is {}", url, e);
                }
                //content
                String content;
                try {
                    content = ((Element) nodes.get(4)).text();
                    reviewInfo.setContent(content);
                } catch (Exception e) {
                    logger.error("get content is fail,this url is {},stackTrace is {}", url, e);
                    e.printStackTrace();
                }
                //helpful

                try {
                    String helpfulCount;
                    if (nodes.size() == 9) {
                        helpfulCount = ((Element) nodes.get(8)).getElementsByTag("span").attr("data-hook", "helpful-vote-statement").get(0).text().substring(0, 3);
                    } else if (nodes.size() == 10) {
                        helpfulCount = ((Element) nodes.get(9)).getElementsByTag("span").attr("data-hook", "helpful-vote-statement").get(0).text().substring(0, 3);
                    } else {
                        helpfulCount = ((Element) nodes.get(5)).getElementsByTag("span").attr("data-hook", "helpful-vote-statement").get(0).text().substring(0, 3);
                    }
                    reviewInfo.setHelpfulCount(helpfulCount);
                } catch (Exception e) {

                }

                reviewInfo.setCreateDate(new Date());
                reviewInfoRepo.save(reviewInfo);
                System.out.println(reviewInfo);
            }
            i++;
            String newUrl = url.substring(0, url.indexOf("?") - 1) + i + url.substring(url.indexOf("?"), url.lastIndexOf("=") + 1) + i;
            FetchReviewInfo fetchReviewInfo = new FetchReviewInfo();
            fetchReviewInfo.setFetchAsin(fetchAsin);
            fetchReviewInfo.setHttpUrl(HttpUrl.get(newUrl));
            queue.add(fetchReviewInfo);

        } else if (webSite.equals("DE"))

        {
            /** int i = 1;
             for (Element element : elements) {
             ReviewInfo reviewInfo = new ReviewInfo();
             List<Node> nodes = element.childNodes().get(0).childNodes();
             boolean verified = nodes.get(4).outerHtml().contains("Verifizierter");
             if (verified) {
             reviewInfo.setIsVp(1);
             } else {
             reviewInfo.setIsVp(0);
             }
             //star
             String star = null;
             try {
             star = nodes.get(1).childNodes().get(2).childNodes().get(0).attr("title");
             reviewInfo.setStar(star);
             } catch (Exception e) {
             e.printStackTrace();
             }
             //title
             String title = nodes.get(2).childNodes().get(2).childNodes().get(0).toString();
             reviewInfo.setTitle(title);
             //url
             String reviewAsin = nodes.get(2).childNodes().get(0).attr("href");
             reviewInfo.setReviewUr(reviewAsin);
             reviewInfo.setReviewAsin(reviewAsin.substring(reviewAsin.indexOf("customer-reviews") + 17, reviewAsin.lastIndexOf("/")));
             reviewInfo.setAsin(fetchAsin.getAsin());
             reviewInfo.setSite(fetchAsin.getWebSite());
             //name
             String customerName = ((Element) nodes.get(0)).getElementsByTag("span").first().text();
             reviewInfo.setCustomerName(customerName);
             //date
             String reviewDate = ((Element) nodes.get(2)).getElementsByTag("span").last().text();
             reviewInfo.setReviewTime(reviewDate);
             //content
             String content = ((Element) nodes.get(4)).text();
             reviewInfo.setContent(content);
             //helpful
             String helpfulCount;
             if (nodes.size() == 9) {
             helpfulCount = ((Element) nodes.get(8)).getElementsByTag("span").attr("data-hook", "helpful-vote-statement").get(0).text().substring(0, 3);
             } else if (nodes.size() == 10) {
             helpfulCount = ((Element) nodes.get(9)).getElementsByTag("span").attr("data-hook", "helpful-vote-statement").get(0).text().substring(0, 3);
             } else {
             helpfulCount = ((Element) nodes.get(5)).getElementsByTag("span").attr("data-hook", "helpful-vote-statement").get(0).text().substring(0, 3);
             }
             reviewInfo.setHelpfulCount(helpfulCount);
             reviewInfo.setCreateDate(new Date());
             reviewInfoRepo.save(reviewInfo);
             System.out.println(reviewInfo);
             }
             i++;
             String newUrl = url.substring(0, url.indexOf("?") - 1) + i + url.substring(url.indexOf("?"), url.lastIndexOf("=") + 1) + i;
             FetchReviewInfo fetchReviewInfo = new FetchReviewInfo();
             fetchReviewInfo.setFetchAsin(fetchAsin);
             fetchReviewInfo.setHttpUrl(HttpUrl.get(newUrl));
             queue.add(fetchReviewInfo);*/
        }

    }


    private void parseHtmlData(Response response, String url, FetchAsin fetchAsin) throws IOException {

        Document document = Jsoup.parse(response.body().string(), url);
        List<Element> elements = document.select("#cm_cr-review_list>div[class='a-section review']");

        saveReviewInfo(document, fetchAsin.getWebSite(), url, fetchAsin);
    }

    @Scheduled(initialDelay = -1L, fixedDelay = 100000000000000000L)
    public void startFetch() {
        List<FetchAsin> fetchAsins = fetchAsinRepo.findAll();


        fetchAsins.forEach(x -> {
            FetchReviewInfo fetchReviewInfo = new FetchReviewInfo();
            fetchReviewInfo.setHttpUrl(HttpUrl.get((x.getUrl())));
            fetchReviewInfo.setFetchAsin(x);
            this.queue.add(fetchReviewInfo);
        });

        this.parallelDrainQueue(1);
    }


}
