package io.github.gmazzo.test.aggregation

import io.github.gmazzo.test.aggregation.TestAggregationReport.BaseVariant
import java.util.concurrent.Callable
import javax.inject.Inject
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Task
import org.gradle.api.artifacts.ResolvableConfiguration
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.TaskProvider

internal abstract class AbstractTestAggregationReport<Variant : BaseVariant, ReportTask : Task> :
    TestAggregationReport<Variant, ReportTask>,
    Callable<TaskProvider<ReportTask>> {

    @get:Inject
    abstract val objects: ObjectFactory

    override lateinit var aggregateFrom: NamedDomainObjectProvider<ResolvableConfiguration>

    val filteredVariants = variants.matching { it.aggregate.get() }

    override fun call() = reportTask

}
