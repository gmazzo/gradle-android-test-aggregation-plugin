package io.github.gmazzo.test.aggregation

import java.io.File
import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurablePublishArtifact
import org.gradle.api.artifacts.ConfigurationVariant
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.attributes.Category.CATEGORY_ATTRIBUTE
import org.gradle.api.attributes.Category.VERIFICATION
import org.gradle.api.attributes.HasConfigurableAttributes
import org.gradle.api.attributes.LibraryElements.CLASSES
import org.gradle.api.attributes.LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.gradle.api.attributes.VerificationType.JACOCO_RESULTS
import org.gradle.api.attributes.VerificationType.MAIN_SOURCES
import org.gradle.api.attributes.VerificationType.TEST_RESULTS
import org.gradle.api.attributes.VerificationType.VERIFICATION_TYPE_ATTRIBUTE
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.artifacts.dsl.PublishArtifactNotationParser
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.reporting.ReportingExtension
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.testing.jacoco.plugins.JacocoPlugin.ANT_CONFIGURATION_NAME

public class TestAggregationBasePlugin @Inject constructor(
    private val publishArtifactNotationParser: PublishArtifactNotationParser
) : Plugin<Project> {

    public companion object {
        public const val DEFAULT_RESULTS_NAME: String = "aggregatedTestResults"
        public const val DEFAULT_COVERAGE_NAME: String = "aggregatedTestCoverage"

        public const val USAGE_AGGREGATED_TEST_RESULTS: String = "aggregated-test-results"
        public const val USAGE_AGGREGATED_TEST_COVERAGE: String = "aggregated-test-coverage"
        public const val TYPE_VARIANTS_LIST: String = "variants-list"
        public val REPORT_ATTRIBUTE: Attribute<String> =
            Attribute.of("io.github.gmazzo.test.aggregation.report", String::class.java)
        public val REPORT_VARIANT_ATTRIBUTE: Attribute<String> =
            Attribute.of("io.github.gmazzo.test.aggregation.report.variant", String::class.java)
    }

    override fun apply(target: Project): Unit = with(target) {
        apply(plugin = "reporting-base")

        val reporting = the<ReportingExtension>()
        reporting.reports {
            registerFactory(TestAggregationResultsReport::class.java) {
                createResultsReport(it, reporting.baseDirectory)
            }
            registerFactory(TestAggregationCoverageReport::class.java) {
                createCoverageReport(it, reporting.baseDirectory)
            }

            withType<AbstractTestAggregationReport<*, *>> { configure() }
        }

        tasks.register("aggregatedTestReport") {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = "Aggregates test results and coverage for all test variants"

            dependsOn(
                reporting.reports.withType<TestAggregationResultsReport>(),
                reporting.reports.withType<TestAggregationCoverageReport>(),
            )
        }

        plugins.withId("org.jetbrains.kotlin.multiplatform") {
            with(KMPSupport) { installBase() }
        }

        for (androidPluginId in listOf(
            "com.android.base",
            "com.android.kotlin.multiplatform.library"
        )) {
            plugins.withId(androidPluginId) {
                with(AndroidSupport) { installBase() }
            }
        }
    }

    private fun Project.createResultsReport(name: String, baseDirectory: DirectoryProperty) =
        objects.newInstance<DefaultTestAggregationResultsReport>(name).apply report@{

            variants.configureEach {

                aggregate
                    .convention(true)
                    .finalizeValueOnRead()

                binaryData.finalizeValueOnRead()

            }

            htmlOutputLocation
                .convention(baseDirectory.dir(name.defaultResultsDir + "/html"))
                .finalizeValueOnRead()

            junitXMLOutputLocation
                .convention(baseDirectory.dir(name.defaultResultsDir + "/junit"))
                .finalizeValueOnRead()

            reportTask =
                tasks.register<AggregatedTestResultsTask>("aggregatedTestResultsReport${name.taskSuffix}") {
                    group = LifecycleBasePlugin.VERIFICATION_GROUP
                    description = "Aggregates test results for all test variants"

                    dependsOn(filteredVariants.map { it.dependsOn })
                    variants.addAll(this@report.filteredVariants.map { it.isolated })
                    variants.addAll(this@report.aggregateFromVariants)
                    htmlOutputLocation.value(this@report.htmlOutputLocation)
                    junitXMLOutputLocation.value(this@report.junitXMLOutputLocation)
                }
        }

    private fun Project.createCoverageReport(name: String, baseDirectory: DirectoryProperty) =
        objects.newInstance<DefaultTestAggregationCoverageReport>(name).apply report@{

            variants.configureEach variant@{

                displayName
                    .convention(this@variant.name)
                    .finalizeValueOnRead()

                aggregate
                    .convention(true)
                    .finalizeValueOnRead()

                sources.finalizeValueOnRead()

                classes.finalizeValueOnRead()

                coverageData.finalizeValueOnRead()

            }

            jacocoClasspath
                .from(provider { configurations.findByName(ANT_CONFIGURATION_NAME) })
                .finalizeValueOnRead()

            afterEvaluate {
                plugins.withId("com.android.base") {
                    with(AndroidSupport) {
                        configurations.maybeCreate(ANT_CONFIGURATION_NAME).defaultDependencies {
                            add(dependencies.create(jacocoDependency))
                        }
                    }
                }
            }

            htmlOutputLocation
                .convention(baseDirectory.dir(name.defaultCoverageDir + "/html"))
                .finalizeValueOnRead()

            xmlOutputLocation
                .convention(baseDirectory.file(name.defaultCoverageDir + "/coverage.xml"))
                .finalizeValueOnRead()

            csvOutputLocation
                .convention(baseDirectory.file(name.defaultCoverageDir + "/coverage.csv"))
                .finalizeValueOnRead()

            reportTask =
                tasks.register<AggregatedTestCoverageTask>("aggregatedTestCoverageReport${name.taskSuffix}") {
                    group = LifecycleBasePlugin.VERIFICATION_GROUP
                    description = "Aggregates test coverage report for all test variants"

                    dependsOn(filteredVariants.map { it.dependsOn })
                    variants.addAll(this@report.filteredVariants.map { it.isolated })
                    variants.addAll(this@report.aggregateFromVariants)
                    jacocoClasspath.from(this@report.jacocoClasspath)
                    htmlOutputLocation.value(this@report.htmlOutputLocation)
                    xmlOutputLocation.value(this@report.xmlOutputLocation)
                    csvOutputLocation.value(this@report.csvOutputLocation)
                }
        }

    context(project: Project)
    private val TestAggregationResultsReport.Variant.isolated
        get() = project.objects.newInstance<TestAggregationResultsReport.Variant>(name).apply new@{
            this@new.binaryData.from(this@isolated.binaryData).disallowChanges()
        }

    context(project: Project)
    private val TestAggregationCoverageReport.Variant.isolated
        get() = project.objects.newInstance<TestAggregationCoverageReport.Variant>(name).apply new@{
            this@new.displayName.value(this@isolated.displayName).disallowChanges()
            this@new.sources.from(this@isolated.sources).disallowChanges()
            this@new.classes.from(this@isolated.classes).disallowChanges()
            this@new.coverageData.from(this@isolated.coverageData).disallowChanges()
        }

    context(project: Project)
    private fun AbstractTestAggregationReport<*, *>.configure() {
        val usage: Usage = objects.named(
            when (this) {
                is TestAggregationResultsReport -> USAGE_AGGREGATED_TEST_RESULTS
                is TestAggregationCoverageReport -> USAGE_AGGREGATED_TEST_COVERAGE
                else -> return
            }
        )

        aggregateFrom = project.configurations.resolvable("${this@configure.name}AggregateFrom") {
            attributes {
                attribute(CATEGORY_ATTRIBUTE, objects.named(VERIFICATION))
                attribute(USAGE_ATTRIBUTE, usage)
                attribute(REPORT_ATTRIBUTE, this@configure.name)
            }
        }.get()

        val variantsFile = project.providers.of(VariantsFileValueSource::class.java) {
            parameters {
                variantsFile.value(
                    project.layout.buildDirectory
                        .file("intermidiates/test-aggregation/${this@configure.name}/variants.txt")
                )
                variantNames.value(project.provider { this@configure.filteredVariants.names })
            }
        }

        project.configurations.consumable(this@configure.name) config@{
            attributes { allOf(aggregateFrom) }
            outgoing {
                variants.create("variants-spec") {
                    attributes {
                        allOf(this@config)
                        attribute(VERIFICATION_TYPE_ATTRIBUTE, objects.named(TYPE_VARIANTS_LIST))
                    }
                    artifact(variantsFile)
                }

                this@configure.variants.all variant@{
                    when (this@variant) {
                        is TestAggregationResultsReport.Variant -> {
                            variants.create("${this@variant.name}-binary-results") {
                                attributes {
                                    allOf(this@config)
                                    attribute(REPORT_VARIANT_ATTRIBUTE, this@variant.name)
                                    attribute(
                                        VERIFICATION_TYPE_ATTRIBUTE,
                                        objects.named(TEST_RESULTS)
                                    )
                                }
                                artifacts(this@variant.binaryData.elements) {
                                    builtBy(this@variant.dependsOn)
                                }
                            }
                        }

                        is TestAggregationCoverageReport.Variant -> {
                            variants.create("${this@variant.name}-classes") {
                                attributes {
                                    allOf(this@config)
                                    attribute(REPORT_VARIANT_ATTRIBUTE, this@variant.name)
                                    attribute(LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(CLASSES))
                                }
                                artifacts(this@variant.classes.elements) {
                                    builtBy(this@variant.dependsOn)
                                }
                            }
                            variants.create("${this@variant.name}-main-sources") {
                                attributes {
                                    allOf(this@config)
                                    attribute(REPORT_VARIANT_ATTRIBUTE, this@variant.name)
                                    attribute(
                                        VERIFICATION_TYPE_ATTRIBUTE,
                                        objects.named(MAIN_SOURCES)
                                    )
                                }
                                artifacts(this@variant.sources.elements) {
                                    builtBy(this@variant.dependsOn)
                                }
                            }
                            variants.create("${this@variant.name}-coverage-data") {
                                attributes {
                                    allOf(this@config)
                                    attribute(REPORT_VARIANT_ATTRIBUTE, this@variant.name)
                                    attribute(
                                        VERIFICATION_TYPE_ATTRIBUTE,
                                        objects.named(JACOCO_RESULTS)
                                    )
                                }
                                artifacts(this@variant.coverageData.elements) {
                                    builtBy(this@variant.dependsOn)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    context(project: Project)
    private val TestAggregationResultsReport.aggregateFromVariants
        get() = aggregateFrom().artifacts.flatMap { artifact ->
            val projectPath = artifact.projectPath

            artifact.file.readLines().map { variantName ->
                val depsBinaryData = aggregateFrom {
                    attribute(REPORT_VARIANT_ATTRIBUTE, variantName)
                    attribute(VERIFICATION_TYPE_ATTRIBUTE, project.objects.named(TEST_RESULTS))
                }.files

                project.objects.newInstance<TestAggregationResultsReport.Variant>("$projectPath:$variantName")
                    .apply {
                        aggregate.disallowChanges()
                        dependsOn.value(setOf(depsBinaryData)).disallowChanges()
                        binaryData.from(depsBinaryData).disallowChanges()
                    }
            }
        }

    context(project: Project)
    private val TestAggregationCoverageReport.aggregateFromVariants
        get() = aggregateFrom().artifacts.flatMap { artifact ->
            val projectPath = artifact.projectPath

            artifact.file.readLines().map { variantName ->
                val depsSources = aggregateFrom {
                    attribute(REPORT_VARIANT_ATTRIBUTE, variantName)
                    attribute(VERIFICATION_TYPE_ATTRIBUTE, project.objects.named(MAIN_SOURCES))
                }.files

                val depsClasses = aggregateFrom {
                    attribute(REPORT_VARIANT_ATTRIBUTE, variantName)
                    attribute(LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(CLASSES))
                }.files

                val depsCoverageData = aggregateFrom {
                    attribute(REPORT_VARIANT_ATTRIBUTE, variantName)
                    attribute(VERIFICATION_TYPE_ATTRIBUTE, project.objects.named(JACOCO_RESULTS))
                }.files

                project.objects.newInstance<TestAggregationCoverageReport.Variant>("$projectPath:$variantName")
                    .apply {
                        aggregate.disallowChanges()
                        dependsOn.value(setOf(depsSources, depsClasses, depsCoverageData))
                            .disallowChanges()
                        displayName.value("$projectPath:$variantName").disallowChanges()
                        sources.from(depsSources).disallowChanges()
                        classes.from(depsClasses).disallowChanges()
                        coverageData.from(depsCoverageData).disallowChanges()
                    }
            }
        }

    context(project: Project)
    private fun TestAggregationReport<*, *>.aggregateFrom(
        forAttrs: Action<AttributeContainer> = {
            attribute(VERIFICATION_TYPE_ATTRIBUTE, project.objects.named(TYPE_VARIANTS_LIST))
        },
    ) = aggregateFrom.incoming.artifactView {
        componentFilter { it is ProjectComponentIdentifier }
        forAttrs.execute(attributes)
    }

    @Suppress("UNCHECKED_CAST")
    private fun AttributeContainer.allOf(config: HasConfigurableAttributes<*>) {
        for (attribute in config.attributes.keySet() as Set<Attribute<Any>>) {
            attribute(attribute, config.attributes.getAttribute(attribute) as Any)
        }
    }

    private fun ConfigurationVariant.artifacts(
        provider: Provider<out Iterable<*>>,
        configure: Action<ConfigurablePublishArtifact> = {},
    ) = artifacts.addAllLater(provider.map { list ->
        list.map { publishArtifactNotationParser.parseNotation(it).also(configure::execute) }
    })

    private val String.defaultResultsDir
        get() = when (this) {
            DEFAULT_RESULTS_NAME, DEFAULT_COVERAGE_NAME -> "aggregated-test-results"
            else -> "aggregated-test/$this/results"
        }

    private val String.defaultCoverageDir
        get() = when (this) {
            DEFAULT_RESULTS_NAME, DEFAULT_COVERAGE_NAME -> "aggregated-test-coverage"
            else -> "aggregated-test/$this/coverage"
        }

    private val String.taskSuffix
        get() = when (this) {
            DEFAULT_RESULTS_NAME, DEFAULT_COVERAGE_NAME -> ""
            else -> "For${capitalized}"
        }

    private val ResolvedArtifactResult.projectPath
        get() = (id.componentIdentifier as ProjectComponentIdentifier).projectPath

    internal abstract class VariantsFileValueSource :
        ValueSource<File, VariantsFileValueSourceParams> {

        override fun obtain() =
            parameters.variantsFile.asFile.get().apply {
                parentFile.mkdirs()
                writer().use { parameters.variantNames.get().forEach(it::appendLine) }
            }

    }

    internal interface VariantsFileValueSourceParams : ValueSourceParameters {
        val variantNames: SetProperty<String>
        val variantsFile: RegularFileProperty
    }

}
