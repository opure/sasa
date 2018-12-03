package com.baofeng.crawler;

import com.baofeng.crawler.domain.FetchAsin;
import com.baofeng.crawler.domain.ReviewInfo;
import com.baofeng.crawler.service.FetchAsinRepo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ReviewCrawlerApplicationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FetchAsinRepo fetchAsinRepo;

    @Autowired
    private RedisTemplate<String, ReviewInfo> redisTemplate;

    private String url = "%s/product-reviews/%s/ref=cm_cr_arp_d_paging_btm_1?ie=UTF8&reviewerType=all_reviews&pageNumber=1";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void test() throws Exception {

        // 保存字符串
        stringRedisTemplate.opsForValue().set("aaa", "111");
        Assert.assertEquals("111", stringRedisTemplate.opsForValue().get("aaa"));

    }


    @Test
    public void testRedisSave() throws Exception {

        // 保存对象
        for (int i = 0; i < 10 ; i++) {

        }
        ReviewInfo reviewInfo = new ReviewInfo();
        reviewInfo.setCustomerName("123");
        reviewInfo.setFullText("456");
        redisTemplate.opsForValue().setIfAbsent("123", reviewInfo);
        Assert.assertEquals("123", redisTemplate.opsForValue().get("123").getCustomerName());
    }


    @Test
    public void testSet() {
        SetOperations<String, ReviewInfo> setOperations = redisTemplate.opsForSet();
        for (int i = 0; i < 5; i++) {
            ReviewInfo reviewInfo = new ReviewInfo();
            reviewInfo.setCustomerName("123");
            reviewInfo.setFullText("456");
            setOperations.add("set1", reviewInfo);
        }
        ReviewInfo reviewInfo = new ReviewInfo();
        reviewInfo.setCustomerName("125");
        reviewInfo.setFullText("456");
        setOperations.add("set1", reviewInfo);
        System.out.println(setOperations.pop("set1"));
    }


    @Test
    public void contextLoads() {
        List<Map<String, Object>> maps = jdbcTemplate.queryForList("SELECT\n" +
                "DISTINCT(uod.asin),uo.site,uo.web_site\n" +
                "FROM\n" +
                "\torder_task ot\n" +
                "INNER JOIN user_order uo on ot.user_order_id = uo.id\n" +
                "INNER JOIN user_order_details uod on uod.user_order_id = uo.id\n" +
                "INNER JOIN amazon_account aa on ot.amazon_account_id = aa.id\n" +
                "INNER JOIN email e on e.id = aa.email_id\n" +
                "INNER JOIN order_comment oc ON ot.id = oc.order_task_id\n" +
                "WHERE\n" +
                " oc.`status` = 'FINISH'");
        maps.forEach(x -> {
            FetchAsin fetchAsin = new FetchAsin();
            if (x.get("site").toString().equals("US")) {
                fetchAsin.setUrl(String.format(url, "https://www.amazon.com", x.get("asin").toString()));
            } else if (x.get("site").toString().equals("DE")) {
                fetchAsin.setUrl(String.format(url, "https://www.amazon.de", x.get("asin").toString()));
            } else if (x.get("site").toString().equals("CA")) {
                fetchAsin.setUrl(String.format(url, "https://www.amazon.ca", x.get("asin").toString()));
            } else if (x.get("site").toString().equals("FR")) {
                fetchAsin.setUrl(String.format(url, "https://www.amazon.fr", x.get("asin").toString()));
            } else if (x.get("site").toString().equals("ES")) {
                fetchAsin.setUrl(String.format(url, "https://www.amazon.es", x.get("asin").toString()));
            } else if (x.get("site").toString().equals("UK")) {
                fetchAsin.setUrl(String.format(url, "https://www.amazon.co.uk", x.get("asin").toString()));
            } else if (x.get("site").toString().equals("IT")) {
                fetchAsin.setUrl(String.format(url, "https://www.amazon.it", x.get("asin").toString()));
            }
            fetchAsin.setCreateDate(new Date());
            fetchAsin.setAsin(x.get("asin").toString());
            fetchAsin.setWebSite(x.get("site").toString());
            fetchAsinRepo.save(fetchAsin);
        });

    }

}
