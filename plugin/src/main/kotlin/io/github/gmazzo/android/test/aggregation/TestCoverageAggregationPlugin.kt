package io.github.gmazzo.android.test.aggregation

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.testing.jacoco.plugins.JacocoCoverageReport
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

class TestCoverageAggregationPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        ensureMinVersions()
        ensureItsNotJava()

        apply(plugin = "reporting-base")
        apply(plugin = "jacoco-report-aggregation")

        val extension = testAggregationExtension
        val coverageExtension = (testAggregationExtension as ExtensionAware).extensions
            .create(PatternFilterable::class, "coverage", PatternSet::class)

        val jacocoReport =
            the<ReportingExtension>().reports.create<JacocoCoverageReport>("jacocoAggregatedReport") {
                with(GradleAPIAdapter) { setDefaultTestSuite() }
                reportTask.configure {
                    executionData.setFrom(files(*executionData.from.toTypedArray()).asFileTree)
                    classDirectories.setFrom(files(*classDirectories.from.toTypedArray()).asFileTree.matching(coverageExtension))
                }
            }

        tasks.register<JacocoCoverageVerification>("jacocoAggregatedCoverageVerification") {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = "Verifies code coverage metrics for the aggregated report"

            val reportTask = provider { jacocoReport.reportTask.get() } // breaks dependency
            executionData(reportTask.map { it.executionData })
            classDirectories.from(reportTask.map { it.classDirectories })
            sourceDirectories.from(reportTask.map { it.sourceDirectories })
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
