plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 33
    namespace = "app.revanced.integrations"

    defaultConfig {
        applicationId = "app.revanced.integrations"
        minSdk = 23
        targetSdk = 33
        multiDexEnabled = false
        versionName = project.version as String
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        applicationVariants.all {
            outputs.all {
                this as com.android.build.gradle.internal.api.ApkVariantOutputImpl

                outputFileName = "${rootProject.name}-$versionName.apk"
            }
        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_11)
        targetCompatibility(JavaVersion.VERSION_11)
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

dependencies {
    compileOnly(project(mapOf("path" to ":dummy")))
    compileOnly("androidx.annotation:annotation:1.7.1")
    compileOnly("androidx.appcompat:appcompat:1.7.0-alpha03")
    compileOnly("com.squareup.okhttp3:okhttp:5.0.0-alpha.11")
    compileOnly("com.squareup.retrofit2:retrofit:2.9.0")
}

tasks.register("publish") { dependsOn("build") }
