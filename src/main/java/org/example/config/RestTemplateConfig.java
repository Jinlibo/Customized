package org.example.config;


import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        // 设置最大连接数
        connectionManager.setMaxTotal(100);
        // 设置每个路由的最大连接数
        connectionManager.setDefaultMaxPerRoute(20);


        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                // 定期清理空闲连接
                .evictIdleConnections(30, TimeUnit.SECONDS)
                //设置Keep-alive
                .setConnectionTimeToLive(60, TimeUnit.SECONDS)
                .build();


        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        // 设置 connect timeout
        factory.setConnectTimeout(10000);
        //设置read timeout
        factory.setReadTimeout(60000);

        return builder.requestFactory(() -> factory).build();
    }
}