package com.thiyagu.reactive.sqs

import com.thiyagu.reactive.domain.MessageDecorator
import com.thiyagu.reactive.domain.SqsConfig
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse
import software.amazon.awssdk.services.sqs.model.Message
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse
import java.util.concurrent.CompletableFuture
import kotlin.test.assertEquals

class SqsAccessorTest {

    private val sqsConfig = SqsConfig(1, 3, "QUEUE_URL", "DEAD_QUEUE_URL")

    private val sqsAsyncClient = mockk<SqsAsyncClient>()

    private val sqsAccessor = SqsAccessor(sqsAsyncClient, sqsConfig)


    @Test
    fun `validate able fetch sqs messages`() = runBlockingTest {
        val receiveMessageRequest = ReceiveMessageRequest
            .builder()
            .queueUrl("QUEUE_URL")
            .maxNumberOfMessages(10)
            .waitTimeSeconds(20)
            .build()

        val message = Message
            .builder()
            .body("HelloWorld")
            .build()

        val response = CompletableFuture.completedFuture(
            ReceiveMessageResponse
                .builder()
                .messages(message)
                .build()
        )

        coEvery { sqsAsyncClient.receiveMessage(receiveMessageRequest) } returns response
        val fetchedMessages = sqsAccessor.fetchMessages()
        coVerify(exactly = 1) { sqsAsyncClient.receiveMessage(receiveMessageRequest) }
        assertEquals(1, fetchedMessages.size)
        assertEquals(message, fetchedMessages[0])
    }

    @Test
    fun `validate able to delete sqs message`() = runBlockingTest {

        val deleteMessageRequest = DeleteMessageRequest
            .builder()
            .queueUrl("QUEUE_URL")
            .receiptHandle("ReceiptHandle")
            .build()

        val message = Message
            .builder()
            .body("HelloWorld")
            .receiptHandle("ReceiptHandle")
            .build()

        val response = CompletableFuture.completedFuture(
            DeleteMessageResponse
                .builder()
                .build()
        )

        coEvery { sqsAsyncClient.deleteMessage(deleteMessageRequest) } returns response
        sqsAccessor.deleteMessage(MessageDecorator(true, message))
        coVerify(exactly = 1) { sqsAsyncClient.deleteMessage(deleteMessageRequest) }
    }

    @Test
    fun `validate able fetch dlq sqs messages`() = runBlockingTest {
        val receiveMessageRequest = ReceiveMessageRequest
            .builder()
            .queueUrl("DEAD_QUEUE_URL")
            .maxNumberOfMessages(10)
            .waitTimeSeconds(20)
            .build()

        val message = Message
            .builder()
            .body("HelloWorld")
            .build()

        val response = CompletableFuture.completedFuture(
            ReceiveMessageResponse
                .builder()
                .messages(message)
                .build()
        )

        coEvery { sqsAsyncClient.receiveMessage(receiveMessageRequest) } returns response
        val fetchedMessages = sqsAccessor.fetchDlqMessages()
        coVerify(exactly = 1) { sqsAsyncClient.receiveMessage(receiveMessageRequest) }
        assertEquals(1, fetchedMessages.size)
        assertEquals(message, fetchedMessages[0])
    }
}
