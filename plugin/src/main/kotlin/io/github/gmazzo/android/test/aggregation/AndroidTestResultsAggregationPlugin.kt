package io.github.gmazzo.android.test.aggregation

import com.android.build.api.variant.HasUnitTest
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.TestSuiteName
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.VerificationType
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.USAGE_TEST_AGGREGATION
import org.gradle.kotlin.dsl.aggregateTestCoverage
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.named
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

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
                    TestSuiteName.TEST_SUITE_NAME_ATTRIBUTE,
                    objects.named(SourceSet.TEST_SOURCE_SET_NAME)
                )
                attribute(
                    VerificationType.VERIFICATION_TYPE_ATTRIBUTE,
                    objects.named(VerificationType.TEST_RESULTS)
                )
            }
        }

        androidComponents.onVariants { variant ->
            testResultsElements.outgoing.artifacts(provider {
                val aggregate =
                    (variant as? HasUnitTest)?.unitTest != null && android.shouldAggregate(variant)

                if (aggregate) listOf(unitTestTaskOf(variant)!!.flatMap { it.binaryResultsDirectory }) else emptyList()
            })
        }

        plugins.withId("kotlin-multiplatform") {
            with(KMPSupport(testResultsElements)) { configure() }
        }
    }

    internal class KMPSupport(
        private val testResultsElements: Configuration,
    ) : AndroidTestBaseAggregationPlugin.KMPSupport() {

        override fun Project.configureTarget(target: KotlinJvmTarget) {
            testResultsElements.outgoing.artifacts(provider {
                val aggregate = target.aggregateTestCoverage.getOrElse(true)

                if (aggregate) listOf(unitTestTaskOf(target).flatMap { it.binaryResultsDirectory }) else emptyList()
            })
        }

    }

}
