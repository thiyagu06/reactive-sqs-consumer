package com.thiyagu.reactive.domain

data class SqsConfig(
    val noOfPollers: Int = 1,
    val noOfProcessors: Int,
    val sqsUrl: String,
    val deadLetterQueueUrl: String
) {
    val maxOfMessageToRetrieved: Int = 10
}
