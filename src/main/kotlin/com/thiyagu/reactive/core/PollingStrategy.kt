package com.thiyagu.reactive.core

interface PollingStrategy {
    fun shouldPollNow() :Boolean = true
}
