package io.github.gmazzo.test.aggregation

import io.github.gmazzo.test.aggregation.TestAggregationBasePlugin.Companion.DEFAULT_COVERAGE_NAME
import io.github.gmazzo.test.aggregation.TestAggregationBasePlugin.Companion.DEFAULT_RESULTS_NAME
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.base.TestingExtension

public class TestAggregationPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply<TestAggregationBasePlugin>()

        val reporting = the<ReportingExtension>()
        val aggregateConfig = configurations.dependencyScope("aggregateTestsFrom")
        val testResults = reporting.reports.create<TestAggregationResultsReport>(DEFAULT_RESULTS_NAME) {
            aggregateFrom.extendsFrom(aggregateConfig)
        }
        val testCoverage = reporting.reports.create<TestAggregationCoverageReport>(DEFAULT_COVERAGE_NAME) {
            aggregateFrom.extendsFrom(aggregateConfig)
        }

        plugins.withId("java") {
            val suites = the<TestingExtension>().suites

            suites.withType<JvmTestSuite> suite@{
                testResults.addTestSuite(this@suite)
            }

            plugins.withId("jacoco") {
                val main = the<SourceSetContainer>().getByName(SourceSet.MAIN_SOURCE_SET_NAME)
                val tests = suites.getByName<JvmTestSuite>(SourceSet.TEST_SOURCE_SET_NAME)

                testCoverage.addTestSuite(main, tests)
            }
        }

        plugins.withId("org.jetbrains.kotlin.multiplatform") {
            with(KMPSupport) { install(testResults, testCoverage) }
        }

        for (androidPluginId in listOf("com.android.base", "com.android.kotlin.multiplatform.library")) {
            plugins.withId(androidPluginId) {
                with(AndroidSupport) { install(testResults, testCoverage) }
            }
        }
    }

}
