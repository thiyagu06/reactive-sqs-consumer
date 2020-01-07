package com.thiyagu.reactive

import com.thiyagu.reactive.core.DefaultMessageProcessor
import com.thiyagu.reactive.core.MessageListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class SqsListener(private val defaultMessageProcessor: DefaultMessageProcessor) : CoroutineScope,
    MessageListener {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Executors.newFixedThreadPool(2).asCoroutineDispatcher()

    private val scope = CoroutineScope(job + coroutineContext)

    override fun start() {
        defaultMessageProcessor.processingChain().startSqsConsumer(scope)
    }

    override fun stop() {
        scope.cancel()
    }
}
