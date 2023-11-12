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

    testOptions.managedDevices.devices {
        create<ManagedVirtualDevice>("emulator") {
            device = "Pixel 2"
            apiLevel = android.compileSdk!!
            systemImageSource = "aosp-atd"
        }
    }
}

androidComponents.finalizeDsl {
    // added this here, because the `baselineprofile` plugin resets it back to false for some reason
    android.buildTypes["nonMinifiedRelease"].enableAndroidTestCoverage = true
}

baselineProfile {
    useConnectedDevices = false
    managedDevices += "pixel2"
}

dependencies {
    implementation(projects.demoProject.app)
    implementation(libs.androidx.test.junit)
    implementation(libs.androidx.test.espresso)
}
