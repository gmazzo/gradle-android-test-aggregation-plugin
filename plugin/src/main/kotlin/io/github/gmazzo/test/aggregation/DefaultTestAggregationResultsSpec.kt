package io.github.gmazzo.test.aggregation

import java.util.concurrent.Callable
import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.reporting.ReportSpec
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskProvider

internal abstract class DefaultTestAggregationResultsSpec @Inject constructor() :
    TestAggregationResultsSpec,
    Callable<TaskProvider<AggregatedTestReport>> {

    override lateinit var reportTask: TaskProvider<AggregatedTestReport>

    override fun call() = reportTask

}
