package io.github.gmazzo.android.test.aggregation

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.TestSuiteType
import org.gradle.api.reporting.ReportingExtension
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.testAggregation
import org.gradle.kotlin.dsl.the
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.testing.jacoco.plugins.JacocoCoverageReport

class TestCoverageAggregationPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply(plugin = "reporting-base")
        apply(plugin = "jacoco-report-aggregation")

        val jacocoReport =
            the<ReportingExtension>().reports.create<JacocoCoverageReport>("jacocoTestReport") {
                testType.set(TestSuiteType.UNIT_TEST)
            }

        val jacocoAggregation by configurations

        allprojects {
            plugins.withId("jacoco") {
                jacocoAggregation.dependencies.add(dependencies.testAggregation(project))
            }

            plugins.withId("com.android.base") {
                apply<AndroidTestCoverageAggregationPlugin>()

                jacocoAggregation.dependencies.add(dependencies.testAggregation(project))
            }
        }

        plugins.withId("base") {
            tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME).configure {
                dependsOn(jacocoReport.reportTask)
            }
        }
    }

}
