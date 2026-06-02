@file:Suppress("DEPRECATION")

package io.github.gmazzo.android.test.aggregation

import com.android.builder.model.Version.ANDROID_GRADLE_PLUGIN_VERSION
import io.github.gmazzo.android.test.aggregation.BuildConfig.MIN_AGP_VERSION
import io.github.gmazzo.android.test.aggregation.BuildConfig.MIN_GRADLE_VERSION
import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.internal.PluginUnderTestMetadataReading.readImplementationClasspath
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AndroidTestAggregationPluginIntegrationTest {

    fun arguments() = listOf(
        arrayOf(MIN_GRADLE_VERSION, MIN_AGP_VERSION),
        arrayOf(GradleVersion.current().version, ANDROID_GRADLE_PLUGIN_VERSION),
    )

    @ParameterizedTest(name = "gradle={0}, android={1}")
    @MethodSource("arguments")
    fun `should aggregate projects`(gradleVersion: String, agpVersion: String) {
        val projectDir =
            File(System.getenv("TEMP_DIR"), "project/gradle-${gradleVersion}-agp-${agpVersion}")

        projectDir.deleteRecursively()
        File(javaClass.getResource("/project")!!.path).copyRecursively(projectDir)

        GradleRunner.create()
            .withProjectDir(projectDir)
            .withGradleVersion(gradleVersion)
            .withPluginClasspath("agp-$agpVersion-metadata.properties")
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

    private fun GradleRunner.withPluginClasspath(vararg andOthers: String) = withPluginClasspath(
        readImplementationClasspath() +
            andOthers.flatMap {
                readImplementationClasspath(
                    Thread.currentThread().getContextClassLoader().getResource(it)
                )
            }
    )

}
