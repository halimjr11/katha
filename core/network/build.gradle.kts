import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
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
        consumerProguardFiles("consumer-rules.pro")
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
    api(project(":core:common"))
    api(libs.bundles.koin.compose)
    api(libs.kotlinx.coroutines.android)
    implementation(libs.bundles.networking)

    testImplementation(libs.bundles.unit.test)
}
