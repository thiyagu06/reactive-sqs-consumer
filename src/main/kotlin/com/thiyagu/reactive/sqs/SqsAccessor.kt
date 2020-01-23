package com.thiyagu.reactive.sqs

import com.thiyagu.reactive.domain.MessageDecorator
import com.thiyagu.reactive.domain.SqsConfig
import kotlinx.coroutines.future.await
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest
import software.amazon.awssdk.services.sqs.model.Message
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest

class SqsAccessor(private val sqsAsyncClient: SqsAsyncClient, private val sqsConfig: SqsConfig) {

    suspend fun fetchMessages(): List<Message> {

        val receiveMessageRequest: ReceiveMessageRequest = ReceiveMessageRequest
            .builder()
            .queueUrl(sqsConfig.sqsUrl)
            .maxNumberOfMessages(sqsConfig.maxOfMessageToRetrieved)
            .waitTimeSeconds(20)
            .build()

        return sqsAsyncClient.receiveMessage(receiveMessageRequest).await().messages()
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

        val receiveMessageRequest: ReceiveMessageRequest = ReceiveMessageRequest
            .builder()
            .queueUrl(sqsConfig.deadLetterQueueUrl)
            .maxNumberOfMessages(sqsConfig.maxOfMessageToRetrieved)
            .waitTimeSeconds(20)
            .build()

        return sqsAsyncClient.receiveMessage(receiveMessageRequest).await().messages()
    }
}
