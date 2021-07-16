import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

version = "1.1"
group = "io.github.thiyagu06"
plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.20"
    `java-library`
    id("io.gitlab.arturbosch.detekt") version "1.17.1"
    jacoco
    `maven-publish`
    signing
    id("io.codearte.nexus-staging") version "0.30.0"
    id ("org.jetbrains.dokka") version "0.10.1"
}

repositories {
    jcenter()
    mavenCentral()
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
    implementation("software.amazon.awssdk:sqs:2.16.95")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.0-native-mt")
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.1-native-mt")
    testImplementation("io.mockk:mockk:1.12.0")
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.2")
    testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine:5.4.2")
    integrationTestImplementation("org.testcontainers:localstack:1.15.3")
    integrationTestImplementation("cloud.localstack:localstack-utils:0.2.13")
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

    withType<JacocoReport> {
        reports {
            xml.isEnabled = true
            html.isEnabled = true
            xml.destination = file("$buildDir/reports/jacoco/jacocoTestReport.xml")
            html.destination = file("$buildDir/reports/jacoco")
        }

        classDirectories.setFrom(
            sourceSets.main.get().output.asFileTree.matching {
                exclude("org/thiyagu/reactive/FlowUtilsKt.class")
                exclude("org/thiyagu/reactive/core/MessageListener.class")
                exclude("org/thiyagu/reactive/SqsListener.class")
                exclude("org/thiyagu/reactive/core/MessageHandler.class")
                exclude("org/thiyagu/reactive/domain/*")
                exclude()
            }
        )
    }
}

val integrationTest = task<Test>("integrationTest") {
    description = "Run integration test"
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    shouldRunAfter("test")
    useJUnitPlatform()
}

//tasks.check { dependsOn(integrationTest) }

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}

val javadocJar by tasks.creating(Jar::class) {
    archiveClassifier.set ("javadoc")
    from("$buildDir/reports/javadoc")
}

val ossrhUsername: String by project
val ossrhPassword: String by project
val projectName="reactive-sqs-consumer"

nexusStaging {
    username = ossrhUsername
    password = ossrhPassword
}

publishing {
    publications {
        create<MavenPublication>("lib") {
            artifactId = projectName
            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)
            pom {
                name.set(projectName)
                description.set("reactive sqs consumer using kotlin flow")
                url.set("https://github.com/thiyagu06/reactive-sqs-consumer")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("thiyagu06")
                        name.set("Thiyagu")
                        email.set("thiyagu103@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/thiyagu06/reactive-sqs-consumer.git")
                    developerConnection.set("scm:git:ssh://github.com/thiyagu06/reactive-sqs-consumer.git")
                    url.set("https://github.com/thiyagu06/reactive-sqs-consumer")
                }
            }
        }
    }
    repositories {
        maven {
            val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            credentials {
                username = ossrhUsername
                password = ossrhPassword
            }
        }
    }
}

extra["isReleaseVersion"] = !version.toString().endsWith("SNAPSHOT")

signing {
    setRequired({
        (project.extra["isReleaseVersion"] as Boolean) && gradle.taskGraph.hasTask("publish")
    })
    sign(publishing.publications["lib"])
}
