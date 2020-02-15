package org.thiyagu.reactive

import cloud.localstack.Localstack
import cloud.localstack.TestUtils
import cloud.localstack.docker.LocalstackDockerExtension
import cloud.localstack.docker.annotation.LocalstackDockerProperties
import com.amazonaws.services.sqs.AmazonSQS
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import org.junit.jupiter.api.condition.DisabledIfSystemProperty
import org.junit.jupiter.api.extension.ExtendWith
import org.thiyagu.reactive.core.DefaultMessageProcessor
import org.thiyagu.reactive.core.MessageHandler
import org.thiyagu.reactive.core.MessageProvider
import org.thiyagu.reactive.core.PollingStrategy
import org.thiyagu.reactive.domain.SqsConfig
import org.thiyagu.reactive.sqs.SqsAccessor
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.Message
import java.net.URI


@ExtendWith(LocalstackDockerExtension::class)
@LocalstackDockerProperties(randomizePorts = true, services = ["sqs"])
@DisabledIfEnvironmentVariable(named = "CI_SERVER", matches = "true")
class ExampleUsage {

    private lateinit var sqsListener: SqsListener

    private lateinit var sqs: AmazonSQS

    private lateinit var queueUrl:String

    @BeforeEach
    fun setup() {
        sqs = TestUtils.getClientSQS()
        val queueCreated = sqs.createQueue("integrationTest")
        queueUrl = queueCreated.queueUrl
        val sqsConfig = SqsConfig(1, 2, queueCreated.queueUrl, "")
        val sqsClient = SqsAsyncClient.builder().endpointOverride(URI(Localstack.INSTANCE.endpointSQS)).build()
        sqs.sendMessage(com.amazonaws.services.sqs.model.SendMessageRequest(queueCreated.queueUrl,"message"))
        val sqsAccessor = SqsAccessor(sqsClient, sqsConfig)
        val messageProvider = MessageProvider(sqsConfig, sqsAccessor, DlqNoPollingStrategy())
        val messageProcessor = DefaultMessageProcessor(messageProvider, LoggingMessageHandler(), sqsAccessor, sqsConfig)
        sqsListener = SqsListener(messageProcessor)
        println("init completed")
    }

    @Test
    fun `start consumer`() = runBlockingTest{
        sqsListener.start()
        delay(10000)
    }

    @AfterEach
    fun `stop consumer`() {
        sqsListener.stop()
    }
}

class DlqNoPollingStrategy : PollingStrategy {

    override fun shouldPollNow(): Boolean = false
}

class LoggingMessageHandler : MessageHandler {

    override suspend fun handleMessage(message: Message) {
        println("message received from amazon");
    }
}