plugins {
    id("com.android.application")
    kotlin("android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "org.jenson.heartrate"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.jenson.heartrate"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Compose BOM — pins compose-ui and friends to a compatible version set
    implementation(platform("androidx.compose:compose-bom:2026.03.00"))
    implementation("androidx.compose.ui:ui")

    // Wear Compose
    implementation("androidx.wear.compose:compose-material3:1.5.0")
    implementation("androidx.wear.compose:compose-foundation:1.5.0")
    implementation("androidx.wear:wear:1.4.0")
    implementation("androidx.wear:wear-ongoing:1.0.0")

    // Activity
    implementation("androidx.activity:activity-compose:1.10.1")

    // Health Services API
    implementation("androidx.health:health-services-client:1.1.0-rc02")
    implementation("com.google.guava:guava:33.3.1-android")

    // Lifecycle + ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")
}
