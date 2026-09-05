package io.github.gmazzo.test.aggregation

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.provider.Property
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.typeOf
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.testing.base.TestingExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension

public class TestAggregationBasePlugin : Plugin<Project> {

    public companion object {
        public const val DEFAULT_NAME: String = "aggregatedTestResults"
    }

    override fun apply(target: Project): Unit = with(target) {
        apply(plugin = "reporting-base")

        val reporting = the<ReportingExtension>()
        reporting.reports {
            registerFactory(TestAggregationResultsSpec::class.java) { name ->
                objects.newInstance<DefaultTestAggregationResultsSpec>(name).apply spec@{
                    required
                        .convention(true)
                        .finalizeValueOnRead()

                    variants.configureEach {

                        aggregate
                            .convention(true)
                            .finalizeValueOnRead()

                        binaryData.finalizeValueOnRead()

                    }

                    htmlOutputLocation
                        .convention(reporting.baseDirectory.dir(name.defaultResultsDir + "/html"))
                        .finalizeValueOnRead()

                    junitXMLOutputLocation
                        .convention(reporting.baseDirectory.dir(name.defaultResultsDir + "/junit"))
                        .finalizeValueOnRead()

                    reportTask =
                        tasks.register<AggregatedTestReport>("aggregatedTestResultsReport${name.taskSuffix}") {
                            group = LifecycleBasePlugin.VERIFICATION_GROUP
                            description = "Aggregates test results for all test variants"

                            variantsBinaryData.value(variants.elements.map { list ->
                                list.asSequence()
                                    .filter { it.aggregate.get() }
                                    .associate { it.name to it.binaryData }
                            })
                            htmlReportLocation.value(this@spec.htmlOutputLocation)
                            junitXMLReportLocation.value(this@spec.junitXMLOutputLocation)
                        }
                }
            }
            registerFactory(TestAggregationCoverageSpec::class.java) { name ->
                objects.newInstance<DefaultTestAggregationCoverageSpec>(name).apply spec@{
                    required
                        .convention(provider { plugins.hasPlugin("jacoco") })
                        .finalizeValueOnRead()

                    variants.configureEach {

                        aggregate
                            .convention(true)
                            .finalizeValueOnRead()

                        sources.finalizeValueOnRead()

                        classes.finalizeValueOnRead()

                        coverageData.finalizeValueOnRead()

                    }

                    htmlOutputLocation
                        .convention(reporting.baseDirectory.dir(name.defaultCoverageDir + "/html"))
                        .finalizeValueOnRead()

                    xmlOutputLocation
                        .convention(reporting.baseDirectory.file(name.defaultCoverageDir + "/coverage.xml"))
                        .finalizeValueOnRead()

                    reportTask = tasks.register("aggregatedTestCoverageReport${name.taskSuffix}") {
                        group = LifecycleBasePlugin.VERIFICATION_GROUP
                        description = "Aggregates test coverage report for all test variants"

                        // TODO bind data
                    }
                }
            }
        }

        tasks.register("aggregatedTestReport") {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = "Aggregates test results and coverage for all test variants"

            dependsOn(reporting.reports.withType<TestAggregationSpec<*, *>>())
        }
    }

    private val String.defaultResultsDir
        get() = when (this) {
            DEFAULT_NAME -> "aggregated-test-results"
            else -> "aggregated-test/$this/results"
        }

    private val String.defaultCoverageDir
        get() = when (this) {
            DEFAULT_NAME -> "aggregated-test-coverage"
            else -> "aggregated-test/$this/coverage"
        }

    private val String.taskSuffix
        get() = when (this) {
            DEFAULT_NAME -> ""
            else -> "For${replaceFirstChar { it.uppercase() }}"
        }

}
