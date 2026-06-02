@file:Suppress("DEPRECATION")

package io.github.gmazzo.android.test.aggregation

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.extension.impl.CurrentAndroidGradlePluginVersion
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.HasUnitTest
import com.android.build.api.variant.Variant
import com.android.build.gradle.tasks.factory.AndroidUnitTest
import com.android.builder.model.Version.ANDROID_GRADLE_PLUGIN_VERSION
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.kotlin.dsl.aggregateTestCoverage
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.testAggregation
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.util.GradleVersion

internal val Project.android
    get() = extensions.getByName<CommonExtension>("android")

internal val Project.androidComponents
    get() = extensions.getByName<AndroidComponentsExtension<*, *, *>>("androidComponents")

internal val Project.testAggregationExtension: TestAggregationExtension
    get() = extensions.findByType()
        ?: extensions.create<TestAggregationExtension>("testAggregation").apply {
            modules.includes.finalizeValueOnRead()
            modules.excludes.finalizeValueOnRead()
        }

private fun String.asVersion() = GradleVersion.version(this)

internal fun Project.ensureMinVersions() {
    if (GradleVersion.current() < BuildConfig.MIN_GRADLE_VERSION.asVersion()) {
        error("This plugin requires Gradle ${BuildConfig.MIN_GRADLE_VERSION}} or later. Current is ${GradleVersion.current()}")
    }
    if (agpVersion.asVersion() < BuildConfig.MIN_AGP_VERSION.asVersion()) {
        error("This plugin requires Gradle ${BuildConfig.MIN_AGP_VERSION} or later. Current is $agpVersion")
    }
}

private val agpVersion
    get() = runCatching { CurrentAndroidGradlePluginVersion.CURRENT_AGP_VERSION.version }.getOrElse { ex1 ->
        runCatching { ANDROID_GRADLE_PLUGIN_VERSION }.getOrElse { ex2 ->
            ex1.addSuppressed(ex2)
            throw IllegalStateException(
                "Failed to get current AGP version, ${BuildConfig.MIN_AGP_VERSION} or later is required.",
                ex1
            )
        }
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
internal fun CommonExtension.shouldAggregate(variant: Variant) = sequence {
    yield(buildTypes[variant.buildType!!].aggregateTestCoverage)
    yieldAll(
        variant.productFlavors.asSequence()
            .map { (_, flavor) -> productFlavors[flavor] }
            .map { it.aggregateTestCoverage })
}.mapNotNull { it.orNull }.fold(true) { acc, it -> acc && it }

internal fun TestAggregationExtension.aggregateProject(
    project: Project,
    config: Configuration
) =
    modules.includes(project) &&
        config.dependencies.add(project.dependencies.testAggregation(project))

private fun TestAggregationExtension.Modules.includes(project: Project) =
    (includes.get()
        .isEmpty() || project.path in includes.get()) && project.path !in excludes.get()

internal fun Project.unitTestTaskOf(variant: Variant) = (variant as? HasUnitTest)
    ?.unitTest
    ?.let { tasks.named<AbstractTestTask>("test${it.name.replaceFirstChar { it.uppercase() }}") }

internal val TaskProvider<AbstractTestTask>.execData
    get() = map {
        when (it) {
            is AndroidUnitTest -> it.jacocoCoverageOutputFile
            else -> it.extensions.getByType<JacocoTaskExtension>().destinationFile
        }
    }
