package io.github.gmazzo.test.aggregation

import io.github.gmazzo.test.aggregation.TestAggregationReport.BaseVariant
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider

public interface TestAggregationCoverageReport :
    TestAggregationReport<TestAggregationCoverageReport.Variant, AggregatedTestCoverageTask> {

    public override val variants: NamedDomainObjectContainer<Variant>

    public val jacocoClasspath: ConfigurableFileCollection

    public val htmlOutputLocation: DirectoryProperty

    public val xmlOutputLocation: RegularFileProperty

    public val csvOutputLocation: RegularFileProperty

    public override val reportTask: TaskProvider<AggregatedTestCoverageTask>

    public fun addTestSuite(mainSources: SourceSet, testSuite: JvmTestSuite)

    public interface Variant : BaseVariant {

        public val displayName: Property<String>

        public val sources: ConfigurableFileCollection

        public val classes: ConfigurableFileCollection

        public val coverageData: ConfigurableFileCollection

    }

}
