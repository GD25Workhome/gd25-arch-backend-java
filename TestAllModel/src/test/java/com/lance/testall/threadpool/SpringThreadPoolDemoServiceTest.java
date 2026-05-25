package com.lance.testall.threadpool;

import com.lance.testall.threadpool.dto.SubmitRequest;
import com.lance.testall.threadpool.dto.SubmitResponse;
import com.lance.testall.threadpool.entity.ExecutorType;
import com.lance.testall.threadpool.service.SpringThreadPoolDemoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class SpringThreadPoolDemoServiceTest {

    @Autowired
    private SpringThreadPoolDemoService springDemoService;

    @Test
    void submit_smallBatch_allSuccess() {
        SubmitRequest request = new SubmitRequest();
        request.setTaskCount(5);
        request.setWorkDelayMs(50);
        request.setWaitForComplete(true);
        request.setBatchTag("spring-unit-test");

        SubmitResponse response = springDemoService.submit(request);
        assertEquals(ExecutorType.SPRING, response.getExecutorType());
        assertEquals(5, response.getSubmitted());
        assertEquals(5, response.getSuccess());
        assertEquals(0, response.getRejected());
    }
}
