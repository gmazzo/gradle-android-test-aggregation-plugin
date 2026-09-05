package io.github.gmazzo.test.aggregation

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.reporting.ReportSpec
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskProvider
import io.github.gmazzo.test.aggregation.TestAggregationSpec.BaseVariant
import org.gradle.api.provider.MapProperty

public interface TestAggregationSpec<Variant : BaseVariant, ReportTask : Task> : ReportSpec {

    public val required: Property<Boolean>

    public val variants: NamedDomainObjectContainer<Variant>

    public val reportTask: TaskProvider<ReportTask>

    public interface BaseVariant : Named {

        public val aggregate: Property<Boolean>

    }

}
