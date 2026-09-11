package io.github.gmazzo.android.test.aggregation

import org.gradle.api.Project
import org.gradle.api.problems.ProblemGroup
import org.gradle.api.problems.ProblemId
import org.gradle.api.problems.Problems
import org.gradle.kotlin.dsl.support.serviceOf

private const val SCHEDULE_MESSAGE =
    "Scheduled for removal in 3.1.0"

internal const val OLD_API_DEPRECATION_MESSAGE =
    "Use new `io.github.gmazzo.test.aggregation` plugin instead. $SCHEDULE_MESSAGE"

internal fun Project.deprecationNotice(pluginId: String) {
    serviceOf<Problems>().reporter.report(ProblemId.create(
        "old-plugin-applied",
        "Applied '$pluginId' plugin",
        ProblemGroup.create("deprecated-api-usage", "Deprecated API usage")
    )) {
        details("Plugin is deprecated. $SCHEDULE_MESSAGE")
        solution("Migrate to the new `io.github.gmazzo.test.aggregation` plugin")
        documentedAt("See https://github.com/gmazzo/gradle-tests-aggregation-plugin/migration-3.x.md")
    }
}
