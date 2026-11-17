plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // api: NetworkResult is part of MovieRepository's public surface
    api(project(":core:common"))
    api(libs.kotlinx.coroutines.core)
    api(libs.androidx.paging.common)

    testImplementation(libs.bundles.unit.test)
}
