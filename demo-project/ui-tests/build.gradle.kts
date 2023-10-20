plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.kotlin.android)
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(11))

android {
    namespace = "com.example.app.test"

    compileSdk = libs.versions.android.compileSDK.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSDK.get().toInt()
    }

    targetProjectPath = projects.demoProject.app.dependencyProject.path

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        missingDimensionStrategy("environment", "stage")
    }
}
