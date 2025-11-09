package org.gradle.kotlin.dsl

import com.android.build.api.dsl.BuildType
import com.android.build.api.dsl.ProductFlavor
import io.github.gmazzo.android.test.aggregation.GradleAPIAdapter
import io.github.gmazzo.android.test.aggregation.UsageTestAggregationCompatibilityRule
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

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

public val BuildType.aggregateTestCoverage: Property<Boolean>
    get() = extensions.getByName<Property<Boolean>>(::aggregateTestCoverage.name)

public val ProductFlavor.aggregateTestCoverage: Property<Boolean>
    get() = extensions.getByName<Property<Boolean>>(::aggregateTestCoverage.name)

public val KotlinJvmTarget.aggregateTestCoverage: Property<Boolean>
    get() = (this as ExtensionAware).extensions.getByName<Property<Boolean>>(::aggregateTestCoverage.name)
