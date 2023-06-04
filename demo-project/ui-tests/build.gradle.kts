plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.app.test"
    compileSdk = 33

    targetProjectPath = projects.demoProject.app.dependencyProject.path

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        missingDimensionStrategy("environment", "stage")
    }
}
