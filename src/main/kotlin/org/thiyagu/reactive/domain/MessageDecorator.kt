package org.thiyagu.reactive.domain

import software.amazon.awssdk.services.sqs.model.Message

data class MessageDecorator(val isSuccessfullyProcessed: Boolean, val message: Message)
