enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

apply(from = "gradle/shared.settings.gradle.kts")

rootProject.name = "gradle-android-test-aggregation-plugin"

includeBuild("plugin")
include("demo-project:app")
include("demo-project:domain")
include("demo-project:login")
include("demo-project:ui-tests")
