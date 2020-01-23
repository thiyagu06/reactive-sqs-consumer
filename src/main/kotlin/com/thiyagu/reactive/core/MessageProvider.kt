package com.thiyagu.reactive.core

import com.thiyagu.reactive.sqs.SqsAccessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import software.amazon.awssdk.services.sqs.model.Message

class MessageProvider(private val noOfReceivers: Int, private val sqsAccessor: SqsAccessor) {


    fun pollMessages(): Flow<Message> {

        return channelFlow {

            repeat(noOfReceivers) {
                launch {
                    val messages = sqsAccessor.fetchMessages()
                    messages.forEach {
                        send(it)
                    }

                }
            }
        }
    }
}
