@file:Suppress("UnstableApiUsage")

package io.github.gmazzo.android.test.aggregation

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.aggregateTestCoverage
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.typeOf

internal abstract class AndroidTestBaseAggregationPlugin : Plugin<Project> {

    internal abstract val extendedProperties: ListProperty<Property<*>>

    override fun apply(target: Project): Unit = with(target) {
        apply(plugin = "com.android.base")

        android.buildTypes.configureEach {
            extensions.add(
                typeOf<Property<Boolean>>(),
                ::aggregateTestCoverage.name,
                objects.property<Boolean>().also(extendedProperties::add)
            )
        }
        android.productFlavors.configureEach {
            extensions.add(
                typeOf<Property<Boolean>>(),
                ::aggregateTestCoverage.name,
                objects.property<Boolean>().also(extendedProperties::add)
            )
        }

        androidComponents.finalizeDsl {
            extendedProperties.finalizeValue()
            extendedProperties.get().forEach { it.finalizeValue() }
        }
    }

}
