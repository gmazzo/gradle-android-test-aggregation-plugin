@file:Suppress("UnstableApiUsage")

package io.github.gmazzo.android.test.aggregation

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.aggregateTestCoverage
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.typeOf
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetsContainer
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

internal abstract class AndroidTestBaseAggregationPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply(plugin = "com.android.base")

        android.buildTypes.configureEach {
            extensions.add(
                typeOf<Property<Boolean>>(),
                ::aggregateTestCoverage.name,
                objects.property<Boolean>()
            )
        }
        android.productFlavors.configureEach {
            extensions.add(
                typeOf<Property<Boolean>>(),
                ::aggregateTestCoverage.name,
                objects.property<Boolean>()
            )
        }
    }

    internal abstract class KMPSupport {

        fun Project.configure() {
            the<KotlinTargetsContainer>().targets.withType<KotlinJvmTarget> target@{
                this@target as ExtensionAware

                if (extensions.findByName(::aggregateTestCoverage.name) == null) {
                    extensions.add(
                        typeOf<Property<Boolean>>(),
                        ::aggregateTestCoverage.name,
                        objects.property<Boolean>()
                    )
                }

                configureTarget(this@target)
            }
        }

        abstract fun Project.configureTarget(target: KotlinJvmTarget)

    }

}
