plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin)
    publishing
    signing
}

android {
    namespace = "app.revanced.integrations"
    compileSdk = 33

    applicationVariants.all {
        outputs.all {
            this as com.android.build.gradle.internal.api.ApkVariantOutputImpl

            outputFileName = "${rootProject.name}-$versionName.apk"
        }
    }

    defaultConfig {
        applicationId = "app.revanced.integrations"
        minSdk = 23
        targetSdk = 33
        multiDexEnabled = false
        versionName = version as String
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

dependencies {
    compileOnly(libs.appcompat)
    compileOnly(libs.annotation)
    compileOnly(libs.okhttp)
    compileOnly(libs.retrofit)

    compileOnly(project(":stub"))
}


tasks {
    val assembleReleaseSignApk by registering {
        dependsOn("assembleRelease")

        val apk = layout.buildDirectory.file("outputs/apk/release/${rootProject.name}-$version.apk")

        inputs.file(apk).withPropertyName("input")
        outputs.file(apk.map { it.asFile.resolveSibling("${it.asFile.name}.asc") })

        doLast {
            signing {
                useGpgCmd()
                sign(*inputs.files.files.toTypedArray())
            }
        }
    }

    // Needed by gradle-semantic-release-plugin.
    // Tracking: https://github.com/KengoTODA/gradle-semantic-release-plugin/issues/435.
    publish {
        dependsOn(assembleReleaseSignApk)
    }
}
