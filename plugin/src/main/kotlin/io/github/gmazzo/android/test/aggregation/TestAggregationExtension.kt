package io.github.gmazzo.android.test.aggregation

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Nested
import org.gradle.kotlin.dsl.invoke

public abstract class TestAggregationExtension {

    @get:Nested
    public abstract val modules: Modules

    public fun modules(configure: Action<Modules>): Unit = configure(modules)

    /**
     * Controls which module should be automatically aggregated
     */
    public abstract class Modules {

        /**
         * List of [Project.getPath] that should be included in the aggregation. If empty, all projects are included
         */
        public abstract val includes: SetProperty<String>

        /**
         * List of [Project.getPath] that should be excluded from the aggregation
         */
        public abstract val excludes: SetProperty<String>

        public fun include(vararg includes: Project): TestAggregationExtension.Modules = apply {
            this.includes.addAll(includes.map { it.path })
        }

        public fun include(includes: Iterable<Project>): TestAggregationExtension.Modules = apply {
            this.includes.addAll(includes.map { it.path })
        }

        public fun include(vararg includes: ProjectDependency): Unit = with(GradleAPIAdapter) {
            this@Modules.includes.addAll(includes.map { it.projectPath })
        }

        public fun exclude(vararg excludes: Project): TestAggregationExtension.Modules = apply {
            this.excludes.addAll(excludes.map { it.path })
        }

        public fun exclude(excludes: Iterable<Project>): TestAggregationExtension.Modules = apply {
            this.excludes.addAll(excludes.map { it.path })
        }

        public fun exclude(vararg excludes: ProjectDependency): Unit = with(GradleAPIAdapter) {
            this@Modules.excludes.addAll(excludes.map { it.projectPath })
        }

    }

}

