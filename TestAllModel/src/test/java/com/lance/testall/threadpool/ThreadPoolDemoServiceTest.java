package com.lance.testall.threadpool;

import com.lance.testall.threadpool.dto.SubmitRequest;
import com.lance.testall.threadpool.dto.SubmitResponse;
import com.lance.testall.threadpool.service.ThreadPoolDemoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ThreadPoolDemoServiceTest {

    @Autowired
    private ThreadPoolDemoService demoService;

    @Test
    void submit_smallBatch_allSuccess() {
        SubmitRequest request = new SubmitRequest();
        request.setTaskCount(5);
        request.setWorkDelayMs(50);
        request.setWaitForComplete(true);
        request.setBatchTag("unit-test");

        SubmitResponse response = demoService.submit(request);
        assertEquals(5, response.getSubmitted());
        assertEquals(5, response.getSuccess());
        assertEquals(0, response.getRejected());
        assertTrue(response.getElapsedMs() < 5 * 50L);
    }
}
