package org.thiyagu.reactive

import org.thiyagu.reactive.core.DefaultMessageProcessor
import org.thiyagu.reactive.core.MessageListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class SqsListener(private val defaultMessageProcessor: DefaultMessageProcessor,
                  private val threadPool: ExecutorService = Executors.newFixedThreadPool(2)) : CoroutineScope,
        MessageListener {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = threadPool.asCoroutineDispatcher()

    private val scope = CoroutineScope(job + coroutineContext)

    override fun start() {
        defaultMessageProcessor.processingChain().startSqsConsumer(scope)
    }

    override fun stop() {
        scope.cancel()
    }
}
