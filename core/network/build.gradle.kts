import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}

android {
    namespace = "com.halimjr11.network"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24

        val token = localProperties.getProperty("TMDB_BEARER_TOKEN") ?: "\"\""
        buildConfigField("String", "TMDB_BEARER_TOKEN", token)
        buildConfigField("String", "BASE_URL", "\"https://api.themoviedb.org/3/\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.bundles.networking)
    implementation(libs.bundles.koin.compose)
    implementation(libs.kotlinx.coroutines.android)
}