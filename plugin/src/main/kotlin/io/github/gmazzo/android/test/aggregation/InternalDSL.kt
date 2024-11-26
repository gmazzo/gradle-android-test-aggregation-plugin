package io.github.gmazzo.android.test.aggregation

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.HasUnitTest
import com.android.build.api.variant.Variant
import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.aggregateTestCoverage
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.testAggregation
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetsContainer
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMetadataTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

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

internal fun Project.unitTestTaskOf(variant: Variant) = (variant as? HasUnitTest)
    ?.unitTest
    ?.let { tasks.named<AbstractTestTask>("test${it.name.replaceFirstChar { it.uppercase() }}") }

internal fun Project.unitTestTaskOf(target: KotlinTarget) =
    tasks.named<AbstractTestTask>("${(target.disambiguationClassifier ?: target.name)}Test")

internal fun Project.onKotlinJVMTargets(action: KotlinJvmTarget.() -> Unit) = plugins.withId("kotlin-multiplatform") {
    the<KotlinTargetsContainer>().targets
        .withType<KotlinJvmTarget>(action)
}
