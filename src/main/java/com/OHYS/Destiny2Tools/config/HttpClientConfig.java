package com.OHYS.Destiny2Tools.config;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {

    @Bean
    public CloseableHttpClient httpClient() {
        return HttpClients.custom()
                .setMaxConnTotal(100)  // 最大连接数
                .setMaxConnPerRoute(20) // 每个路由的最大连接数
                .build();
    }
}