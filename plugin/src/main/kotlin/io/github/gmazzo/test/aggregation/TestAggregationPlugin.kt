package io.github.gmazzo.test.aggregation

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.provider.Property
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.maybeCreate
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.typeOf
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.testing.base.TestingExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension

public class TestAggregationPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply<TestAggregationBasePlugin>()

        val reporting = the<ReportingExtension>()

        val testResults = reporting.reports
            .maybeCreate<TestAggregationResultsSpec>("aggregatedTestResults")

        val testCoverage = reporting.reports
            .maybeCreate<TestAggregationCoverageSpec>("aggregatedTestCoverage")

        plugins.withId("jvm-test-suite") {
            the<TestingExtension>().suites.withType<JvmTestSuite> suite@{
                this@suite.targets.all target@{
                    val aggExtension = createAggregateExtension(this@target, this@suite)

                    with(testResults.variants.maybeCreate(this@target.name)) {
                        aggregate.convention(aggExtension)
                        binaryData.from(this@target.testTask.map { it.binaryResultsDirectory })
                        testResults.reportTask.bindTo(this@target.testTask)
                    }
                    with(testCoverage.variants.maybeCreate(this@target.name)) {
                        aggregate.convention(aggExtension)
                        sources.from(this@suite.sources.allSource)
                        classes.from(this@target.testTask.map { it.candidateClassFiles })
                        coverageData.from(this@target.testTask.map { it.coverageFile })
                        testCoverage.reportTask.bindTo(this@target.testTask)
                    }
                }
            }
        }
    }

    private fun Project.createAggregateExtension(
        vararg onTargets: Any
    ) = objects.property<Boolean>()
        .convention(true)
        .apply { finalizeValueOnRead() }
        .also {
            for (target in onTargets) {
                (target as ExtensionAware).extensions
                    .add(typeOf<Property<Boolean>>(), "aggregate", it)
            }
        }

    context(specVariant: TestAggregationSpec.BaseVariant)
    private fun TaskProvider<*>.bindTo(task: TaskProvider<*>) = configure {
        dependsOn(specVariant.aggregate.map { if (it) task else null })
    }

    private val AbstractTestTask.coverageFile
        get() = extensions.findByType<JacocoTaskExtension>()?.destinationFile

}
