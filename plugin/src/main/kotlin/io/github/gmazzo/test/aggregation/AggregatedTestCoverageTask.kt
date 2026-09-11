@file:OptIn(ExperimentalPathApi::class)

package io.github.gmazzo.test.aggregation

import javax.inject.Inject
import kotlin.io.path.ExperimentalPathApi
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
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
    public abstract val variants: SetProperty<TestAggregationCoverageReport.Variant>

    @get:Input
    internal val variantsDisplayNames =
        variants.map { v -> v.map { it.displayName } }

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

    @get:OutputDirectory
    @get:Optional
    public abstract val htmlOutputLocation: DirectoryProperty

    @get:OutputFile
    @get:Optional
    public abstract val xmlOutputLocation: RegularFileProperty

    @get:OutputFile
    @get:Optional
    public abstract val csvOutputLocation: RegularFileProperty

    @TaskAction
    internal fun generateCoverageReport() {
        check(jacocoClasspath.files.isNotEmpty()) {
            "Could not find default JaCoCo Ant task classpath. Did you apply the 'jacoco' plugin?"
        }

        workerExecutor.classLoaderIsolation().submit(AggregatedTestCoverageAction::class) params@{
            this@params.antLibraryClasspath.from(this@AggregatedTestCoverageTask.jacocoClasspath)
            this@params.reportName.set(this@AggregatedTestCoverageTask.name)
            this@params.variants.set(this@AggregatedTestCoverageTask.variants.map { it.resolved() })
            this@params.htmlOutputLocation.set(this@AggregatedTestCoverageTask.htmlOutputLocation)
            this@params.xmlOutputLocation.set(this@AggregatedTestCoverageTask.xmlOutputLocation)
            this@params.csvOutputLocation.set(this@AggregatedTestCoverageTask.csvOutputLocation)
        }
    }

    private fun Set<TestAggregationCoverageReport.Variant>.resolved(): Set<TestAggregationCoverageReport.Variant> =
        mapTo(linkedSetOf()) {
            objects.newInstance<TestAggregationCoverageReport.Variant>(it.name).apply {
                displayName.value(it.displayName).disallowChanges()
                sources.from(it.sources.files).disallowChanges()
                classes.from(it.classes.files).disallowChanges()
                coverageData.from(it.coverageData.files).disallowChanges()
            }
        }

}
