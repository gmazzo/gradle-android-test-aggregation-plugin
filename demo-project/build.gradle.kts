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
            projects.demoProject.app,
            projects.demoProject.domain,
            projects.demoProject.login,
            projects.demoProject.kmp,
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
    val tookRegEx = "\\b\\d+(?:\\.\\d+)?s\\b".toRegex()

    from(tasks.jacocoAggregatedReport) { include("**/*.csv") }
    from(tasks.testAggregatedReport) {
        into("tests")
        filter {
            when {
                it.startsWith("<a href=\"http://www.gradle.org\">") -> ""
                else -> it.replace(tookRegEx, "0.100s")
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

val checkAggregatedReportsContent by tasks.registering(Sync::class) {
    outputs.upToDateWhen { false }
    with(reportsSpec) { into("actual") }
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
                )
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
    dependsOn(tasks.jacocoAggregatedCoverageVerification, checkAggregatedReportsContent)
}
