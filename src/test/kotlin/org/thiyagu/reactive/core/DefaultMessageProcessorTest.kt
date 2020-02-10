package org.thiyagu.reactive.core

import org.thiyagu.reactive.domain.MessageDecorator
import org.thiyagu.reactive.domain.SqsConfig
import org.thiyagu.reactive.sqs.SqsAccessor
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.sqs.model.Message

class DefaultMessageProcessorTest {

    private val sqsAccessor = mockk<SqsAccessor>()

    private val handler = mockk<MessageHandler>()

    private val sqsConfig = mockk<SqsConfig>()

    private val pollingStrategy = mockk<PollingStrategy>()

    private val messageProvider = MessageProvider(sqsConfig, sqsAccessor, pollingStrategy)

    private val messageProcessor = DefaultMessageProcessor(messageProvider, handler, sqsAccessor, sqsConfig)

    @Test
    fun `process should succeed`() = runBlockingTest {
        val messages = mutableListOf<Message>()

        every { sqsConfig.noOfProcessors } returns 1

        every { sqsConfig.noOfPollers } returns 1

        every { pollingStrategy.shouldPollNow() } returns false

        (0..4).forEach {
            val message = Message.builder().body("m$it").receiptHandle("R$it").build()
            messages.add(message)
        }

        coEvery { sqsAccessor.fetchMessages() } returns messages

        coEvery { handler.handle(any()) } returns MessageDecorator(true, messages[0])

        coEvery { sqsAccessor.deleteMessage(any()) } returns mockk()

        messageProcessor.processingChain().launchIn(this)

        coVerify { sqsAccessor.fetchMessages() }

        coVerify(exactly = 5) { handler.handle(any()) }

        coVerify(exactly = 5) { sqsAccessor.deleteMessage(any()) }
    }

    @Test
    fun `throwing exception in handler step should not stop the flow processing`() = runBlockingTest {
        val messages = mutableListOf<Message>()

        val failureMessage = Message.builder().body("fail").receiptHandle("fail").build()

        messages.add(failureMessage)

        every { sqsConfig.noOfProcessors } returns 1

        every { sqsConfig.noOfPollers } returns 1

        coEvery { sqsAccessor.fetchDlqMessages() } returns emptyList()

        every { pollingStrategy.shouldPollNow() } returns true

        (0..4).forEach {
            val message = Message.builder().body("m$it").receiptHandle("R$it").build()
            messages.add(message)
        }

        coEvery { sqsAccessor.fetchMessages() } returns messages

        coEvery { handler.handle(failureMessage) } returns MessageDecorator(false, failureMessage)

        coEvery { handler.handle(neq(failureMessage)) } returns MessageDecorator(true, messages[0])

        coEvery { sqsAccessor.deleteMessage(any()) } returns mockk()

        messageProcessor.processingChain().launchIn(this)

        coVerify { sqsAccessor.fetchMessages() }

        coVerify(exactly = 6) { handler.handle(any()) }

        coVerify(exactly = 5) { sqsAccessor.deleteMessage(any()) }
    }
}
