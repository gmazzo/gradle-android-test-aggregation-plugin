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
