import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

version = "0.1.0-SNAPSHOT"

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.41"
    `java-library`
    id("io.gitlab.arturbosch.detekt") version "1.4.0"
    jacoco
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
}

sourceSets {
    create("integrationTest") {
        withConvention(KotlinSourceSet::class) {
            kotlin.srcDir("src/it/kotlin")
            kotlin.srcDir("src/it/resources")
            compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output + configurations.testRuntimeClasspath
            runtimeClasspath += output + compileClasspath + sourceSets.test.get().runtimeClasspath
        }
    }
}

val integrationTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

configurations["integrationTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("software.amazon.awssdk:sqs:2.10.41")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.3")
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.3.3")
    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.4.2")
    testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine:5.4.2")
    integrationTestImplementation("org.testcontainers:localstack:1.12.5")
    integrationTestImplementation("cloud.localstack:localstack-utils:0.2.0")
}

tasks {
    named<Task>("check") {
        dependsOn(named<Task>("jacocoTestReport"))
    }
    named<Test>("test") {
        useJUnitPlatform()
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "12"
    }
}

tasks.withType<JacocoReport> {
    reports {
        xml.isEnabled = true
        html.isEnabled = true
        xml.destination = file("$buildDir/reports/jacoco/jacocoTestReport.xml")
        html.destination = file("$buildDir/reports/jacoco")
    }

    classDirectories.setFrom(
            sourceSets.main.get().output.asFileTree.matching {
                exclude("com/thiyagu/reactive/FlowUtilsKt.class")
                exclude("com/thiyagu/reactive/core/MessageListener.class")
                exclude("com/thiyagu/reactive/SqsListener.class")
                exclude("com/thiyagu/reactive/core/MessageHandler.class")
                exclude("com/thiyagu/reactive/domain/*")
            }
    )
}

val integrationTest = task<Test>("integrationTest") {
    description = "Run integration test"
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    shouldRunAfter("test")
    useJUnitPlatform()
}

tasks.check { dependsOn(integrationTest) }