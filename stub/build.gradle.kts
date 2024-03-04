plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "app.revanced.integrations.stub"
    compileSdk = 33

    defaultConfig {
        multiDexEnabled = false
        minSdk = 23
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
}
