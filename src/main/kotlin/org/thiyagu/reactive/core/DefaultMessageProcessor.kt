package org.thiyagu.reactive.core

import org.thiyagu.reactive.domain.MessageDecorator
import org.thiyagu.reactive.domain.SqsConfig
import org.thiyagu.reactive.sqs.SqsAccessor
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import java.util.function.Predicate

class DefaultMessageProcessor(
        private val messageProvider: MessageProvider,
        private val messageHandler: MessageHandler,
        private val sqsAccessor: SqsAccessor,
        private val sqsConfig: SqsConfig
) {

    private val isProcessed =
        Predicate { messageDecorator: MessageDecorator -> messageDecorator.isSuccessfullyProcessed }

    fun processingChain() =

        messageProvider.pollMessages()
            .buffer(sqsConfig.noOfProcessors)
            .map { messageHandler.handle(it) }
            .filter { isProcessed.test(it) }
            .map { sqsAccessor.deleteMessage(it) }
}
