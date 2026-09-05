package io.github.gmazzo.test.aggregation

import org.gradle.api.Action
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.reporting.ReportSpec
import org.gradle.api.tasks.Nested
import io.github.gmazzo.test.aggregation.TestAggregationSpec.BaseVariant
import io.github.gmazzo.test.aggregation.TestAggregationResultsSpec.Variant

public interface TestAggregationResultsSpec : TestAggregationSpec<Variant, AggregatedTestReport> {

    public val htmlOutputLocation: DirectoryProperty

    public val junitXMLOutputLocation: DirectoryProperty

    public interface Variant : BaseVariant {

        public val binaryData: ConfigurableFileCollection

    }

}
