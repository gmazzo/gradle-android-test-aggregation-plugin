import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootEnvSpec

plugins {
    alias(libs.plugins.android.multiplatform)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.ksp)
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))

kotlin {
    androidLibrary {
        namespace = "com.example.login"

        compileSdk = libs.versions.android.compileSDK.get().toInt()
        minSdk = libs.versions.android.minSDK.get().toInt()
    }
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
