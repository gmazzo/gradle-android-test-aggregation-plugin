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

        abstract val includes: SetProperty<Project>

        abstract val excludes: SetProperty<Project>

        fun include(vararg includes: Project) = apply {
            this.includes.addAll(*includes)
        }

        fun include(includes: Iterable<Project>) = apply {
            this.includes.addAll(includes)
        }

        fun include(vararg includes: ProjectDependency) =
            include(includes.map { it.dependencyProject })

        fun exclude(vararg excludes: Project) = apply {
            this.excludes.addAll(*excludes)
        }

        fun exclude(excludes: Iterable<Project>) = apply {
            this.excludes.addAll(excludes)
        }

        fun exclude(vararg excludes: ProjectDependency) =
            exclude(excludes.map { it.dependencyProject })

    }

}

