package io.github.gmazzo.android.test.aggregation

import org.gradle.api.Project
import org.gradle.api.attributes.AttributeCompatibilityRule
import org.gradle.api.attributes.AttributesSchema
import org.gradle.api.attributes.CompatibilityCheckDetails
import org.gradle.api.attributes.Usage
import org.gradle.kotlin.dsl.USAGE_TEST_AGGREGATION
import org.gradle.kotlin.dsl.add

internal class UsageTestAggregationCompatibilityRule : AttributeCompatibilityRule<Usage> {

    override fun execute(details: CompatibilityCheckDetails<Usage>) = with(details) {
        if (consumerValue?.name == USAGE_TEST_AGGREGATION && producerValue?.name == Usage.JAVA_RUNTIME) {
            compatible()
        }
    }

    companion object {

        fun bind(project: Project) {
            bind(project.dependencies.attributesSchema)
        }

        fun bind(schema: AttributesSchema) {
            schema
                .attribute(Usage.USAGE_ATTRIBUTE)
                .compatibilityRules
                .add(UsageTestAggregationCompatibilityRule::class)
        }

    }

}
