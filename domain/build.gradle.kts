plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.halimjr11.domain"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24
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
    // api: NetworkResult, Flow and PagingData are part of MovieRepository's public surface
    api(project(":core:common"))
    api(libs.kotlinx.coroutines.core)
    api(libs.androidx.paging.common)

    testImplementation(libs.bundles.unit.test)
}
