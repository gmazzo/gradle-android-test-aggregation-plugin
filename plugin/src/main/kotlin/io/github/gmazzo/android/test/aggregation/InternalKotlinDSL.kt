@file:Suppress("DEPRECATION")

package io.github.gmazzo.android.test.aggregation

import org.gradle.api.Project
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.kotlin.dsl.named
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

internal fun Project.unitTestTaskOf(target: KotlinTarget) =
    tasks.named<AbstractTestTask>("${(target.disambiguationClassifier ?: target.name)}Test")
