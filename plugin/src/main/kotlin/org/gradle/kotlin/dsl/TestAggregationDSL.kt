package org.gradle.kotlin.dsl

import io.github.gmazzo.android.test.aggregation.GradleAPIAdapter
import io.github.gmazzo.android.test.aggregation.UsageTestAggregationCompatibilityRule
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE

public const val USAGE_TEST_AGGREGATION: String = "test-aggregation"

public fun DependencyHandler.testAggregation(dependency: Any): ProjectDependency =
    (create(dependency) as ProjectDependency).apply {
        UsageTestAggregationCompatibilityRule.bind(attributesSchema)

        attributes {
            attribute(
                USAGE_ATTRIBUTE,
                with(GradleAPIAdapter) { objects.named(USAGE_TEST_AGGREGATION) },
            )
        }
    }
