plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.app.test"
    compileSdk = libs.versions.android.sdk.get().toInt()

    targetProjectPath = projects.demoProject.app.dependencyProject.path

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        missingDimensionStrategy("environment", "stage")
    }
}
