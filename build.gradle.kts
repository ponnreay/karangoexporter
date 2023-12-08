val kotlin_version: String by project
val logback_version: String by project
val project_name: String by project
plugins {
    kotlin("jvm") version "1.9.21"
    id("org.graalvm.buildtools.native") version "0.9.27"
}

group = "com.example"
version = "0.0.1"

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.example.ApplicationKt"
    }
    archiveFileName = "$project_name.jar"
    from(configurations.getByName("runtimeClasspath").map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("META-INF/*.RSA")
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("ch.qos.logback:logback-core:$logback_version")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("com.arangodb:arangodb-java-driver:7.1.0")
}


graalvmNative {
    agent {
        modes {
            standard
        }
    }
    binaries {
        named("main") {
            mainClass.set("com.example.ApplicationKt")
            imageName.set(project_name)
        }
        all {
            resources.autodetect()
            fallback.set(false)
            verbose.set(true)
            buildArgs.add("-march=compatibility")
            buildArgs.add("-H:+ReportExceptionStackTraces")
        }
    }
    metadataRepository {
        enabled.set(true)
    }
    toolchainDetection.set(false)
}