import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.ManagedVirtualDevice

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

// setups emulator for all android projects
allprojects {
    plugins.withId("com.android.base") {
        val android = extensions.getByName<CommonExtension<*, *, *, *, *>>("android")

        val pixel2 by android.testOptions.managedDevices.devices.creating(ManagedVirtualDevice::class) {
            device = "Pixel 6"
            apiLevel = 30
            systemImageSource = "aosp-atd"
        }
    }
}
