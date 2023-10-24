package com.lulobank.sqs_demo.adapter;

import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsListener {
    private final SqsTemplate sqsTemplate;

    @SqsListener("jhon-test")
    public void listen(String message) {
        var sendResult = sqsTemplate.send("jhon-test-dlq", message.toUpperCase());
        log.info("messageId {}", sendResult.messageId());
    }

}
