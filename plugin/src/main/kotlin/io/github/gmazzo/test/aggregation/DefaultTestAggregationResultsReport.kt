package io.github.gmazzo.test.aggregation

import io.github.gmazzo.test.aggregation.TestAggregationResultsReport.Variant
import javax.inject.Inject
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.tasks.TaskProvider

internal abstract class DefaultTestAggregationResultsReport @Inject constructor() :
    AbstractTestAggregationReport<Variant, AggregatedTestResultsTask>(),
    TestAggregationResultsReport {

    override lateinit var reportTask: TaskProvider<AggregatedTestResultsTask>

    override fun addTestSuite(testSuite: JvmTestSuite): Variant {
        val aggregate = (testSuite as ExtensionAware).aggregateTests(objects)

        val variant = variants.maybeCreate(testSuite.name)
        variant.aggregate.convention(aggregate)

        testSuite.targets.all target@{
            variant.dependsOn(testTask)
            variant.binaryData.from(testTask.map { it.binaryResultsDirectory })

            testTask.configure task@{ this@task.aggregateTests = aggregate }
        }
        return variant
    }

}
