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
import io.github.gmazzo.test.aggregation.TestAggregationSpec.BaseVariant
import io.github.gmazzo.test.aggregation.TestAggregationCoverageSpec.Variant

public interface TestAggregationCoverageSpec : TestAggregationSpec<Variant, Task> {

    public val htmlOutputLocation: DirectoryProperty

    public val xmlOutputLocation: RegularFileProperty

    public interface Variant : BaseVariant {

        public val sources: ConfigurableFileCollection

        public val classes: ConfigurableFileCollection

        public val coverageData: ConfigurableFileCollection

    }

}
