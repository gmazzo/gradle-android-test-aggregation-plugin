plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.android.baseline)
    alias(libs.plugins.kotlin.android)
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(11))

android {
    namespace = "com.example.app.test"

    compileSdk = libs.versions.android.compileSDK.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSDK.get().toInt()
    }

    targetProjectPath = projects.demoProject.app.path

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        missingDimensionStrategy("environment", "stage")
    }
}

val emulator by android.testOptions.managedDevices.localDevices.registering {
    device = "Pixel 6"
    apiLevel = 33
    systemImageSource = "aosp"
}

baselineProfile {
    useConnectedDevices = false
    managedDevices += emulator.name
}

dependencies {
    implementation(libs.androidx.test.junit)
    implementation(libs.androidx.test.espresso)
}
