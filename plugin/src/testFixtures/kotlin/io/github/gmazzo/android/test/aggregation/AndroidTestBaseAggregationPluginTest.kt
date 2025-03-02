package io.github.gmazzo.android.test.aggregation

import org.gradle.kotlin.dsl.apply
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull

abstract class AndroidTestBaseAggregationPluginTest(
    private val pluginId: String,
    private val expectedTask: String,
) {

    private val root = ProjectBuilder.builder()
        .withName("root")
        .build()

    private val app = ProjectBuilder.builder()
        .withName("app")
        .withParent(root)
        .build()

    private val lib1 = ProjectBuilder.builder()
        .withName("lib1")
        .withParent(root)
        .build()

    private val lib2 = ProjectBuilder.builder()
        .withName("lib2")
        .withParent(root)
        .build()

    @Test
    fun `applies correctly on a multi module project`() {
        root.apply(plugin = pluginId)
        app.apply(plugin = "com.android.application")
        lib1.apply(plugin = "com.android.library")
        lib2.apply(plugin = "java")

        assertNotNull(root.tasks.findByName(expectedTask))
    }

}
