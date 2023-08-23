package io.github.gmazzo.android.test.aggregation

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
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

internal fun TestAggregationExtension.aggregateProject(project: Project, config: Configuration) =
    modules.includes(project) &&
            config.dependencies.add(project.dependencies.testAggregation(project))

private fun TestAggregationExtension.Modules.includes(project: Project) =
    (includes.get().isEmpty() || project in includes.get()) && project !in excludes.get()
