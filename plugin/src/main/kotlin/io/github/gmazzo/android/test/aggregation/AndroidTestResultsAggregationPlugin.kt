package io.github.gmazzo.android.test.aggregation

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.TestSuiteType
import org.gradle.api.attributes.VerificationType
import org.gradle.api.tasks.testing.Test
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.named

abstract class AndroidTestResultsAggregationPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply(plugin = "com.android.base")

        configurations.create("testResultsElements") {
            isCanBeConsumed = true
            isCanBeResolved = false
            isVisible = false
            attributes {
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
            android.unitTestVariants.all {
                val testTask = tasks.named<Test>("test${name.capitalized()}")

                outgoing.artifact(testTask.flatMap { it.binaryResultsDirectory })
            }
        }
    }

}
