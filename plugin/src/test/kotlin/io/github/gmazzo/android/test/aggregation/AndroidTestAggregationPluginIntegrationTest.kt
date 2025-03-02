package io.github.gmazzo.android.test.aggregation

import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
sealed class AndroidTestAggregationPluginIntegrationTest(private val gradleVersion: String) {

    class Min : AndroidTestAggregationPluginIntegrationTest("8.13")

    class Latest : AndroidTestAggregationPluginIntegrationTest(GradleVersion.current().version)

    private val tempDir = File(System.getenv("TEMP_DIR"))

    @Test
    fun `should aggregate projects`() {
        val projectDir = tempDir.resolve("project/gradle-${gradleVersion}").apply {
            deleteRecursively()
            File(this@AndroidTestAggregationPluginIntegrationTest.javaClass.getResource("/project")!!.path).copyRecursively(this)
        }

        GradleRunner.create()
            .withProjectDir(projectDir)
            .withGradleVersion(gradleVersion)
            .withPluginClasspath()
            .withArguments("check", "-s")
            .build()

        Assertions.assertEquals(
            javaClass.getResource("/expected-coverage.xml")!!.readText().withoutSessionInfo,
            projectDir.resolve("build/reports/jacoco/jacocoAggregatedReport/jacocoAggregatedReport.xml")
                .readText().withoutSessionInfo,
        )
    }

    private val String.withoutSessionInfo
        get() = replace("<sessioninfo[^>]+/>".toRegex(), "")

}
