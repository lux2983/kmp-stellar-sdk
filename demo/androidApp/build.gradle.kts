plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
}

android {
    namespace = "com.soneso.stellar.demo.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.soneso.stellar.demo.android"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = project.property("demo.version") as String
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(project(":demo:shared"))
    implementation("androidx.activity:activity-compose:1.8.2")
}
