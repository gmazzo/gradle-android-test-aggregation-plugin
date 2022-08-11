apply(from = "gradle/shared.settings.gradle.kts")

rootProject.name = "android-jacoco-aggregated-demo"

include("app")
include("domain")
include("login")
includeBuild("coverage-plugin")
