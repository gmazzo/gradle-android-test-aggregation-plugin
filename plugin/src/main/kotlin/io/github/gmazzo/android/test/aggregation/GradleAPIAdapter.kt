package io.github.gmazzo.android.test.aggregation

import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.testing.AggregateTestReport
import org.gradle.testing.jacoco.plugins.JacocoCoverageReport
import java.util.ServiceLoader

interface GradleAPIAdapter {

    fun AttributeContainer.setDefaultTestSuite(objects: ObjectFactory)

    fun AggregateTestReport.setDefaultTestSuite()

    fun JacocoCoverageReport.setDefaultTestSuite()

    val ProjectDependency.objects: ObjectFactory

    val ProjectDependency.projectPath: String

    companion object : GradleAPIAdapter by ServiceLoader.load(GradleAPIAdapter::class.java).single()

}
