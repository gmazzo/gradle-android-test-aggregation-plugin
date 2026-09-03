@file:Suppress("UnstableApiUsage")

package io.github.gmazzo.android.test.aggregation

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.HasUnitTest
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.api.variant.Variant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.type.ArtifactTypeDefinition
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.TestSuiteName.TEST_SUITE_NAME_ATTRIBUTE
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.VerificationType
import org.gradle.api.file.Directory
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.RegularFile
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSet.TEST_SOURCE_SET_NAME
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.USAGE_TEST_AGGREGATION
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.namedDomainObjectSet
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registering
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.MAIN_COMPILATION_NAME
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

public abstract class AndroidTestCoverageAggregationPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply<AndroidTestBaseAggregationPlugin>()
        addRobolectricTestsSupport()

        // enables jacoco test coverage on `debug` build type by default
        android.buildTypes["debug"].enableUnitTestCoverage = true

        val jacocoVariants = objects.namedDomainObjectSet(Variant::class)

        androidComponents.onVariants(androidComponents.selector().all()) { variant ->
            jacocoVariants.addAllLater(provider {
                val buildType = android.buildTypes[variant.buildType!!]
                val aggregate = (variant as? HasUnitTest)?.unitTest != null &&
                    buildType.enableUnitTestCoverage &&
                    android.shouldAggregate(variant)

                if (aggregate) listOf(variant) else emptyList()
            })
        }

        val codeCoverageExecutionData = configurations.create("codeCoverageExecutionData") {
            isCanBeConsumed = true
            isCanBeResolved = false

            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(USAGE_TEST_AGGREGATION))
                attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.VERIFICATION))
                attribute(TEST_SUITE_NAME_ATTRIBUTE, objects.named(TEST_SOURCE_SET_NAME))
                attribute(
                    VerificationType.VERIFICATION_TYPE_ATTRIBUTE,
                    objects.named(VerificationType.JACOCO_RESULTS)
                )
            }
            afterEvaluate {
                jacocoVariants.all variant@{
                    outgoing.artifact(unitTestTaskOf(this@variant)!!.execData) {
                        type = ArtifactTypeDefinition.BINARY_DATA_TYPE
                    }
                }
            }
        }

        val allVariantsSourcesTask = tasks.register<Sync>("allVariantsSourcesForCoverageReport") {
            destinationDir = temporaryDir
            duplicatesStrategy = DuplicatesStrategy.INCLUDE // in case of duplicated classes
            jacocoVariants.all {
                from(listOfNotNull(sources.java?.all, sources.kotlin?.all))
            }
        }

        configurations.create("codeCoverageSources") {
            isCanBeConsumed = true
            isCanBeResolved = false

            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(USAGE_TEST_AGGREGATION))
                attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.VERIFICATION))
                attribute(TEST_SUITE_NAME_ATTRIBUTE, objects.named(TEST_SOURCE_SET_NAME))
                attribute(
                    VerificationType.VERIFICATION_TYPE_ATTRIBUTE,
                    objects.named(VerificationType.MAIN_SOURCES)
                )
            }
            outgoing.artifact(allVariantsSourcesTask) {
                type = ArtifactTypeDefinition.DIRECTORY_TYPE
            }
        }

        // Jacoco does not supports multiple versions of the same class (when merging jar of different variants)
        // So we create a unified classes dir, doing the best effort keeping the first of each variant
        val allVariantsJars = objects.listProperty<RegularFile>()
        val allVariantsDirs = objects.listProperty<Directory>()
        val allVariantsClasses = tasks.register<Sync>("allVariantsClassesForCoverageReport") {
            from(allVariantsDirs)
            into(provider { temporaryDir })
            duplicatesStrategy = DuplicatesStrategy.INCLUDE // in case of duplicated classes
            exclude(
                // same exclude logic than https://android.googlesource.com/platform/tools/base/+/studio-master-dev/build-system/gradle-core/src/main/java/com/android/build/gradle/internal/coverage/JacocoReportTask.java#377
                "**/R.class",
                "**/R$*",
                "**/Manifest.class",
                "**/Manifest$*",
                "**/BuildConfig.class",
                "**/BR.class",
            )
        }

        jacocoVariants.all task@{
            artifacts
                .forScope(ScopedArtifacts.Scope.PROJECT)
                .use(allVariantsClasses)
                .toGet(ScopedArtifact.CLASSES, { allVariantsJars }, { allVariantsDirs })
        }

        configurations.create("codeCoverageElements") {
            isCanBeConsumed = true
            isCanBeResolved = false

            attributes {
                attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(USAGE_TEST_AGGREGATION))
                attribute(
                    LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
                    objects.named(LibraryElements.CLASSES)
                )
            }
            outgoing.artifact(allVariantsClasses) {
                type = ArtifactTypeDefinition.JVM_CLASS_DIRECTORY
            }
        }

        plugins.withId("kotlin-multiplatform") {
            with(
                KMPSupport(
                    codeCoverageExecutionData,
                    allVariantsSourcesTask,
                    allVariantsClasses,
                )
            ) { configure() }
        }
    }

    private fun Project.addRobolectricTestsSupport() {
        val robolectricSupport = objects.property<Boolean>()
            .convention(true)
            .apply { finalizeValueOnRead() }

        (android as ExtensionAware).extensions
            .add("coverageRobolectricSupport", robolectricSupport)

        afterEvaluate {
            if (robolectricSupport.get()) {
                android.testOptions.unitTests.all {
                    plugins.withId("jacoco") {
                        it.configure<JacocoTaskExtension> {
                            isIncludeNoLocationClasses = true
                            excludes = listOf("jdk.internal.*")
                        }
                    }
                }
            }
        }
    }

    internal class KMPSupport(
        private val codeCoverageExecutionData: Configuration,
        private val allVariantsSourcesForCoverageReport: TaskProvider<Sync>,
        private val allVariantsClassesForCoverageReport: TaskProvider<Sync>,
    ) : AndroidTestBaseAggregationPlugin.KMPSupport() {

        override fun Project.configureTarget(target: KotlinJvmTarget) {
            val main = target.compilations[MAIN_COMPILATION_NAME]

            codeCoverageExecutionData.outgoing.artifact(unitTestTaskOf(target).execData) {
                type = ArtifactTypeDefinition.BINARY_DATA_TYPE
            }
            allVariantsSourcesForCoverageReport.configure {
                main.allKotlinSourceSets.forAll {
                    from(it.kotlin)
                }
            }
            allVariantsClassesForCoverageReport.configure {
                from(main.output.classesDirs)
            }
        }

    }

}
