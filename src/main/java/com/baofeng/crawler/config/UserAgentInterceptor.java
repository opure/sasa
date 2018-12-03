package com.baofeng.crawler.config;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Component
public class UserAgentInterceptor implements Interceptor {


    @Override
    public Response intercept(Chain chain) throws IOException {
        BufferedReader fileInputStream = new BufferedReader(new FileReader(ResourceUtils.getFile("classpath:user-agent.txt")));
        List<String> result = new ArrayList<>();
        for (; ; ) {
            String line = fileInputStream.readLine();
            if (line == null)
                break;
            result.add(line);
        }
        Request originalRequest = chain.request();
        Request requestWithUserAgent = originalRequest.newBuilder()
                .header("User-Agent", result.get(new Random().nextInt(2900)))
                .build();
        return chain.proceed(requestWithUserAgent);
    }
}
