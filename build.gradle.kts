plugins {
    alias(libs.plugins.android) apply false
    alias(libs.plugins.android.lib) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("io.github.gmazzo.test-aggregation")
}
