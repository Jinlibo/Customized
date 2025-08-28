package org.example.service;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
@Data
@Component
public class RestTemplateRetryService implements DisposableBean {

    private final ScheduledThreadPoolExecutor scheduler;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final int maxAttempts;
    private final long retryDelay;

    public RestTemplateRetryService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        messageConverters.forEach(converter -> {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                ((MappingJackson2HttpMessageConverter) converter).setObjectMapper(objectMapper);
            }
        });
        this.maxAttempts = 5;
        this.retryDelay = 10000;
        scheduler = new ScheduledThreadPoolExecutor(10, r -> {
            Thread t = new Thread(r);
            t.setName("rest-template-retry-thread");
            t.setDaemon(true);
            return t;
        });
        // 最大线程数
        scheduler.setMaximumPoolSize(30);
        // 空闲时间
        scheduler.setKeepAliveTime(60, TimeUnit.SECONDS);
        // 允许核心线程空闲释放
        scheduler.allowCoreThreadTimeOut(true);
        // 拒绝策略
        scheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * 异步发送请求并通过回调处理结果
     */
    public <T> void sendWithRetryAsync(String url, T body,
                                       Consumer<ResponseEntity<?>> onSuccess,
                                       Consumer<Throwable> onFailure) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> entity = new HttpEntity<>(body, headers);

        CompletableFuture<ResponseEntity<?>> futureResult = new CompletableFuture<>();

        Runnable task = new Runnable() {
            int attempt = 0;

            @Override
            public void run() {
                attempt++;
                try {
                    log.info("第 {} 次请求 URL: {}", attempt, url);
                    ResponseEntity<?> response = restTemplate.exchange(url, HttpMethod.POST, entity, Object.class);
                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && Objects.equals(String.valueOf(JSON.parseObject(JSON.toJSONString(response.getBody())).get("code")), "200")) {
                        log.info("请求成功: {}", url);
                        futureResult.complete(response);
                    } else {
                        String respMsg = String.valueOf(JSON.parseObject(JSON.toJSONString(response.getBody())).get("msg"));
                        throw new RuntimeException("url" + url + "请求失败" + (StringUtils.hasText(respMsg) ? "响应:" + respMsg : ""));
                    }
                } catch (Exception e) {
                    log.error("第 {} 次请求失败: {}", attempt, e.getMessage());
                    if (attempt < maxAttempts) {
                        log.info("等待 {} 秒后进行第 {} 次重试", retryDelay / 1000, attempt + 1);
                        scheduler.schedule(this, retryDelay, TimeUnit.MILLISECONDS);
                    } else {
                        log.error("请求失败已达最大次数({})，停止重试: {}", maxAttempts, url);
                        futureResult.completeExceptionally(e);
                    }
                }
            }
        };

        // 注册异步回调
        futureResult
                .thenAccept(onSuccess)           // 成功时执行
                .exceptionally(ex -> {           // 失败时执行
                    onFailure.accept(ex);
                    return null;
                });

        scheduler.submit(task);
    }

    @Override
    public void destroy() {
        scheduler.shutdown();
    }
}
