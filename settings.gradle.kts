plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

apply(from = "gradle/shared.settings.gradle.kts")

rootProject.name = "gradle-android-test-aggregation-plugin"

includeBuild("plugin")
include("demo-project-legacy:app")
include("demo-project-legacy:domain")
include("demo-project-legacy:login")
include("demo-project-legacy:kmp")
include("demo-project-legacy:ui-tests")
