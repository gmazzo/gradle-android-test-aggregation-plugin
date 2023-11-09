package io.github.gmazzo.android.test.aggregation

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ComponentIdentity
import com.android.build.api.variant.GeneratesTestApk
import com.android.build.api.variant.UnitTest
import com.android.build.api.variant.Variant
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.coverage.JacocoReportTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.aggregateTestCoverage
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.testAggregation
import org.gradle.kotlin.dsl.the

internal val Project.android
    get() = the<BaseExtension>()

internal val Project.androidComponents
    get() = extensions.getByName<AndroidComponentsExtension<*, *, *>>("androidComponents")

internal val Project.testAggregationExtension: TestAggregationExtension
    get() = extensions.findByType()
        ?: extensions.create<TestAggregationExtension>("testAggregation").apply {
            modules.includes.finalizeValueOnRead()
            modules.excludes.finalizeValueOnRead()
        }

internal fun Project.ensureItsNotJava() = plugins.withId("java-base") {
    error("This plugin can not work with `java` plugin as well. It's recommended to apply it at the root project with at most the `base` plugin")
}

/**
 * `aggregateTestCoverage` applies to `BuildType`s and `Flavor`s and
 * can take 3 possible values: `true`, `false` or `null` (missing).
 *
 * Because of this, we may found conflicting declarations where a
 * `BuildType` is set to `true` but a `Flavor` to `false`.
 * The following logic is no honor the precedence order:
 * - If any component of the variant (buildType/flavor) says `true`, then `true`
 * - If any component of the variant says `false` (and other says nothing `null`), then `false`
 * - If no component says anything (`null`), then `true` (because its `BuildType` has `enableUnitTestCoverage = true`)
 */
internal fun BaseExtension.shouldAggregate(variant: Variant) =
    (sequenceOf(buildTypes[variant.buildType!!].aggregateTestCoverage) +
            variant.productFlavors.asSequence()
                .map { (_, flavor) -> productFlavors[flavor] }
                .map { it.aggregateTestCoverage })
        .mapNotNull { it.orNull }
        .reduceOrNull { acc, aggregate -> acc || aggregate } != false

internal fun TestAggregationExtension.aggregateProject(project: Project, config: Configuration) =
    modules.includes(project) &&
            config.dependencies.add(project.dependencies.testAggregation(project))

private fun TestAggregationExtension.Modules.includes(project: Project) =
    (includes.get().isEmpty() || project in includes.get()) && project !in excludes.get()

internal fun Project.unitTestTaskOf(variant: UnitTest) = provider {
    tasks.getByName<AbstractTestTask>("test${variant.name.capitalized()}")
}

internal fun <Type> Project.androidTestTaskOf(variant: Type) where Type : GeneratesTestApk, Type: ComponentIdentity = provider {
    tasks.getByName<JacocoReportTask>("create${variant.name.capitalized()}CoverageReport")
}
