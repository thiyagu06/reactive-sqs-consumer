package com.thiyagu.reactive.core

import com.thiyagu.reactive.domain.MessageDecorator
import com.thiyagu.reactive.domain.SqsConfig
import kotlinx.coroutines.future.await
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.Message

class SqsAccessor(private val sqsAsyncClient: SqsAsyncClient, private val sqsConfig: SqsConfig) {

    suspend fun fetchMessages(): List<Message> =

        sqsAsyncClient.receiveMessage {
            it.maxNumberOfMessages(sqsConfig.maxOfMessageToRetrieved)
            it.queueUrl(sqsConfig.sqsUrl)
            it.waitTimeSeconds(20)
        }.await().messages()


    suspend fun deleteMessage(messageDecorator: MessageDecorator) {

        with(messageDecorator.message) {
            sqsAsyncClient.deleteMessage {
                it.queueUrl(sqsConfig.sqsUrl)
                it.receiptHandle(this.receiptHandle())
            }.await()
        }

    }
}
