package io.github.gmazzo.test.aggregation

import io.github.gmazzo.test.aggregation.TestAggregationReport.BaseVariant
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.tasks.TaskProvider

public interface TestAggregationResultsReport :
    TestAggregationReport<TestAggregationResultsReport.Variant, AggregatedTestResultsTask> {

    public override val variants: NamedDomainObjectContainer<Variant>

    public val htmlOutputLocation: DirectoryProperty

    public val junitXMLOutputLocation: DirectoryProperty

    public override val reportTask: TaskProvider<AggregatedTestResultsTask>

    public fun addTestSuite(testSuite: JvmTestSuite)

    public interface Variant : BaseVariant {

        public val binaryData: ConfigurableFileCollection

    }

}
