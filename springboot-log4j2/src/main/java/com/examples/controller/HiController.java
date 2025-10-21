package com.examples.controller;

import com.alibaba.ttl.TtlRunnable;
import com.examples.common.TraceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
public class HiController {

    ExecutorService es = Executors.newFixedThreadPool(3);

    @GetMapping("/hi")
    public String hi() {
        log.info("hi");
        log.info("traceId: {}", TraceContext.getTraceId());

        es.execute(TtlRunnable.get(() -> log.info("thread hi")));
        
        return "hi";
    }

}
