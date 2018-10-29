
package com.baofeng.crawler;

import com.baofeng.crawler.domain.ReviewInfo;
import com.baofeng.crawler.service.ReviewInfoRepo;
import okhttp3.*;
import okhttp3.internal.NamedRunnable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
public class CrawlerController {
    @Resource
    private ReviewInfoRepo reviewInfoRepo;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .build();
    private final Set<HttpUrl> fetchedUrls = Collections.synchronizedSet(
            new LinkedHashSet<HttpUrl>());
    private final LinkedBlockingQueue<HttpUrl> queue = new LinkedBlockingQueue<>();
    private final ConcurrentHashMap<String, AtomicInteger> hostnames = new ConcurrentHashMap<>();

    private void parallelDrainQueue(int threadCount) {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < 1; i++) {
            executor.execute(new NamedRunnable("Crawler %s", i) {
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
        for (HttpUrl url; (url = queue.take()) != null; ) {
            if (!fetchedUrls.add(url)) {
                continue;
            }

            Thread currentThread = Thread.currentThread();
            String originalName = currentThread.getName();
            currentThread.setName("Crawler " + url.toString());
            try {
                fetch(url);
            } catch (IOException e) {
                System.out.printf("XXX: %s %s%n", url, e);
            } finally {
                currentThread.setName(originalName);
            }
        }
    }

    public void fetch(HttpUrl url) throws IOException {
        // Skip hosts that we've visited many times.
        AtomicInteger hostnameCount = new AtomicInteger();
        AtomicInteger previous = hostnames.putIfAbsent(url.toString(), hostnameCount);
        if (previous != null) hostnameCount = previous;
        if (hostnameCount.incrementAndGet() > 1) return;

        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            String responseSource = response.networkResponse() != null ? ("(network: "
                    + response.networkResponse().code()
                    + " over "
                    + response.protocol()
                    + ")") : "(cache)";
            int responseCode = response.code();

            System.out.printf("%03d: %s %s%n", responseCode, url, responseSource);

            String contentType = response.header("Content-Type");
            if (responseCode != 200 || contentType == null) {
                return;
            }

            MediaType mediaType = MediaType.parse(contentType);
            if (mediaType == null || !mediaType.subtype().equalsIgnoreCase("html")) {
                return;
            }

            parseHtmlData(response, url.toString());

        }
    }

    private void parseHtmlData(Response response, String url) throws IOException {


        Document document = Jsoup.parse(response.body().string(), url);
        // String href = document.select("#cm_cr-review_list>div>span>div>ul>li[class='page-button']>a").last().attr("href");
        //Integer pages = Integer.parseInt(href.substring(href.indexOf("&pageNumber=")));

        for (Element element : document.select("#cm_cr-review_list>div[class='a-section review']")) {
            ReviewInfo reviewInfo = new ReviewInfo();
            List<Node> nodes = element.childNodes().get(0).childNodes();
            boolean verified = nodes.get(3).outerHtml().contains("Verified");
            if (verified) {
                reviewInfo.setIsVp(1);
            } else {
                reviewInfo.setIsVp(0);
            }
            //star
            String star = nodes.get(1).childNodes().get(0).attr("title").substring(0, 3);
            reviewInfo.setStar(star);
            //title
            String title = nodes.get(0).childNodes().get(2).childNodes().get(0).toString();
            reviewInfo.setTitle(title);
            //name
            String customerName = ((Element) nodes.get(1)).getElementsByTag("span").first().text();
            reviewInfo.setCustomerName(customerName);
            //date
            String reviewDate = ((Element) nodes.get(1)).getElementsByTag("span").last().text();
            reviewInfo.setReviewTime(reviewDate);
            //content
            String content = ((Element) nodes.get(3)).text();
            reviewInfo.setContent(content);
            //helpful
            String helpfulCount;
            if (nodes.size() == 9) {
                helpfulCount = ((Element) nodes.get(8)).getElementsByTag("span").attr("data-hook", "helpful-vote-statement").get(0).text().substring(0, 3);
            } else {
                helpfulCount = ((Element) nodes.get(4)).getElementsByTag("span").attr("data-hook", "helpful-vote-statement").get(0).text().substring(0, 3);
            }
            reviewInfo.setHelpfulCount(helpfulCount);
            reviewInfo.setCreateDate(new Date());

           reviewInfoRepo.save(reviewInfo);
            System.out.println(reviewInfo);
  /*          String href = element.attr("href");
            HttpUrl link = response.request().url().resolve(href);
            if (link == null) continue; // URL is either invalid or its scheme isn't http/https.
            queue.add(link.newBuilder().fragment(null).build());*/
        }
    }

    @Scheduled(initialDelay = -1L, fixedDelay = 100000000000000000L)
    public void startFetch() {
        String url = "https://www.amazon.com/Straightener-Entil-Titanium-Professional-Controls/product-reviews/B07FNBKHBB/ref=cm_cr_dp_d_show_all_btm?ie=UTF8&reviewerType=all_reviews";
        this.queue.add(HttpUrl.get(url));
        for (int i = 0; i < 3; i++) {
            queue.add(HttpUrl.get(url + "&pageNumber=" + (i + 2)));
        }
        this.parallelDrainQueue(1);
    }


}
