package io.github.gmazzo.android.test.aggregation

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.testing.AggregateTestReport
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.language.base.plugins.LifecycleBasePlugin

public class TestResultsAggregationPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        ensureMinVersions()
        ensureItsNotJava()

        apply(plugin = "reporting-base")
        apply(plugin = "test-report-aggregation")

        val extension = testAggregationExtension

        val testResultsReport =
            extensions.getByType<ReportingExtension>().reports.create<AggregateTestReport>("testAggregatedReport") {
                with(GradleAPIAdapter) { setDefaultTestSuite() }
            }

        val testReportAggregation by configurations

        allprojects {

            plugins.withId("jvm-test-suite") {
                extension.aggregateProject(project, testReportAggregation)
            }

            plugins.withId("com.android.base") {
                apply<AndroidTestResultsAggregationPlugin>()

                extension.aggregateProject(project, testReportAggregation)
            }
        }

        plugins.withId("base") {
            tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME).configure {
                dependsOn(testResultsReport.reportTask)
            }
        }
    }

}
