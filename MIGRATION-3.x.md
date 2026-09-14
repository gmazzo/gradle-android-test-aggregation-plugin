# API 3.0 migration guide

This document describes the steps to migrate from version 2.x to 3.x of the plugin.

## Apply the new plugin

Replace all usages of `io.github.gmazzo.test.aggregation.coverage` and
   `io.github.gmazzo.test.aggregation.results` with `io.github.gmazzo.test.aggregation`

> [!NOTE]
> You can keep legacy plugins applies to simplify the migration,
> but it's strongly recommended to remove once you are done.

## Locate the new entrypoint tasks

The `2.x` version was registering two Gradle tasks as entrypoint: `jacocoAggregatedReport` and `testAggregatedReport`.

The `3.0` version registers a single task `aggregatedTestReport`, which depends on `aggregatedTestCoverageReport` and `aggregatedTestResultsReport` ones.

## Filtering modules / choosing what aggregate

**This a major behavior change:**

In version `2.x` was appling the plugin recursively across every project in the build.

In the opposite, version `3.x` requires you to explicitly declare which modules should be aggregated, with the following DSL:
```kotlin
dependencies {
    aggregateTestsFrom(project(":app"))
    aggregateTestsFrom(project(":foo"))
    aggregateTestsFrom(projects.bar)
}
```

Note that `2.x`s `modules` DSL is completely gone:
```kotlin
testAggregation {
    modules {
        include(project(":app"))
        exclude(projects.lib) // typesafe accessors are also supported!
    }
}
```
This is because now you can have full control of what's aggregated with the `aggregateTestsFrom` configuration

> [!IMPORTANT]
> It's not enough anymore that you apply the plugin at the root project.
> On `3.x`, the `io.github.gmazzo.test.aggregation` must be applied as well on
> every project getting aggregated (referenced at `aggregateTestsFrom` configuration).

## Enforcing aggregated code coverage metrics

Due to this feature was provided by
Gradle's official [JaCoCo Report Aggregation Plugin](https://docs.gradle.org/current/userguide/jacoco_report_aggregation_plugin.html),
this feature was completely removed from this plugin.

We can evaluate to port it at some point, but at first sight it doesn't makes sense.
This is becasue now we produce individual Coverage reports per module (and variant).

## Choosing with variants (from each project) to aggregate

The `aggregateTestCoverage` was replaced by `aggregateTests`.

The `aggregateTestsFrom` lets you choose which projects are aggregated.
But the plugin also discovers (and automatically aggregates) all variants of each:
- `JVMTestSuite`s for Java projects
- `Variant`s for Android projects
- `KotlinTarget`s for Kotlin Multiplatform projects

By default, each variant is configured to be aggregated.
You can use the `aggregateTests` DSL for this:
```kotlin
// for JVM
testing.suites.withType<JvmTestSuite> {
  aggregateTests = false
}

// for Android
androidComponents {
  onVariants {
    it.aggregateTests = it.buildType == "debug"
  }
}

// for Kotlin Multiplatform
kotlin {
  android { aggregateTests = false }
  jvm { aggregateTests = false }
}
```
