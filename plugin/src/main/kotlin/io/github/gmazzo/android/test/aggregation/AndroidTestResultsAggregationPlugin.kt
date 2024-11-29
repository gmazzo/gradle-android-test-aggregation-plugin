package io.github.gmazzo.android.test.aggregation

import com.android.build.api.variant.HasUnitTest
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.TestSuiteType
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.VerificationType
import org.gradle.kotlin.dsl.USAGE_TEST_AGGREGATION
import org.gradle.kotlin.dsl.aggregateTestCoverage
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.named

abstract class AndroidTestResultsAggregationPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply<AndroidTestBaseAggregationPlugin>()

        val testResultsElements = configurations.create("testResultsElements") {
            isCanBeConsumed = true
            isCanBeResolved = false
            isVisible = false
            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(USAGE_TEST_AGGREGATION))
                attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.VERIFICATION))
                attribute(
                    TestSuiteType.TEST_SUITE_TYPE_ATTRIBUTE,
                    objects.named(TestSuiteType.UNIT_TEST)
                )
                attribute(
                    VerificationType.VERIFICATION_TYPE_ATTRIBUTE,
                    objects.named(VerificationType.TEST_RESULTS)
                )
            }
        }

        androidComponents.onVariants { variant ->
            testResultsElements.outgoing.artifacts(provider {
                val aggregate = (variant as? HasUnitTest)?.unitTest != null && android.shouldAggregate(variant)

                if (aggregate) listOf(unitTestTaskOf(variant)!!.flatMap { it.binaryResultsDirectory }) else emptyList()
            })
        }

        onKotlinJVMTargets target@{
            testResultsElements.outgoing.artifacts(provider {
                val aggregate = this@target.aggregateTestCoverage.getOrElse(true)

                if (aggregate) listOf(unitTestTaskOf(this@target).flatMap { it.binaryResultsDirectory }) else emptyList()
            })
        }
    }

}
