import com.android.build.api.dsl.ManagedVirtualDevice

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

    targetProjectPath = projects.demoProject.app.dependencyProject.path

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        missingDimensionStrategy("environment", "stage")
    }
}

val emulator by android.testOptions.managedDevices.devices.creating(ManagedVirtualDevice::class) {
    device = "Pixel 2"
    apiLevel = 30
    systemImageSource = "aosp-atd"
}

baselineProfile {
    useConnectedDevices = false
    managedDevices += emulator.name
}

dependencies {
    implementation(libs.androidx.test.junit)
    implementation(libs.androidx.test.espresso)
}
