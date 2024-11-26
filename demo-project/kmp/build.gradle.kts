import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
    alias(libs.plugins.android.lib)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.ksp)
}

afterEvaluate {
    rootProject.the<NodeJsRootExtension>().download = false
    rootProject.the<YarnRootExtension>().download = false
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(11))

android {
    namespace = "com.example.login"

    compileSdk = libs.versions.android.compileSDK.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSDK.get().toInt()
    }
}

kotlin {
    androidTarget()
    jvm()
    js { nodejs() }
}

dependencies {
    ksp(libs.moshi.codegen)
    "androidMainImplementation"(libs.moshi.kotlin)
    "jvmMainImplementation"(libs.moshi.kotlin)
    commonTestImplementation(libs.kotlin.test)
}
