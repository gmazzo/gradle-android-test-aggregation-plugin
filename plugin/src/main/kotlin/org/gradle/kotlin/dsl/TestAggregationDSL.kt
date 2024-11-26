package org.gradle.kotlin.dsl

import com.android.build.api.dsl.BuildType
import com.android.build.api.dsl.ProductFlavor
import io.github.gmazzo.android.test.aggregation.UsageTestAggregationCompatibilityRule
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.attributes.Usage
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

const val USAGE_TEST_AGGREGATION = "test-aggregation"

fun DependencyHandler.testAggregation(dependency: Any): Dependency = create(dependency).also {
    (it as? ProjectDependency)?.apply {
        UsageTestAggregationCompatibilityRule.bind(dependencyProject)

        attributes {
            attribute(Usage.USAGE_ATTRIBUTE, dependencyProject.objects.named(USAGE_TEST_AGGREGATION))
        }
    }
}

val BuildType.aggregateTestCoverage: Property<Boolean>
    get() = extensions.getByName<Property<Boolean>>(::aggregateTestCoverage.name)

val ProductFlavor.aggregateTestCoverage: Property<Boolean>
    get() = extensions.getByName<Property<Boolean>>(::aggregateTestCoverage.name)

val KotlinJvmTarget.aggregateTestCoverage: Property<Boolean>
    get() = (this as ExtensionAware).extensions.getByName<Property<Boolean>>(::aggregateTestCoverage.name)
