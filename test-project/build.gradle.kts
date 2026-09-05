plugins {
    id("io.github.gmazzo.test.aggregation")
}

allprojects {
    plugins.withId("jvm-test-suite") {
        the<TestingExtension>().suites.withType<JvmTestSuite> {
            useKotlinTest()
        }
    }
}
