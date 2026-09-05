@file:OptIn(ExperimentalPathApi::class)

package io.github.gmazzo.test.aggregation

import javax.inject.Inject
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import org.gradle.api.DefaultTask
import org.gradle.api.DomainObjectSet
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.internal.tasks.testing.junit.result.JUnitXmlResultOptions
import org.gradle.api.internal.tasks.testing.report.generic.GenericHtmlTestReportGenerator
import org.gradle.api.internal.tasks.testing.report.generic.JunitXmlTestReportGenerator
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.newInstance

@CacheableTask
public abstract class AggregatedTestReport : DefaultTask() {

    @get:Inject
    protected abstract val objects: ObjectFactory

    @get:Internal
    public abstract val variantsBinaryData: MapProperty<String, FileCollection>

    @get:Input
    internal val variantNames = variantsBinaryData.map { it.keys }

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    @get:SkipWhenEmpty
    internal val binaryData = variantsBinaryData.map { it.values }

    @get:OutputDirectory
    @get:Optional
    public abstract val htmlReportLocation: DirectoryProperty

    @get:OutputDirectory
    @get:Optional
    public abstract val junitXMLReportLocation: DirectoryProperty

    @TaskAction
    internal fun generateHTMLReport() {
        val outputDir = htmlReportLocation.asFile.orNull?.toPath() ?: return
        outputDir.deleteRecursively()

        val generator = objects.newInstance<GenericHtmlTestReportGenerator>(outputDir)
        generator.generate(binaryData.get().flatMap { it.files}.map { it.toPath() })
    }

    @TaskAction
    internal fun generateXMLReport() {
        val outputDir = junitXMLReportLocation.asFile.orNull?.toPath() ?: return
        outputDir.deleteRecursively()

        val options = JUnitXmlResultOptions(true, true, true, true)

        for ((variant, files) in variantsBinaryData.get()) {
            val generator = objects.newInstance<JunitXmlTestReportGenerator>(outputDir.resolve(variant), options)
            generator.generate(files.map { it.toPath() })
        }
    }

}
