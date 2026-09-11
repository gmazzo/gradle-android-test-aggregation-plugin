package io.github.gmazzo.test.aggregation

import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.kotlin.dsl.addKotlinTarget
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.typeOf
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.ExecutionTaskHolder
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.MAIN_COMPILATION_NAME
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetWithTests
import org.jetbrains.kotlin.gradle.testing.internal.KotlinTestReport

internal object KMPSupport {

    fun Project.installBase() = configure<ReportingExtension> {
        reports.withType<TestAggregationResultsReport> report@{
            (this@report as ExtensionAware).extensions
                .add(
                    typeOf<TestAggregationReportKotlinExtension>(),
                    "addKotlinTarget",
                    ResultsExtension(objects, this@report)
                )
        }
        reports.withType<TestAggregationCoverageReport> report@{
            (this@report as ExtensionAware).extensions
                .add(
                    typeOf<TestAggregationReportKotlinExtension>(),
                    "addKotlinTarget",
                    CoverageExtension(objects, this@report)
                )
        }
    }

    fun Project.install(
        testResults: TestAggregationResultsReport,
        testCoverage: TestAggregationCoverageReport,
    ) {
        the<KotlinMultiplatformExtension>().targets.all target@{
            if (platformType == KotlinPlatformType.common) return@target // it would never have tests
            if (platformType == KotlinPlatformType.androidJvm) return@target // will be handled by AndroidSupport

            testResults.addKotlinTarget(this@target)

            if (supportsJacoco) {
                plugins.withId("jacoco") {
                    testCoverage.addKotlinTarget(this@target)
                }
            }
        }
    }

    private val KotlinTarget.supportsJacoco
        get() = when (platformType) {
            KotlinPlatformType.jvm, KotlinPlatformType.androidJvm -> true
            else -> false
        }

    class ResultsExtension(
        private val objects: ObjectFactory,
        private val report: TestAggregationResultsReport,
    ) : TestAggregationReportKotlinExtension {

        override fun invoke(target: KotlinTarget) {
            check(target is KotlinTargetWithTests<*, *>) {
                "Test aggregation is only supported for targets with tests, but ${target.name} does not have any test runs"
            }

            val targetAggregate = (target as ExtensionAware).aggregateTests(objects)

            val variant = report.variants.maybeCreate(target.name)
            variant.aggregate.convention(targetAggregate)

            target.testRuns.all run@{
                if (this@run !is ExecutionTaskHolder<*>) return@run

                variant.dependsOn(executionTask)
                variant.binaryData.from(executionTask.map {
                    when (it) {
                        is AbstractTestTask -> it.binaryResultsDirectory
                        is KotlinTestReport -> it.testResults
                        else -> emptyArray<Any>()
                    }
                })
            }
        }
    }

    class CoverageExtension(
        private val objects: ObjectFactory,
        private val report: TestAggregationCoverageReport,
    ) : TestAggregationReportKotlinExtension {

        override fun invoke(target: KotlinTarget) {
            check(target is KotlinTargetWithTests<*, *>) {
                "Test aggregation is only supported for targets with tests, but ${target.name} does not have any test runs"
            }

            val targetAggregate = (target as ExtensionAware).aggregateTests(objects)
            val main = target.compilations.getByName(MAIN_COMPILATION_NAME)

            val variant = report.variants.maybeCreate(target.name)
            variant.dependsOn(main.compileTaskProvider)
            variant.aggregate.convention(targetAggregate)
            variant.sources.from(main.allKotlinSourceSets.asSequence().map {
                listOf(
                    it.kotlin.srcDirs,
                    it.generatedKotlin.srcDirs,
                )
            }.asIterable())
            variant.classes.from(main.output.classesDirs)

            target.testRuns.all run@{
                if (this@run !is ExecutionTaskHolder<*>) return@run

                executionTask.configure task@{ this@task.aggregateTests = targetAggregate }
                variant.dependsOn(executionTask)
                variant.coverageData.from(executionTask.map { task ->
                    when (task) {
                        is AbstractTestTask -> task.coverageFile
                        is KotlinTestReport -> task.testTasks.mapNotNull { it.coverageFile }
                        else -> emptyArray<Any>()
                    }
                })
            }
        }
    }

}
