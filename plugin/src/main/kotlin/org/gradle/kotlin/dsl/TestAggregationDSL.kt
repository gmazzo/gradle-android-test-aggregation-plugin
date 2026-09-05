package org.gradle.kotlin.dsl

import io.github.gmazzo.android.test.aggregation.TestAggregationExtension
import io.github.gmazzo.android.test.aggregation.UsageTestAggregationCompatibilityRule
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.gradle.api.internal.artifacts.dependencies.AbstractModuleDependency
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.plugins.jvm.JvmTestSuiteTarget
import org.gradle.api.provider.Property

public const val USAGE_TEST_AGGREGATION: String = "test-aggregation"

public fun DependencyHandler.testAggregation(dependency: Any): ProjectDependency =
    (create(dependency) as ProjectDependency).apply dep@{
        UsageTestAggregationCompatibilityRule.bind(attributesSchema)

        attributes {
            attribute(
                USAGE_ATTRIBUTE,
                (this@dep as AbstractModuleDependency).objectFactory.named(USAGE_TEST_AGGREGATION),
            )
        }
    }

public val JvmTestSuite.aggregate: Property<Boolean>
    get() = (this as ExtensionAware).extensions.getByName<Property<Boolean>>("aggregate")

public val JvmTestSuiteTarget.aggregate: Property<Boolean>
    get() = (this as ExtensionAware).extensions.getByName<Property<Boolean>>("aggregate")
