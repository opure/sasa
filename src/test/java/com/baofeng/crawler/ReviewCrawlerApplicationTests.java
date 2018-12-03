package com.baofeng.crawler;

import com.baofeng.crawler.domain.FetchAsin;
import com.baofeng.crawler.service.FetchAsinRepo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

    private String url = "%s/product-reviews/%s/ref=cm_cr_arp_d_paging_btm_1?ie=UTF8&reviewerType=all_reviews&pageNumber=1";

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
