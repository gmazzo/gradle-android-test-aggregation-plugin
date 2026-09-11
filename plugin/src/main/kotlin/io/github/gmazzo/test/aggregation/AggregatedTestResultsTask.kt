@file:OptIn(ExperimentalPathApi::class)

package io.github.gmazzo.test.aggregation

import javax.inject.Inject
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.tasks.testing.junit.result.JUnitXmlResultOptions
import org.gradle.api.internal.tasks.testing.report.generic.GenericHtmlTestReportGenerator
import org.gradle.api.internal.tasks.testing.report.generic.JunitXmlTestReportGenerator
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.newInstance

@CacheableTask
public abstract class AggregatedTestResultsTask : DefaultTask() {

    @get:Inject
    protected abstract val objects: ObjectFactory

    @get:Internal
    public abstract val variants: SetProperty<TestAggregationResultsReport.Variant>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    @get:SkipWhenEmpty
    internal val variantsBinaryData =
        variants.map { v -> v.map { it.binaryData.asFileTree } }

    @get:OutputDirectory
    @get:Optional
    public abstract val htmlOutputLocation: DirectoryProperty

    @get:OutputDirectory
    @get:Optional
    public abstract val junitXMLOutputLocation: DirectoryProperty

    @TaskAction
    internal fun generateHTMLReport() {
        val outputDir = htmlOutputLocation.asFile.orNull?.toPath() ?: return
        outputDir.deleteRecursively()

        val generator = objects.newInstance<GenericHtmlTestReportGenerator>(outputDir)
        generator.generate(variants.get().flatMap { it.binaryDataDirs })
    }

    @TaskAction
    internal fun generateXMLReport() {
        val outputDir = junitXMLOutputLocation.asFile.orNull?.toPath() ?: return
        outputDir.deleteRecursively()

        val options = JUnitXmlResultOptions(true, true, true, true)

        for (variant in variants.get()) {
            val generator = objects.newInstance<JunitXmlTestReportGenerator>(
                outputDir.resolve(variant.name
                    .removePrefix(":")
                    .replace(':', '-')),
                options
            )
            generator.generate(variant.binaryDataDirs)
        }
    }

    private val TestAggregationResultsReport.Variant.binaryDataDirs
        get() = binaryData.asFileTree.mapTo(linkedSetOf()) { it.parentFile.toPath() }.toList()

}
