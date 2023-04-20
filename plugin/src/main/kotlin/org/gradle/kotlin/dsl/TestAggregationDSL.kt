package org.gradle.kotlin.dsl

import com.android.build.api.dsl.BuildType
import com.android.build.api.dsl.ProductFlavor
import io.github.gmazzo.android.test.aggregation.UsageTestAggregationCompatibilityRule
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.attributes.Usage
import org.gradle.api.provider.Property

fun DependencyHandler.testAggregation(project: Project): Dependency = create(project).apply {
    (this as? ModuleDependency)?.attributes {
        attribute(
            Usage.USAGE_ATTRIBUTE,
            project.objects.named(UsageTestAggregationCompatibilityRule.USAGE_TEST_AGGREGATION)
        )
    }
}

val BuildType.aggregateTestCoverage: Property<Boolean>
    get() = extensions.getByName<Property<Boolean>>(::aggregateTestCoverage.name)

val ProductFlavor.aggregateTestCoverage: Property<Boolean>
    get() = extensions.getByName<Property<Boolean>>(::aggregateTestCoverage.name)
