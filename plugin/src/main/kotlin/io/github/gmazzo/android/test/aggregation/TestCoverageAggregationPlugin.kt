package io.github.gmazzo.android.test.aggregation

import io.github.gmazzo.android.test.aggregation.AndroidTestCoverageAggregationPlugin.Companion.AGGREGATED_TEST_COVERAGE_ATTRIBUTE
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.attributes.TestSuiteType
import org.gradle.api.reporting.ReportingExtension
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
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
            fun aggregate() = jacocoAggregation.dependencies
                .add((project.dependencies.create(project) as ModuleDependency).attributes {
                    attribute(AGGREGATED_TEST_COVERAGE_ATTRIBUTE, true)
                })

            plugins.withId("jacoco") {
                aggregate()
            }

            plugins.withId("com.android.base") {
                apply<AndroidTestCoverageAggregationPlugin>()
                aggregate()
            }
        }

        plugins.withId("base") {
            tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME).configure {
                dependsOn(jacocoReport.reportTask)
            }
        }
    }

}
