package io.github.gmazzo.test.aggregation

import io.github.gmazzo.test.aggregation.TestAggregationReport.BaseVariant
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider

public interface TestAggregationCoverageReport :
    TestAggregationReport<TestAggregationCoverageReport.Variant, AggregatedTestCoverageTask> {

    public override val variants: NamedDomainObjectContainer<Variant>

    @get:Nested
    public val content: Content

    public fun content(configure: Action<Content>) {
        configure.execute(content)
    }

    public val htmlOutputLocation: DirectoryProperty

    public val xmlOutputLocation: RegularFileProperty

    public val csvOutputLocation: RegularFileProperty

    public override val reportTask: TaskProvider<AggregatedTestCoverageTask>

    public fun addTestSuite(mainSources: SourceSet, testSuite: JvmTestSuite): Variant

    public interface Variant : BaseVariant {

        public val sources: ConfigurableFileCollection

        public val classes: ConfigurableFileCollection

        public val coverageData: ConfigurableFileCollection

    }

    public interface Content {

        public val includes: SetProperty<String>

        public fun include(vararg includes: String): Content = apply {
            this.includes.addAll(*includes)
        }

        public val excludes: SetProperty<String>

        public fun exclude(vararg excludes: String): Content = apply {
            this.excludes.addAll(*excludes)
        }

    }

}
