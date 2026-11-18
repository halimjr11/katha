plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.halimjr11.data"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    implementation(project(":core:network"))
    implementation(project(":domain"))

    api(libs.bundles.koin.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.paging.common)
    implementation(libs.retrofit.core)

    testImplementation(libs.bundles.unit.test)
    testImplementation(libs.androidx.paging.testing)
    androidTestImplementation(libs.androidx.junit)
}
