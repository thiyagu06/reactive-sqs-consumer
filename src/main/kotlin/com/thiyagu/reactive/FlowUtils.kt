package com.thiyagu.reactive

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn

fun <Unit> Flow<Unit>.startSqsConsumer(scope: CoroutineScope): Job = this.launchIn(scope)
