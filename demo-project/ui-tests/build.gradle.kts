import com.android.build.api.dsl.ManagedVirtualDevice
import com.android.build.gradle.internal.tasks.ManagedDeviceInstrumentationTestSetupTask

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

val pixel2 by android.testOptions.managedDevices.devices.creating(ManagedVirtualDevice::class) {
    device = "Pixel 6"
    apiLevel = 30
    systemImageSource = "aosp-atd"
}

baselineProfile {
    useConnectedDevices = false
    managedDevices += pixel2.name
}

dependencies {
    implementation(libs.androidx.test.junit)
    implementation(libs.androidx.test.espresso)
}

val acceptAndroidLicences by tasks.registering(Exec::class) {
    val androidHome = androidComponents.sdkComponents.sdkDirectory.get().asFile.absolutePath

    commandLine("bash", "-c", "yes | $androidHome/cmdline-tools/latest/bin/sdkmanager --licenses")
}

tasks.withType<ManagedDeviceInstrumentationTestSetupTask>().configureEach {
    dependsOn(acceptAndroidLicences)
}
