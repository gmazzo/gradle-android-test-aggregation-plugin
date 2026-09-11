package io.github.gmazzo.test.aggregation

import org.gradle.api.Task
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.typeOf
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension

private const val AGGREGATE_EXTENSION_NAME = "aggregateTests"

internal val String.capitalized: String
    get() = replaceFirstChar { it.uppercase() }

@Suppress("UNCHECKED_CAST")
internal fun ExtensionAware.aggregateTests(objects: ObjectFactory) =
    when (val existing = extensions.findByName(AGGREGATE_EXTENSION_NAME)) {
        null -> objects.property<Boolean>()
            .convention(true)
            .apply { finalizeValueOnRead() }
            .also { aggregateTests = it }
        else -> existing as Property<Boolean>
    }

@Suppress("UNCHECKED_CAST")
internal var ExtensionAware.aggregateTests: Property<Boolean>
    get() = extensions.getByName(AGGREGATE_EXTENSION_NAME) as Property<Boolean>
    set(value) {
        when (val existing = extensions.findByName(AGGREGATE_EXTENSION_NAME)) {
            null -> extensions.add(typeOf<Property<Boolean>>(), AGGREGATE_EXTENSION_NAME, value)
            else -> check(existing === value)
        }
    }

internal val Task.coverageFile
    get() = extensions.findByType<JacocoTaskExtension>()?.destinationFile
