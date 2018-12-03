package com.baofeng.crawler.config;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Random;

import static com.baofeng.crawler.ReviewCrawlerApplication.userAgents;


@Component
public class UserAgentInterceptor implements Interceptor {


    @Override
    public Response intercept(Chain chain) throws IOException {

        Request originalRequest = chain.request();
        Request requestWithUserAgent = originalRequest.newBuilder()
                .header("User-Agent", userAgents.get(new Random().nextInt(2900)))
                .build();
        return chain.proceed(requestWithUserAgent);
    }
}
