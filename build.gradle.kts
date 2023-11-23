buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.0.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.20")
    }
}

allprojects {
    repositories {
        google()
        maven { url = uri("https://jitpack.io") }
        mavenCentral()
    }
}

// Tracking issue https://github.com/semantic-release/semantic-release/issues/963
tasks.register("publish", DefaultTask::class) {
    group = "publish"
    description = "Dummy publish to pass the verification phase of the gradle-semantic-release-plugin"
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
