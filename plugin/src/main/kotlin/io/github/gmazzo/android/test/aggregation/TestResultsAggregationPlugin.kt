package io.github.gmazzo.android.test.aggregation

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.attributes.TestSuiteType
import org.gradle.api.attributes.VerificationType
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.testing.AggregateTestReport
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.the
import org.gradle.language.base.plugins.LifecycleBasePlugin

class TestResultsAggregationPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply(plugin = "reporting-base")
        apply(plugin = "test-report-aggregation")

        val testResultsReport =
            the<ReportingExtension>().reports.create<AggregateTestReport>("testAggregateTestReport") {
                testType.set(TestSuiteType.UNIT_TEST)
            }

        val testReportAggregation by configurations

        allprojects {
            fun aggregate() = testReportAggregation.dependencies
                .add((project.dependencies.create(project) as ModuleDependency).attributes {
                    attribute(
                        TestSuiteType.TEST_SUITE_TYPE_ATTRIBUTE,
                        objects.named(TestSuiteType.UNIT_TEST)
                    )
                    attribute(
                        VerificationType.VERIFICATION_TYPE_ATTRIBUTE,
                        objects.named(VerificationType.TEST_RESULTS)
                    )
                })

            plugins.withId("jvm-test-suite") {
                aggregate()
            }

            plugins.withId("com.android.base") {
                apply<AndroidTestResultsAggregationPlugin>()
                aggregate()
            }
        }

        plugins.withId("base") {
            tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME).configure {
                dependsOn(testResultsReport.reportTask)
            }
        }
    }

}
