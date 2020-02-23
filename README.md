# reactive-sqs-consumer

[![Kotlin version badge](https://img.shields.io/badge/kotlin-1.3-blue.svg)](https://kotlinlang.org/docs/reference/whatsnew13.html) 
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![codebeat badge](https://codebeat.co/badges/d8f4dd3e-5082-40cc-bb25-75141a282c62)](https://codebeat.co/projects/github-com-thiyagu06-reactive-sqs-consumer-master)
[![CircleCI](https://circleci.com/gh/thiyagu06/reactive-sqs-consumer.svg?style=svg)](https://circleci.com/gh/thiyagu06/reactive-sqs-consumer)
[![codecov](https://codecov.io/gh/thiyagu06/reactive-sqs-consumer/branch/master/graph/badge.svg)](https://codecov.io/gh/thiyagu06/reactive-sqs-consumer)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.thiyagu06/reactive-sqs-consumer.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.thiyagu06%22%20AND%20a:%22reactive-sqs-consumer%22)

## Introduction

This [reactive sqs consumer](https://github.com/thiyagu06/reactive-sqs-consumer) built with aim to reduce the boilerplate code to launch the sqs consumer and allow the developers to focus on processing the message for implement the business logic.
This has been built from the scratch with a goal making it easy to customizable allowing each component of the library is easy to interchangeable if desired. This also, allows us define custom configuration such as amount of thread to fetch messages and amount of threads to process the messages etc.

The distinguished feature of [reactive sqs consumer](https://github.com/thiyagu06/reactive-sqs-consumer) are

 * Its truly non blocking implementation to maximize throughput and optimize resource utilization by leveraging [Asynchronous AWS SDK for Java 2.0](https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/basics-async.html) and [Kotlin > Flow](https://kotlinlang.org/docs/reference/coroutines/flow.html)
 * automatic handling of back pressure between [MessageProvider](https://github.com/thiyagu06/reactive-sqs-consumer/blob/master/src/main/kotlin/org/thiyagu/reactive/core/MessageProvider.kt) and [MessageHandler](https://github.com/thiyagu06/reactive-sqs-consumer/blob/master/src/main/kotlin/org/thiyagu/reactive/core/MessageHandler.kt) by using kotlin flows.
 * customize the no of concurrent [MessageProvider](https://github.com/thiyagu06/reactive-sqs-consumer/blob/master/src/main/kotlin/org/thiyagu/reactive/core/MessageProvider.kt) and concurrent no of [MessageHandler](https://github.com/thiyagu06/reactive-sqs-consumer/blob/master/src/main/kotlin/org/thiyagu/reactive/core/MessageHandler.kt) through configuration.
 * abstraction of complexity for fetching and deleting the sqs messages.
 * automatic exception handling of messages using [Dead-Letter Queues](https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-dead-letter-queues.html)
 * better control of defining polling logic for DLQ message by implementing [PollingStrategy](https://github.com/thiyagu06/reactive-sqs-consumer/blob/master/src/main/kotlin/org/thiyagu/reactive/core/PollingStrategy.kt)
 * reduce cost by leveraging [Long Polling](https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-long-polling.html)   
 
## Design

This library is divided into four sub components each with its own responsibilities. The below diagram shows the simple flow of sqs message lifecycle flowing throw each components.

![Design Diagram](./docs/SqsConsumer.png "Design Diagram")

## usage

 See [ExampleUsage.kt](https://github.com/thiyagu06/reactive-sqs-consumer/blob/master/src/it/kotlin/org/thiyagu/reactive/ExampleUsage.kt) for how to use this library.
