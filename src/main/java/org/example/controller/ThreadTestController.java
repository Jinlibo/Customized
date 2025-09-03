package org.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

@RestController
public class ThreadTestController {
    @Qualifier("customTaskExecutor")
    @Autowired
    private TaskExecutor taskExecutor;

    @GetMapping("/sync")
    public String syncMethod() {
        String threadName = Thread.currentThread().getName();
        System.out.println("同步处理线程: " + threadName);
        return "同步响应，线程: " + threadName;
    }

    @GetMapping("/async-callable")
    public Callable<String> asyncCallable() {
        String controllerThread = Thread.currentThread().getName();
        System.out.println("Controller执行线程: " + controllerThread);

        return () -> {
            String asyncThread = Thread.currentThread().getName();
            System.out.println("异步任务执行线程: " + asyncThread);

            Thread.sleep(500);
            return String.format("Controller线程: %s, 异步执行线程: %s",
                    controllerThread, asyncThread);
        };
    }

    @GetMapping("/async-future")
    public CompletableFuture<String> asyncFuture() {
        String controllerThread = Thread.currentThread().getName();
        System.out.println("Controller执行线程: " + controllerThread);

        return CompletableFuture.supplyAsync(() -> {
            String asyncThread = Thread.currentThread().getName();
            System.out.println("CompletableFuture执行线程: " + asyncThread);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return String.format("Controller线程: %s, 异步执行线程: %s",
                    controllerThread, asyncThread);
        });
    }

    @GetMapping("/async-future-custom")
    public CompletableFuture<String> testFutureCustom() {
        String controllerThread = Thread.currentThread().getName();
        System.out.println("Controller执行线程: " + controllerThread);
        return CompletableFuture.supplyAsync(() -> {
            String asyncThread = Thread.currentThread().getName();
            System.out.println("CompletableFuture执行线程: " + asyncThread);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return String.format("Controller线程: %s, 异步执行线程: %s",
                    controllerThread, asyncThread);
        }, taskExecutor);
    }
}