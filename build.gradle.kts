plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
}

// Local unit tests run on JUnit 5 (Jupiter). Instrumented tests (androidTest) stay on JUnit 4.
subprojects {
    tasks.withType<Test>().configureEach { useJUnitPlatform() }
}
