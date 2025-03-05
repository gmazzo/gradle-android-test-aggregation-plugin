package io.github.gmazzo.android.test.aggregation

import io.github.gmazzo.android.test.aggregation.BuildConfig.MIN_GRADLE_VERSION
import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AndroidTestAggregationPluginIntegrationBaseTest(
    private val gradleVersion: GradleVersion = MIN_GRADLE_VERSION,
) {

    private val tempDir = File(System.getenv("TEMP_DIR"))

    @Test
    fun `should aggregate projects`() {
        val projectDir = tempDir.resolve("project/gradle-${gradleVersion.version}")

        projectDir.deleteRecursively()
        File(javaClass.getResource("/project")!!.path).copyRecursively(projectDir)

        GradleRunner.create()
            .withProjectDir(projectDir)
            .withGradleVersion(gradleVersion.version)
            .withPluginClasspath()
            .withArguments("check", "-s")
            .build()

        assertEquals(
            javaClass.getResource("/expected-coverage.xml")!!.readText().withoutSessionInfo,
            projectDir.resolve("build/reports/jacoco/jacocoAggregatedReport/jacocoAggregatedReport.xml")
                .readText().withoutSessionInfo,
        )
    }

    private val String.withoutSessionInfo
        get() = replace("<sessioninfo[^>]+/>".toRegex(), "")

}
