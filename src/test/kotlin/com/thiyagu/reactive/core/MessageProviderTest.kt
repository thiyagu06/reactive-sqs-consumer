package com.thiyagu.reactive.core

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import software.amazon.awssdk.services.sqs.model.Message
import kotlin.test.assertEquals


class MessageProviderTest {

    private val sqsAccessor = mockk<SqsAccessor>()

    private val messageProvider = MessageProvider(1, sqsAccessor)

    @Test
    fun `test able to receive message`() = runBlockingTest {

        val message = Message.builder().body("testMessage").receiptHandle("dummy receipt").build()

        val values = mutableListOf<Message>()

        coEvery { sqsAccessor.fetchMessages() } returns listOf(message)

        messageProvider.pollMessages().collect { values.add(it) }

        coVerify(exactly = 1) { sqsAccessor.fetchMessages() }

        assertEquals(message, values[0])
    }
}
