plugins {
    java
    id("io.github.gmazzo.test.aggregation")
}

dependencies {
    testImplementation(testFixtures(projects.testProject.jvm))
}
