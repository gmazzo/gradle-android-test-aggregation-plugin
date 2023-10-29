plugins {
    base
    id("io.github.gmazzo.test.aggregation.results")
    id("io.github.gmazzo.test.aggregation.coverage")
}

testAggregation {
    modules {
        include(projects.demoProject.app, projects.demoProject.domain, projects.demoProject.login)
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

tasks.check {
    dependsOn(tasks.jacocoAggregatedCoverageVerification)
}
