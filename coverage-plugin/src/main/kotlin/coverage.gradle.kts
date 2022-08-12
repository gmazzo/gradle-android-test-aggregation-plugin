import com.android.build.api.artifact.MultipleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestExtension
import com.android.build.gradle.TestedExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.configurationcache.extensions.capitalized
import java.io.File

val TYPE_ALL_VARIANTS_CLASSES = "all-variants-classes"

check(project == rootProject) { "The coverage plugin can only be applied at root project" }

apply(plugin = "base")
apply(plugin = "jacoco-report-aggregation")

configurations.named("allCodeCoverageReportClassDirectories") {
    attributes {
        attribute(VerificationType.VERIFICATION_TYPE_ATTRIBUTE, objects.named(TYPE_ALL_VARIANTS_CLASSES))
    }
}

allprojects {

    plugins.withId("jacoco") {
        rootProject.dependencies {
            "jacocoAggregation"(project)
        }
        plugins.withId("java") {
            tasks.named("jacocoTestReport").configure {
                dependsOn("test")
            }
        }
    }

    plugins.withId("com.android.base") {
        val android = the<TestedExtension>()
        val androidComponents = extensions
            .getByName<AndroidComponentsExtension<*, *, *>>("androidComponents")

        // enables jacoco test coverage on `debug` build type by default
        android.buildTypes["debug"].enableUnitTestCoverage = true

        // support for Robolectric tests
        android.testOptions.unitTests.all {
            it.configure<JacocoTaskExtension> {
                isIncludeNoLocationClasses = true
                excludes = listOf("jdk.internal.*")
            }
        }

        android.buildTypes.configureEach {
            extensions.add(typeOf<Property<Boolean>>(), ::aggregateTestCoverage.name, objects.property())
        }
        android.productFlavors.configureEach {
            extensions.add(typeOf<Property<Boolean>>(), ::aggregateTestCoverage.name, objects.property())
        }

        @Suppress("UNCHECKED_CAST")
        val jacocoVariants = objects.namedDomainObjectSet(Variant::class)

        androidComponents.onVariants { variant ->
            val buildType = android.buildTypes[variant.buildType!!]

            if (variant.unitTest != null && buildType.enableUnitTestCoverage) {
                afterEvaluate {
                    /**
                     * `aggregateTestCoverage` applies to `BuildType`s and `Flavor`s and
                     * can take 3 possible values: `true`, `false` or `null` (missing).
                     *
                     * Because of this, we may found conflicting declarations where a
                     * `BuildType` is set to `true` but a `Flavor` to `false`.
                     * The following logic is no honor the precedence order:
                     * - If any component of the variant (buildType/flavor) says `true`, then `true`
                     * - If any component of the variant says `false` (and other says nothing `null`), then `false`
                     * - If no component says anything (`null`), then `true` (because its `BuildType` has `enableUnitTestCoverage = true`)
                     */
                    val aggregateSources = sequenceOf(buildType.aggregateTestCoverage) +
                            variant.productFlavors.asSequence()
                                .map { (_, flavor) -> android.productFlavors[flavor] }
                                .map { it.aggregateTestCoverage }

                    if (aggregateSources.shouldAggregate) {
                        jacocoVariants.add(variant)
                    }
                }
            }
        }

        configurations.create("codeCoverageExecutionData") {
            isCanBeConsumed = true
            isCanBeResolved = false
            isVisible = false
            attributes {
                attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.VERIFICATION))
                attribute(TestSuiteType.TEST_SUITE_TYPE_ATTRIBUTE, objects.named(TestSuiteType.UNIT_TEST))
                attribute(VerificationType.VERIFICATION_TYPE_ATTRIBUTE, objects.named(VerificationType.JACOCO_RESULTS))
            }
            jacocoVariants.all task@{
                val execData = tasks
                    .named("test${unitTest!!.name.capitalized()}")
                    .map { it.the<JacocoTaskExtension>().destinationFile!! }

                outgoing.artifact(execData) {
                    type = ArtifactTypeDefinition.BINARY_DATA_TYPE
                }
            }
        }

        configurations.create("codeCoverageSources") {
            isCanBeConsumed = true
            isCanBeResolved = false
            isVisible = false
            attributes {
                attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.VERIFICATION))
                attribute(TestSuiteType.TEST_SUITE_TYPE_ATTRIBUTE, objects.named(TestSuiteType.UNIT_TEST))
                attribute(VerificationType.VERIFICATION_TYPE_ATTRIBUTE, objects.named(VerificationType.MAIN_SOURCES))
            }
            jacocoVariants.all task@{
                val variant = android.variants.single { it.name == name }
                val sources = objects.setProperty(File::class)

                sources.addAll(variant.sourceSets.asSequence()
                    .flatMap { it.javaDirectories + it.kotlinDirectories }
                    .asIterable())

                outgoing.artifacts(sources) {
                    type = ArtifactTypeDefinition.DIRECTORY_TYPE
                }
            }
        }

        // Jacoco does not supports multiple versions of the same class (when merging jar of different variants)
        // So we create a unified classes dir, doing the best effort keeping the first of each variant
        val allVariantsClassesForCoverageReport by tasks.registering(Sync::class) {
            jacocoVariants.all task@{
                from(artifacts.getAll(MultipleArtifact.ALL_CLASSES_DIRS))
            }
            into(provider { temporaryDir })
            duplicatesStrategy = DuplicatesStrategy.WARN // in case of duplicated classes
            exclude(
                // same exclude logic than https://android.googlesource.com/platform/tools/base/+/studio-master-dev/build-system/gradle-core/src/main/java/com/android/build/gradle/internal/coverage/JacocoReportTask.java#377
                "**/R.class",
                "**/R$*",
                "**/Manifest.class",
                "**/Manifest$*",
                "**/BuildConfig.class",
            )
        }

        configurations.create("codeCoverageElements") {
            isCanBeConsumed = true
            isCanBeResolved = false
            isVisible = false
            attributes {
                attribute(VerificationType.VERIFICATION_TYPE_ATTRIBUTE, objects.named(TYPE_ALL_VARIANTS_CLASSES))
            }
            outgoing.artifact(allVariantsClassesForCoverageReport) {
                type = ArtifactTypeDefinition.JVM_CLASS_DIRECTORY
            }
        }

    }
}

val report = the<ReportingExtension>().reports.create<JacocoCoverageReport>("jacocoTestReport") {
    testType.set(TestSuiteType.UNIT_TEST)
}

tasks.named("check").configure {
    dependsOn(report.reportTask)
}

val BaseExtension.variants: DomainObjectSet<out BaseVariant>
    get() = when (this) {
        is AppExtension -> applicationVariants
        is LibraryExtension -> libraryVariants
        is TestExtension -> applicationVariants
        else -> error("unsupported module type: $this")
    }

val Sequence<Property<Boolean>>.shouldAggregate
    get() = mapNotNull { it.orNull }
        .reduceOrNull { acc, aggregate -> acc || aggregate } != false
