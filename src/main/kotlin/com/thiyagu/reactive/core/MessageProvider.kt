package com.thiyagu.reactive.core

import com.thiyagu.reactive.domain.SqsConfig
import com.thiyagu.reactive.sqs.SqsAccessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import software.amazon.awssdk.services.sqs.model.Message
import java.util.concurrent.atomic.AtomicInteger

class MessageProvider(private val sqsConfig: SqsConfig, private val sqsAccessor: SqsAccessor) {

    private val pollSequence = AtomicInteger()

    fun pollMessages(): Flow<Message> {

        return channelFlow {

            repeat(sqsConfig.noOfPollers) {
                launch {
                    val messages = sqsAccessor.fetchMessages()
                    val shouldPollNow =
                        pollSequence.updateAndGet { value -> (value + 1) % sqsConfig.dlqPollFrequency } == 0
                    if (shouldPollNow) {
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
