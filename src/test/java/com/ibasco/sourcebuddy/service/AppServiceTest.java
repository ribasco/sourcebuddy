package com.ibasco.sourcebuddy.service;

import com.ibasco.sourcebuddy.service.impl.AppServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

@ExtendWith(MockitoExtension.class)
class AppServiceTest {

    private static final Logger log = LoggerFactory.getLogger(AppServiceTest.class);

    @Mock
    private ScheduledExecutorService scheduledTaskService;

    @InjectMocks
    private AppService service = new AppServiceImpl();

    @Test
    void test01() {
        service.runTaskAfter(Duration.ofSeconds(10), () -> {
            try {
                log.info("Running for 10 seconds");
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}