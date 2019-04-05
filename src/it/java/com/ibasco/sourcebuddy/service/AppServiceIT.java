package com.ibasco.sourcebuddy.service;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

@SpringBootTest
class ApServiceIT {

    private static final Logger log = LoggerFactory.getLogger(ApServiceIT.class);

    @Autowired
    private AppService service;

    @Test
    void test01() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        log.debug("Scheduling task to run after 3 seconds");
        int id = service.runTaskAfter(Duration.ofSeconds(3), () -> {
            try {
                log.debug("Task now Running for 10 seconds");
                Thread.sleep(10000);
                log.debug("Done");
            } catch (InterruptedException e) {
                log.debug("Interrupted exception", e);
            } finally {
                latch.countDown();
            }
        });
        log.debug("Cancelling task");
        //boolean res = service.cancelTask(id, null);
        //log.debug("Task cancelled = {}", res);
        //latch.await(5, TimeUnit.SECONDS);
        latch.await();
    }
}