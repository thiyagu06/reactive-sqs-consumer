package org.thiyagu.reactive.core

import org.thiyagu.reactive.domain.MessageDecorator
import software.amazon.awssdk.services.sqs.model.Message

interface MessageHandler {

    suspend fun handleMessage(message: Message)

    suspend fun handle(message: Message): MessageDecorator {
        return try {
            handleMessage(message)
            MessageDecorator(true, message)
        } catch (e: Exception) {
            MessageDecorator(false, message)
        }
    }
}
