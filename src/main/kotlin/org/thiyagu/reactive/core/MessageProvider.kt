package org.thiyagu.reactive.core

import org.thiyagu.reactive.domain.SqsConfig
import org.thiyagu.reactive.sqs.SqsAccessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import software.amazon.awssdk.services.sqs.model.Message

class MessageProvider(private val sqsConfig: SqsConfig, private val sqsAccessor: SqsAccessor,
                      private val pollingStrategy: PollingStrategy) {

    fun pollMessages(): Flow<Message> {

        return channelFlow {

            repeat(sqsConfig.noOfPollers) {
                launch {
                    val messages = sqsAccessor.fetchMessages()

                    if (pollingStrategy.shouldPollNow()) {
                        val dlqMessages = sqsAccessor.fetchDlqMessages()
                        messages.plus(dlqMessages)
                    }
                    messages.forEach {
                        send(it)
                    }

                }
            }
        }
    }
}
