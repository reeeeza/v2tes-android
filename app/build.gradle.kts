plugins {
    id("com.android.application")
}

android {
    namespace = "app.v2tes.client"
    compileSdk = 34

    defaultConfig {
        applicationId = "app.v2tes.client"
        minSdk = 23
        targetSdk = 34
        versionCode = 150
        versionName = "1.5.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.annotation:annotation:1.8.2")
}
