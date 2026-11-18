plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.halimjr11.common"
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
    // api: DispatcherProvider and the Koin Module type appear in public signatures
    api(libs.kotlinx.coroutines.core)
    api(libs.bundles.koin.compose)

    testImplementation(libs.bundles.unit.test)
}
