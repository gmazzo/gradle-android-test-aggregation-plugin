package io.github.gmazzo.test.aggregation

import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

public fun interface TestAggregationReportKotlinExtension {

    public operator fun invoke(target: KotlinTarget)

    // for Groovy DSL support
    public fun call(target: KotlinTarget) {
        invoke(target)
    }

}
