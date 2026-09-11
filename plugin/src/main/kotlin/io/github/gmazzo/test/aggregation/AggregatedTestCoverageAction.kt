@file:OptIn(ExperimentalPathApi::class)

package io.github.gmazzo.test.aggregation

import io.github.gmazzo.test.aggregation.AggregatedTestCoverageAction.Params
import java.io.File
import kotlin.io.path.ExperimentalPathApi
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.project.antbuilder.AntBuilderDelegate
import org.gradle.api.plugins.internal.ant.AntWorkAction
import org.gradle.api.plugins.internal.ant.AntWorkParameters
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

internal abstract class AggregatedTestCoverageAction : AntWorkAction<Params>() {

    override fun getActionName() = "aggregated-coverage"

    override fun execute(antBuilder: AntBuilderDelegate): Unit = with(antBuilder) {
        taskdef("jacocoReport", "org.jacoco.ant.ReportTask")

        createNode("jacocoReport", emptyMap()) {
            val coverageFiles = mutableSetOf<File>()

            createNode("structure", mapOf("name" to parameters.reportName.get())) {
                val variants = parameters.variants.get()

                when (val single = variants.singleOrNull()) {
                    null -> for (variant in variants) {
                        createNode("group", mapOf("name" to variant.displayName.get())) {
                            bindData(variant, coverageFiles)
                        }
                    }

                    else -> {
                        bindData(single, coverageFiles)
                    }
                }
            }
            createNode("executiondata", emptyMap()) {
                addFiles("resources", coverageFiles)
            }
            parameters.htmlOutputLocation.asFile.orNull?.let {
                createNode("html", mapOf("destdir" to it))
            }
            parameters.xmlOutputLocation.asFile.orNull?.let {
                createNode("xml", mapOf("destfile" to it))
            }
            parameters.csvOutputLocation.asFile.orNull?.let {
                createNode("csv", mapOf("destfile" to it))
            }
        }
    }

    private fun AntBuilderDelegate.bindData(
        variant: TestAggregationCoverageReport.Variant,
        coverageFiles: MutableSet<File>,
    ) {
        coverageFiles += variant.coverageData.asFileTree

        createNode("classfiles", emptyMap()) {
            addFiles("resources", variant.classes.asFileTree)
        }
        createNode("sourcefiles", emptyMap()) {
            addFiles("resources", variant.sources.asFileTree)
        }
    }

    interface Params : AntWorkParameters {

        val reportName: Property<String>

        val variants: SetProperty<TestAggregationCoverageReport.Variant>

        val htmlOutputLocation: DirectoryProperty

        val xmlOutputLocation: RegularFileProperty

        val csvOutputLocation: RegularFileProperty

    }

}
