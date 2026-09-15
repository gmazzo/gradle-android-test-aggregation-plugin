@file:OptIn(ExperimentalPathApi::class)

package io.github.gmazzo.test.aggregation

import io.github.gmazzo.test.aggregation.TestAggregationCoverageReport.Variant
import javax.inject.Inject
import kotlin.io.path.ExperimentalPathApi
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.submit
import org.gradle.workers.WorkerExecutor


@CacheableTask
public abstract class AggregatedTestCoverageTask : DefaultTask() {

    @get:Inject
    protected abstract val objects: ObjectFactory

    @get:Inject
    protected abstract val workerExecutor: WorkerExecutor

    @get:Internal
    public abstract val variants: SetProperty<Variant>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal val variantsSources =
        variants.map { v -> v.map { it.sources.asFileTree } }

    @get:Classpath
    @get:SkipWhenEmpty
    internal val variantsClasses =
        variants.map { v -> v.map { it.classes.asFileTree } }

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    internal val variantsCoverageData =
        variants.map { v -> v.map { it.coverageData.asFileTree } }

    @get:Classpath
    public abstract val jacocoClasspath: ConfigurableFileCollection

    @get:Input
    @get:Optional
    public abstract val htmlRequired: Property<Boolean>

    @get:OutputDirectory
    @get:Optional
    public abstract val htmlOutputLocation: DirectoryProperty

    @get:Input
    @get:Optional
    public abstract val xmlRequired: Property<Boolean>

    @get:OutputFile
    @get:Optional
    public abstract val xmlOutputLocation: RegularFileProperty

    @get:Input
    @get:Optional
    public abstract val csvRequired: Property<Boolean>

    @get:OutputFile
    @get:Optional
    public abstract val csvOutputLocation: RegularFileProperty

    @TaskAction
    internal fun generateCoverageReport() {
        check(jacocoClasspath.files.isNotEmpty()) {
            "Could not find default JaCoCo Ant task classpath. Did you apply the 'jacoco' plugin?"
        }

        htmlOutputLocation.asFile.orNull?.apply { deleteRecursively() }
        xmlOutputLocation.asFile.orNull?.apply { deleteRecursively() }
        csvOutputLocation.asFile.orNull?.apply { deleteRecursively() }

        workerExecutor.classLoaderIsolation().submit(AggregatedTestCoverageAction::class) params@{
            this@params.antLibraryClasspath
                .from(this@AggregatedTestCoverageTask.jacocoClasspath)
                .disallowChanges()
            this@params.reportName
                .value(this@AggregatedTestCoverageTask.name)
                .disallowChanges()
            this@params.variants
                .value(this@AggregatedTestCoverageTask.variants.map { list ->
                    list.associate { it.name to it.resolved() }
                })
                .disallowChanges()
            this@params.htmlOutputLocation
                .value(
                    this@AggregatedTestCoverageTask.htmlRequired
                        .zip(this@AggregatedTestCoverageTask.htmlOutputLocation) { required, location ->
                            if (required) location else null
                        })
                .disallowChanges()
            this@params.xmlOutputLocation
                .value(
                    this@AggregatedTestCoverageTask.xmlRequired
                        .zip(this@AggregatedTestCoverageTask.xmlOutputLocation) { required, location ->
                            if (required) location else null
                        })
                .disallowChanges()
            this@params.csvOutputLocation
                .value(
                    this@AggregatedTestCoverageTask.csvRequired
                        .zip(this@AggregatedTestCoverageTask.csvOutputLocation) { required, location ->
                            if (required) location else null
                        })
                .disallowChanges()
        }
    }

    private fun Variant.resolved() =
        objects.newInstance<Variant>(name).apply new@{
            this@new.sources.from(this@resolved.sources.files).disallowChanges()
            this@new.classes.from(this@resolved.classes.files).disallowChanges()
            this@new.coverageData.from(this@resolved.coverageData.files).disallowChanges()
        }

}
