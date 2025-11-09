package io.github.gmazzo.android.test.aggregation

import java.util.ServiceLoader
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.testing.AggregateTestReport
import org.gradle.testing.jacoco.plugins.JacocoCoverageReport

public interface GradleAPIAdapter {

    public fun AttributeContainer.setDefaultTestSuite(objects: ObjectFactory)

    public fun AggregateTestReport.setDefaultTestSuite()

    public fun JacocoCoverageReport.setDefaultTestSuite()

    public val ProjectDependency.objects: ObjectFactory

    public val ProjectDependency.projectPath: String

    public companion object : GradleAPIAdapter by ServiceLoader.load(GradleAPIAdapter::class.java).single()

}
