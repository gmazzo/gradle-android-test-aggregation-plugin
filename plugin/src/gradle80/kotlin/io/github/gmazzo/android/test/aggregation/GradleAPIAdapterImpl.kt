package io.github.gmazzo.android.test.aggregation

import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.attributes.TestSuiteType.TEST_SUITE_TYPE_ATTRIBUTE
import org.gradle.api.attributes.TestSuiteType.UNIT_TEST
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.testing.AggregateTestReport
import org.gradle.kotlin.dsl.named
import org.gradle.testing.jacoco.plugins.JacocoCoverageReport

class GradleAPIAdapterImpl : GradleAPIAdapter {

    override fun AttributeContainer.setDefaultTestSuite(objects: ObjectFactory) {
        attribute(TEST_SUITE_TYPE_ATTRIBUTE, objects.named(UNIT_TEST))
    }

    override fun AggregateTestReport.setDefaultTestSuite() {
        testType.set(UNIT_TEST)
    }

    override fun JacocoCoverageReport.setDefaultTestSuite() {
        testType.set(UNIT_TEST)
    }

    override val ProjectDependency.objects: ObjectFactory
        get() = dependencyProject.objects

    override val ProjectDependency.projectPath
        get() = dependencyProject.path

}
