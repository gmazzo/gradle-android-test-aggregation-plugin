package io.github.gmazzo.android.test.aggregation

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.TestSuiteType
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.the
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.testing.jacoco.plugins.JacocoCoverageReport

class TestCoverageAggregationPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        ensureItsNotJava()

        apply(plugin = "reporting-base")
        apply(plugin = "jacoco-report-aggregation")

        val extension = testAggregationExtension
        val coverageExtension = (testAggregationExtension as ExtensionAware).extensions
            .create(PatternFilterable::class, "coverage", PatternSet::class)

        val jacocoReport =
            the<ReportingExtension>().reports.create<JacocoCoverageReport>("jacocoAggregatedReport") {
                testType.set(TestSuiteType.UNIT_TEST)
                reportTask.configure {
                    classDirectories.setFrom(
                        files(*classDirectories.from.toTypedArray()).asFileTree
                            .matching(coverageExtension)
                    )
                }
            }

        val jacocoAggregation by configurations

        allprojects {
            plugins.withId("java") {
                plugins.withId("jacoco") {
                    extension.aggregateProject(project, jacocoAggregation)
                }
            }

            plugins.withId("com.android.base") {
                apply<AndroidTestCoverageAggregationPlugin>()

                extension.aggregateProject(project, jacocoAggregation)
            }
        }

        plugins.withId("base") {
            tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME).configure {
                dependsOn(jacocoReport.reportTask)
            }
        }
    }

}
