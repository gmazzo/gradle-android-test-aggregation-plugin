package io.github.gmazzo.android.test.aggregation

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.TestedExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.the

internal val Project.android
    get() = the<TestedExtension>()

internal val Project.androidComponents
    get() = extensions.getByName<AndroidComponentsExtension<*, *, *>>("androidComponents")
