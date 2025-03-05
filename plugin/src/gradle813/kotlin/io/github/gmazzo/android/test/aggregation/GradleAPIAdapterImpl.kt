package io.github.gmazzo.android.test.aggregation

import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.attributes.TestSuiteName.TEST_SUITE_NAME_ATTRIBUTE
import org.gradle.api.internal.artifacts.dependencies.AbstractModuleDependency
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.SourceSet.TEST_SOURCE_SET_NAME
import org.gradle.api.tasks.testing.AggregateTestReport
import org.gradle.kotlin.dsl.named
import org.gradle.testing.jacoco.plugins.JacocoCoverageReport

class GradleAPIAdapterImpl : GradleAPIAdapter {

    override fun AttributeContainer.setDefaultTestSuite(objects: ObjectFactory) {
        attribute(TEST_SUITE_NAME_ATTRIBUTE, objects.named(TEST_SOURCE_SET_NAME))
    }

    override fun AggregateTestReport.setDefaultTestSuite() {
        testSuiteName.set(TEST_SOURCE_SET_NAME)
    }

    override fun JacocoCoverageReport.setDefaultTestSuite() {
        testSuiteName.set(TEST_SOURCE_SET_NAME)
    }

    override val ProjectDependency.objects: ObjectFactory
        get() = (this as AbstractModuleDependency).objectFactory

    override val ProjectDependency.projectPath
        get() = path

}
