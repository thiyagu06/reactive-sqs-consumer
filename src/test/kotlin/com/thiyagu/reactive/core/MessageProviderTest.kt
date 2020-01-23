package com.thiyagu.reactive.core

import com.thiyagu.reactive.domain.SqsConfig
import com.thiyagu.reactive.sqs.SqsAccessor
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import software.amazon.awssdk.services.sqs.model.Message
import kotlin.test.assertEquals


class MessageProviderTest {

    private val sqsAccessor = mockk<SqsAccessor>()

    private val sqsConfig = mockk<SqsConfig>()

    private val messageProvider = MessageProvider(sqsConfig, sqsAccessor)

    @Test
    fun `test able to receive message`() = runBlockingTest {

        val message = Message.builder().body("testMessage").receiptHandle("dummy receipt").build()

        val values = mutableListOf<Message>()

        every {sqsConfig.noOfPollers} returns 1

        every { sqsConfig.dlqPollFrequency } returns 10

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

        every { sqsConfig.dlqPollFrequency } returns 1

        coEvery { sqsAccessor.fetchMessages() } returns listOf(message)

        coEvery { sqsAccessor.fetchDlqMessages() } returns emptyList()

        messageProvider.pollMessages().collect { values.add(it) }

        coVerify(exactly = 1) { sqsAccessor.fetchMessages() }

        coVerify(exactly = 1) {sqsAccessor.fetchDlqMessages() }

        assertEquals(message, values[0])
    }
}
