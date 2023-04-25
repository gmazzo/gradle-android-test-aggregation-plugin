package io.github.gmazzo.android.test.aggregation

import org.gradle.api.attributes.AttributeCompatibilityRule
import org.gradle.api.attributes.CompatibilityCheckDetails
import org.gradle.api.attributes.Usage
import org.gradle.kotlin.dsl.USAGE_TEST_AGGREGATION

internal class UsageTestAggregationCompatibilityRule : AttributeCompatibilityRule<Usage> {

    override fun execute(details: CompatibilityCheckDetails<Usage>) = with(details) {
        if (consumerValue?.name == USAGE_TEST_AGGREGATION && producerValue?.name == Usage.JAVA_RUNTIME) {
            compatible()
        }
    }

}
