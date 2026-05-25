package org.gradle.kotlin.dsl

import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

public val KotlinJvmTarget.aggregateTestCoverage: Property<Boolean>
    get() = (this as ExtensionAware).extensions.getByName<Property<Boolean>>(::aggregateTestCoverage.name)
