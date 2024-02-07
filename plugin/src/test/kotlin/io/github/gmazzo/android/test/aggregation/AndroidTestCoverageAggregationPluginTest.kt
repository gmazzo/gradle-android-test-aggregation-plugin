package io.github.gmazzo.android.test.aggregation

class AndroidTestCoverageAggregationPluginTest : AndroidTestBaseAggregationPluginTest(
    pluginId = "io.github.gmazzo.test.aggregation.coverage",
    expectedTask = "jacocoAggregatedReport",
)
