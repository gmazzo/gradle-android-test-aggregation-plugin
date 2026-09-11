package io.github.gmazzo.test.aggregation

import io.github.gmazzo.test.aggregation.TestAggregationReport.BaseVariant
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.reporting.ReportSpec
import org.gradle.api.tasks.TaskProvider

public interface TestAggregationReport<Variant : BaseVariant, ReportTask : Task> : ReportSpec {

    public val variants: NamedDomainObjectContainer<Variant>

    public val aggregateFrom: Configuration

    public val reportTask: TaskProvider<ReportTask>

    public interface BaseVariant : Named {

        override fun getName(): String

        public val aggregate: Property<Boolean>

        public val dependsOn: SetProperty<Any>

        public fun dependsOn(vararg tasks: Any) {
            dependsOn.addAll(*tasks)
        }

    }

}
