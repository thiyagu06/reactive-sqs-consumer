package org.thiyagu.reactive.core


import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DlqPollingStrategyTest {

    @Test
    fun `test polling strategy with frequency value as 1`() {
        val dlqPollingStrategy = DlqPollingStrategy(2)
        dlqPollingStrategy.shouldPollNow()
        assertTrue (dlqPollingStrategy.shouldPollNow())
        assertFalse (dlqPollingStrategy.shouldPollNow())
    }
}