package org.thiyagu.reactive.core

interface PollingStrategy {
    fun shouldPollNow() :Boolean = true
}
