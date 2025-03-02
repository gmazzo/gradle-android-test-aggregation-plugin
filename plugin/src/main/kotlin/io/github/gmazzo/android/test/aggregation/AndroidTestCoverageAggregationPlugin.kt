@file:Suppress("UnstableApiUsage")

package io.github.gmazzo.android.test.aggregation

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.HasUnitTest
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.api.variant.Variant
import com.android.build.gradle.tasks.factory.AndroidUnitTest
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.type.ArtifactTypeDefinition
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.TestSuiteName
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.VerificationType
import org.gradle.api.file.Directory
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.RegularFile
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.kotlin.dsl.USAGE_TEST_AGGREGATION
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.namedDomainObjectSet
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import org.gradle.kotlin.dsl.the
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.MAIN_COMPILATION_NAME

abstract class AndroidTestCoverageAggregationPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply<AndroidTestBaseAggregationPlugin>()
        addRobolectricTestsSupport()

        // enables jacoco test coverage on `debug` build type by default
        android.buildTypes["debug"].enableUnitTestCoverage = true

        val jacocoVariants = objects.namedDomainObjectSet(Variant::class)

        androidComponents.onVariants { variant ->
            jacocoVariants.addAllLater(provider {
                val buildType = android.buildTypes[variant.buildType!!]
                val aggregate = (variant as? HasUnitTest)?.unitTest != null &&
                        buildType.enableUnitTestCoverage &&
                        android.shouldAggregate(variant)

                if (aggregate) listOf(variant) else emptyList()
            })
        }

        val codeCoverageExecutionData by configurations.registering {
            isCanBeConsumed = true
            isCanBeResolved = false
            isVisible = false
            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(USAGE_TEST_AGGREGATION))
                attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.VERIFICATION))
                attribute(
                    TestSuiteName.TEST_SUITE_NAME_ATTRIBUTE,
                    objects.named(SourceSet.TEST_SOURCE_SET_NAME)
                )
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

        val allVariantsSourcesForCoverageReport by tasks.registering(Sync::class) {
            destinationDir = temporaryDir
            duplicatesStrategy = DuplicatesStrategy.INCLUDE // in case of duplicated classes
            jacocoVariants.all {
                from(sources.java?.all, sources.kotlin?.all)
            }
        }

        configurations.create("codeCoverageSources") {
            isCanBeConsumed = true
            isCanBeResolved = false
            isVisible = false
            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(USAGE_TEST_AGGREGATION))
                attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.VERIFICATION))
                attribute(
                    TestSuiteName.TEST_SUITE_NAME_ATTRIBUTE,
                    objects.named(SourceSet.TEST_SOURCE_SET_NAME)
                )
                attribute(
                    VerificationType.VERIFICATION_TYPE_ATTRIBUTE,
                    objects.named(VerificationType.MAIN_SOURCES)
                )
            }
            outgoing.artifact(allVariantsSourcesForCoverageReport) {
                type = ArtifactTypeDefinition.DIRECTORY_TYPE
            }
        }

        // Jacoco does not supports multiple versions of the same class (when merging jar of different variants)
        // So we create a unified classes dir, doing the best effort keeping the first of each variant
        val allVariantsJars = objects.listProperty<RegularFile>()
        val allVariantsDirs = objects.listProperty<Directory>()
        val allVariantsClassesForCoverageReport by tasks.registering(Sync::class) {
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
                .use(allVariantsClassesForCoverageReport)
                .toGet(ScopedArtifact.CLASSES, { allVariantsJars }, { allVariantsDirs })
        }

        configurations.create("codeCoverageElements") {
            isCanBeConsumed = true
            isCanBeResolved = false
            isVisible = false
            attributes {
                attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(USAGE_TEST_AGGREGATION))
                attribute(
                    LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
                    objects.named(LibraryElements.CLASSES)
                )
            }
            outgoing.artifact(allVariantsClassesForCoverageReport) {
                type = ArtifactTypeDefinition.JVM_CLASS_DIRECTORY
            }
        }

        onKotlinJVMTargets variant@{
            val main = compilations[MAIN_COMPILATION_NAME]

            codeCoverageExecutionData.configure {
                outgoing.artifact(unitTestTaskOf(this@variant).execData) {
                    type = ArtifactTypeDefinition.BINARY_DATA_TYPE
                }
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

    private val TaskProvider<AbstractTestTask>.execData
        get() = map {
            when (it) {
                is AndroidUnitTest -> it.jacocoCoverageOutputFile
                else -> it.the<JacocoTaskExtension>().destinationFile
            }
        }

}
