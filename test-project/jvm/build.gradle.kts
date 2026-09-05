plugins {
    kotlin("jvm")
    `java-library`
    `java-test-fixtures`
    jacoco
    id("io.github.gmazzo.test.aggregation")
}

testing.suites.create<JvmTestSuite>("integrationTests")

dependencies {
    "testFixturesImplementation"(kotlin("test-junit5"))
    "integrationTestsImplementation"(testFixtures(project))
}
