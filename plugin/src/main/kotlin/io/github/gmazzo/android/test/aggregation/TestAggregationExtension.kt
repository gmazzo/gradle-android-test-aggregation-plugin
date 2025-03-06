package io.github.gmazzo.android.test.aggregation

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Nested
import org.gradle.kotlin.dsl.invoke

abstract class TestAggregationExtension {

    @get:Nested
    abstract val modules: Modules

    fun modules(configure: Action<Modules>) = configure(modules)

    /**
     * Controls which module should be automatically aggregated
     */
    abstract class Modules {

        /**
         * List of [Project.getPath] that should be included in the aggregation. If empty, all projects are included
         */
        abstract val includes: SetProperty<String>

        /**
         * List of [Project.getPath] that should be excluded from the aggregation
         */
        abstract val excludes: SetProperty<String>

        fun include(vararg includes: Project) = apply {
            this.includes.addAll(includes.map { it.path })
        }

        fun include(includes: Iterable<Project>) = apply {
            this.includes.addAll(includes.map { it.path })
        }

        fun include(vararg includes: ProjectDependency) = with(GradleAPIAdapter) {
            this@Modules.includes.addAll(includes.map { it.projectPath })
        }

        fun exclude(vararg excludes: Project) = apply {
            this.excludes.addAll(excludes.map { it.path })
        }

        fun exclude(excludes: Iterable<Project>) = apply {
            this.excludes.addAll(excludes.map { it.path })
        }

        fun exclude(vararg excludes: ProjectDependency) = with(GradleAPIAdapter) {
            this@Modules.excludes.addAll(excludes.map { it.projectPath })
        }

    }

}

