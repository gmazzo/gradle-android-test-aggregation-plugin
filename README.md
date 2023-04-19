[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.github.gmazzo.test.aggregation.coverage)](https://plugins.gradle.org/plugin/io.github.gmazzo.test.aggregation.coverage)
![Build Status](https://github.com/gmazzo/gradle-android-test-aggregation-plugin/actions/workflows/build.yaml/badge.svg)
[![Coverage](https://codecov.io/gh/gmazzo/gradle-android-test-aggregation-plugin/branch/main/graph/badge.svg?token=D5cDiPWvcS)](https://codecov.io/gh/gmazzo/gradle-android-test-aggregation-plugin)

# gradle-android-test-aggregation-plugin
A couple Gradle plugins to make Android modules to work with 
[JaCoCo Report Aggregation Plugin](https://docs.gradle.org/current/userguide/jacoco_report_aggregation_plugin.html) and
[Test Report Aggregation Plugin](https://docs.gradle.org/current/userguide/test_report_aggregation_plugin.html)

# Usage
Apply the plugin at the **root** project and/or at **any child** project that uses it:
```kotlin
plugins {
    id("io.github.gmazzo.test.aggregation.coverage") version "<latest>" 
    // and/or
    id("io.github.gmazzo.test.aggregation.results") version "<latest>"
}
```

The `jacocoTestReport` (for `coverage`) and `testAggregateTestReport` (for `results`) will be created 
to aggregate test results from all projects in the build

The following is the old README.me of the demo project of my [Medium article](https://medium.com/p/53e912b2e63c) about this topic, 
now promoted to dedicated Gradle plugins: 
[io.github.gmazzo.test.aggregation.coverage](https://plugins.gradle.org/plugin/io.github.gmazzo.test.aggregation.coverage) and
[io.github.gmazzo.test.aggregation.results](https://plugins.gradle.org/plugin/io.github.gmazzo.test.aggregation.results)

# Demo project for aggregating Jacoco Android & JVM coverage reports
This is an example project that illustrates how can the 
[JaCoCo Report Aggregation Plugin](https://docs.gradle.org/current/userguide/jacoco_report_aggregation_plugin.html) and
[Test Report Aggregation Plugin](https://docs.gradle.org/current/userguide/test_report_aggregation_plugin.html)
can be used to aggregate a complex Android project with JVM modules in a single `:jacocoTestReport` and `:testAggregateTestReport` tasks.

## Project structure
- A `plugin` included build that provides the `coverage` root plugin
- A `demo-project` with:
  - An `app` android module (with Robolectric tests)
  - A `login` android library module (with JUnit4/JVM tests)
  - A `domain` jvm module (with tests)

## The `test-aggregation` root plugin
The plugin fills the gaps between [AGP](https://developer.android.com/studio/releases/gradle-plugin) and 
[JaCoCo Report Aggregation Plugin](https://docs.gradle.org/current/userguide/jacoco_report_aggregation_plugin.html)
by providing the necessary setup missing:
- It applies `jacoco-report-aggregation` and `test-report-aggregation` at root project
- Creates `jacocoTestReport` and `testAggregateTestReport` for `TestSuiteType.UNIT_TEST`
- If a module applies `jacoco` plugin, it adds it to the `jacocoAggregation` and `testReportAggregation` root configurations
- If a module applies the `java` plugin, makes its child `jacocoTestReport` task to depend on `test`
- If a module applies the `android` plugin:
  - it enables by default `BuildType.enableUnitTestCoverage` on `debug` to produce jacoco exec files
  - adds the `codeCoverageExecutionData`, `codeCoverageSources`, `codeCoverageElements` (classes) and `testResultsElements`
    outgoing variants, to allow `jacoco-report-aggregation` and `test-report-aggregation` to aggregate it

Please note that JVM still need to manually apply `jacoco` plugin (this is an intentional opt-in behavior)
[build.gradle.kts](build.gradle.kts#L3)

## Producing an aggregated report for the whole project
The task `:jacocoTestReport` is added to the root project when applying this plugin and it can be
run to produce the report. All dependent `test` tasks will be run too to produce the required execution data.
![Aggregated JaCoCo Report example](README-aggregated-jacoco-report.png)

The same for `:testAggregateTestReport`:
![Aggregated Test Report example](README-aggregated-test-report.png)

## Use the `coverage` plugin on your own project
The easiest way to adopt this plugin, is to put the [coverage.gradle.kts](coverage-plugin/src/main/kotlin/coverage.gradle.kts)
on your `buildSrc/src/main/kotlin` folder.
Create the `buildSrc` project if missing and make sure to apply `kotlin-dsl` plugin on it

Don't forget its companion [CoveragePluginDSL.kt](coverage-plugin/src/main/kotlin/org/gradle/kotlin/dsl/CoveragePluginDSL.kt) file

## The `aggregateTestCoverage` DSL extension
This is an opt-in/out switch meant to be used when having `productFlavors`.

`enableUnitTestCoverage` is a `BuildType` setting (default on `debug`). When having flavors, you'll
have many coverage reports to produce targeting `debug` (one per flavor variant).
You can use `enableUnitTestCoverage.set(false)` to turn aggregation off for an specific `ProductFlavor`. 
Basically, the variant won't be added to the `codeCoverageExecutionData` configuration, so `:jacocoTestReport` won't compute it

For instance, `app` module has a `environment` dimension with 2 flavors: `stage` and `prod`.
Without any extra settings, `:jacocoTestReport` will depend on `:app:testStageDebugUnitTest` and 
`:app:testProdDebugUnitTest` (running its `src/test/` tests effectively twice).
You may choose which flavors participates in the aggregated report by doing:
```kotlin
    productFlavors {
        create("stage") { 
            dimension = "environment" 
        }
        create("prod") { 
            dimension = "environment"
            aggregateTestCoverage.set(false)
        }
    }
```
where it effectively only run `:app:testStageDebugUnitTest`
