package org.thiyagu.reactive.core

import org.thiyagu.reactive.domain.SqsConfig
import org.thiyagu.reactive.sqs.SqsAccessor
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.sqs.model.Message
import kotlin.test.assertEquals


class MessageProviderTest {

    private val sqsAccessor = mockk<SqsAccessor>()

    private val sqsConfig = mockk<SqsConfig>()

    private val pollingStrategy = mockk<PollingStrategy>()

    private val messageProvider = MessageProvider(sqsConfig, sqsAccessor, pollingStrategy)

    @Test
    fun `test able to receive message`() = runBlockingTest {

        val message = Message.builder().body("testMessage").receiptHandle("dummy receipt").build()

        val values = mutableListOf<Message>()

        every {sqsConfig.noOfPollers} returns 1

        every { pollingStrategy.shouldPollNow() } returns false

        coEvery { sqsAccessor.fetchMessages() } returns listOf(message)

        messageProvider.pollMessages().collect { values.add(it) }

        coVerify(exactly = 1) { sqsAccessor.fetchMessages() }

        assertEquals(message, values[0])
    }

    @Test
    fun `test able to receive message from dlq`() = runBlockingTest {

        val message = Message.builder().body("testMessage").receiptHandle("dummy receipt").build()

        val values = mutableListOf<Message>()

        every {sqsConfig.noOfPollers} returns 1

        every { pollingStrategy.shouldPollNow() } returns false

        coEvery { sqsAccessor.fetchMessages() } returns listOf(message)

        coEvery { sqsAccessor.fetchDlqMessages() } returns emptyList()

        messageProvider.pollMessages().collect { values.add(it) }

        coVerify(exactly = 1) { sqsAccessor.fetchMessages() }

        coVerify(exactly = 0) {sqsAccessor.fetchDlqMessages() }

        assertEquals(message, values[0])
    }

    @Test
    fun `should call fetchMessages N (noOfPollers) times`() = runBlockingTest {

        val message = Message.builder().body("testMessage").receiptHandle("dummy receipt").build()

        val values = mutableListOf<Message>()

        every {sqsConfig.noOfPollers} returns 5

        every { pollingStrategy.shouldPollNow() } returns true

        coEvery { sqsAccessor.fetchMessages() } returns listOf(message)

        coEvery { sqsAccessor.fetchDlqMessages() } returns emptyList()

        messageProvider.pollMessages().collect { values.add(it) }

        coVerify(exactly = 5) { sqsAccessor.fetchMessages() }

        coVerify(exactly = 5) {sqsAccessor.fetchDlqMessages() }

        assertEquals(message, values[0])
    }
}
