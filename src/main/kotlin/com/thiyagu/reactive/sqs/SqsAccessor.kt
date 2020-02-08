package com.thiyagu.reactive.sqs

import com.thiyagu.reactive.domain.MessageDecorator
import com.thiyagu.reactive.domain.SqsConfig
import kotlinx.coroutines.future.await
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest
import software.amazon.awssdk.services.sqs.model.Message
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest

class SqsAccessor(private val sqsAsyncClient: SqsAsyncClient, private val sqsConfig: SqsConfig) {

    private val sqsFetchWaitTime: Int = 20

    suspend fun fetchMessages(): List<Message> {
        val messageRequest = buildReceiveMessageRequest(sqsConfig.sqsUrl)
        return sqsAsyncClient.receiveMessage(messageRequest).await().messages()
    }


    suspend fun deleteMessage(messageDecorator: MessageDecorator) {

        with(messageDecorator.message) {
            val deleteMessageRequest = DeleteMessageRequest
                    .builder()
                    .queueUrl(sqsConfig.sqsUrl)
                    .receiptHandle(this.receiptHandle())
                    .build()
            sqsAsyncClient.deleteMessage(deleteMessageRequest).await()
        }
    }

    suspend fun fetchDlqMessages(): List<Message> {
        val messageRequest = buildReceiveMessageRequest(sqsConfig.deadLetterQueueUrl)
        return sqsAsyncClient.receiveMessage(messageRequest).await().messages()
    }

    private fun buildReceiveMessageRequest(sqsUrl: String) = ReceiveMessageRequest
            .builder()
            .queueUrl(sqsUrl)
            .maxNumberOfMessages(sqsConfig.maxOfMessageToRetrieved)
            .waitTimeSeconds(sqsFetchWaitTime)
            .build()
}
