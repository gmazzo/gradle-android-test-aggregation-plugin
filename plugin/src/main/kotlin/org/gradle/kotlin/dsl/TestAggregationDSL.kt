package org.gradle.kotlin.dsl

import io.github.gmazzo.android.test.aggregation.OLD_API_DEPRECATION_MESSAGE
import io.github.gmazzo.android.test.aggregation.UsageTestAggregationCompatibilityRule
import io.github.gmazzo.test.aggregation.aggregateTests
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.gradle.api.internal.artifacts.dependencies.AbstractModuleDependency
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.testing.AbstractTestTask

@Deprecated(OLD_API_DEPRECATION_MESSAGE)
public const val USAGE_TEST_AGGREGATION: String = "test-aggregation"

@Deprecated(OLD_API_DEPRECATION_MESSAGE)
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

public val SourceSet.aggregateTests: Property<Boolean>
    get() = (this as ExtensionAware).aggregateTests

public val JvmTestSuite.aggregateTests: Property<Boolean>
    get() = (this as ExtensionAware).aggregateTests

public val AbstractTestTask.aggregateTests: Property<Boolean>
    get() = (this as ExtensionAware).aggregateTests
