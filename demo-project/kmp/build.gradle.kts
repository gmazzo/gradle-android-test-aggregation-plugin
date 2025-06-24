import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin.Companion.kotlinNodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
    alias(libs.plugins.android.lib)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.ksp)
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))

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

listOf(
    the<NodeJsEnvSpec>(),
    rootProject.the<NodeJsEnvSpec>(),
    rootProject.the<YarnRootEnvSpec>(),
).forEach {
    it.download = false
    it.downloadBaseUrl = null
}

dependencies {
    ksp(libs.moshi.codegen)
    "androidMainImplementation"(libs.moshi.kotlin)
    "jvmMainImplementation"(libs.moshi.kotlin)
    commonTestImplementation(libs.kotlin.test)
}
