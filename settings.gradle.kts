apply(from = "gradle/shared.settings.gradle.kts")

rootProject.name = "gradle-android-test-aggregation-plugin"

includeBuild("plugin")
include("app")
include("domain")
include("login")
