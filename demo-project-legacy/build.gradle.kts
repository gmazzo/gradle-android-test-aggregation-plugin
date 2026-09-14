import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils

buildscript {
    dependencies {
        classpath(libs.diffUtils)
    }
}

plugins {
    base
    id("io.github.gmazzo.test.aggregation.results")
    id("io.github.gmazzo.test.aggregation.coverage")
}

testAggregation {
    modules {
        include(
            projects.demoProjectLegacy.app,
            projects.demoProjectLegacy.domain,
            projects.demoProjectLegacy.login,
            projects.demoProjectLegacy.kmp,
        )
        exclude(rootProject)
    }
    coverage {
        exclude("**/ContentMainBinding*")
    }
}

tasks.jacocoAggregatedCoverageVerification {
    violationRules {
        rule {
            limit {// current 19%
                minimum = "0.19".toBigDecimal()
            }
            limit {// desired 80%
                minimum = "0.8".toBigDecimal()
                isFailOnViolation = false
            }
        }
    }
}

val aggregatedReportsSpecs = layout.projectDirectory.dir("specs/aggregated-reports")

tasks.jacocoAggregatedReport {
    reports.csv.required = true
}

val reportsSpec = copySpec {
    val dataSortRegEx = "\\bdata-sort-value=\"\\d+\"".toRegex()
    val tookRegEx = "\\b\\d+(?:\\.\\d+)?s\\b".toRegex()

    into("coverage") {
        from(tasks.jacocoAggregatedReport) { include("**/*.csv") }
    }
    into("tests") {
        from(tasks.testAggregatedReport) {
            filter {
                when {
                    it.startsWith("<a href=\"https://www.gradle.org\">") -> ""
                    else -> it
                        .replace(dataSortRegEx, "data-sort-value=\"100\"")
                        .replace(tookRegEx, "0.100s")
                }
            }
        }
    }
    includeEmptyDirs = false
}

tasks.register<Sync>("collectExpectedReports") {
    outputs.upToDateWhen { false }
    with(reportsSpec)
    into(aggregatedReportsSpecs)
}

val checkReportsTask = tasks.register<Sync>("checkAggregatedReportsContent") {
    outputs.upToDateWhen { false }
    into("expects") {
        from(aggregatedReportsSpecs)
    }
    into("actual") {
        with(reportsSpec)
    }
    into(temporaryDir)
    doLast {
        fun File.collect() = walkTopDown()
            .filter(File::isFile)
            .associateBy { it.toRelativeString(this) }

        val expected = File(temporaryDir, "expects").collect()
        val actual = File(temporaryDir, "actual").collect()
        val diff = (expected.keys + actual.keys).mapNotNull {
            val expectedLines = expected[it]?.readLines().orEmpty()
            val actualLines = actual[it]?.readLines().orEmpty()

            when (actualLines) {
                expectedLines -> null
                else -> UnifiedDiffUtils.generateUnifiedDiff(
                    "expected:${it}", "actual:${it}",
                    expectedLines,
                    DiffUtils.diff(expectedLines, actualLines),
                    3
                ).joinToString("\n")
            }
        }
        check(diff.isEmpty()) {
            diff.joinToString(
                prefix = "The generated reports are different than the expected ones:\n",
                separator = "\n\n\n"
            )
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoAggregatedCoverageVerification, checkReportsTask)
}
