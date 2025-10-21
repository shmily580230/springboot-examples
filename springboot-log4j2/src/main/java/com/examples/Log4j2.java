package com.examples;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class Log4j2 {
    public static void main(String[] args) {
        SpringApplication.run(Log4j2.class, args);
        long s = System.currentTimeMillis();
        for (int i = 0; i < 100_000; i++) {
            log.debug("debug");
            log.info("info");
            log.warn("warn");
            log.error("error");
        }
        log.info("time: {}", System.currentTimeMillis() - s);
    }
}