package com.lulobank.sqs_demo;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

@Testcontainers
@SpringBootTest
class SqsDemoApplicationTests {
	private static final List<String> queueNames = List.of("jhon-test", "jhon-test-dlq");

	private static final DockerImageName LOCALSTACK_IMAGE = DockerImageName.parse("localstack/localstack");

	@Container
	public static final LocalStackContainer LOCALSTACK_CONTAINER = new LocalStackContainer(LOCALSTACK_IMAGE)
			.withServices(SQS);

	@Autowired
	private SqsAsyncClient sqsAsyncClient;

	@SpyBean
	private SqsTemplate sqsTemplate;

	@BeforeAll
	static void setUp() {
		queueNames.forEach(SqsDemoApplicationTests::createQueue);
	}

	private static void createQueue(String queueName) {
		try {
			LOCALSTACK_CONTAINER.execInContainer(
					"awslocal",
					"sqs",
					"create-queue",
					"--queue-name",
					queueName
			);
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@AfterEach
	void after() {
		queueNames.forEach(queueName -> {
			sqsAsyncClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(queueName).build());
		});
	}


	@Test
	void contextLoads() {
		var sendResult = sqsTemplate.send("jhon-test", "Laoghaire Yaropolk");
		var receive = sqsTemplate.receive(sqsReceiveOptions -> sqsReceiveOptions.queue("jhon-test-dlq")
				.visibilityTimeout(Duration.ofSeconds(20))
		);

		var payload = (String) receive.get().getPayload();
		System.out.println(payload);
		assertEquals("LAOGHAIRE YAROPOLK", payload);
	}

	protected int numberOfMessagesInQueue(String queueName) {
		var attributes = sqsAsyncClient.getQueueAttributes(builder
						-> builder.queueUrl(queueName)
						.attributeNames(QueueAttributeName.ALL)
						.build())
				.join()
				.attributes();

		return Integer.parseInt(attributes.get(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES));

	}

}
