apply(from = "../gradle/shared.settings.gradle.kts")

rootProject.name = "plugin"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
