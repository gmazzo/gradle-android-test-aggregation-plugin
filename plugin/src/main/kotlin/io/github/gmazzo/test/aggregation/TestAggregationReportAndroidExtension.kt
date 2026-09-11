package io.github.gmazzo.test.aggregation

import com.android.build.api.variant.Variant

public fun interface TestAggregationReportAndroidExtension {

    public operator fun invoke(androidVariant: Variant)

    // for Groovy DSL support
    public fun call(androidVariant: Variant) {
        invoke(androidVariant)
    }

}
