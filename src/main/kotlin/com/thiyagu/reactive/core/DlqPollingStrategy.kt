package com.thiyagu.reactive.core

import java.util.concurrent.atomic.AtomicInteger

class DlqPollingStrategy(private val dlqPollingFrequency: Int) : PollingStrategy {

    private val pollSequence = AtomicInteger()

    override fun shouldPollNow(): Boolean =
            pollSequence.updateAndGet { value -> (value + 1)}  % dlqPollingFrequency  == 0
}
