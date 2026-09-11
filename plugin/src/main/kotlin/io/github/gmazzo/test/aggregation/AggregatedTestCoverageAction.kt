@file:OptIn(ExperimentalPathApi::class)

package io.github.gmazzo.test.aggregation

import io.github.gmazzo.test.aggregation.AggregatedTestCoverageAction.Params
import kotlin.io.path.ExperimentalPathApi
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.project.antbuilder.AntBuilderDelegate
import org.gradle.api.plugins.internal.ant.AntWorkAction
import org.gradle.api.plugins.internal.ant.AntWorkParameters
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

internal abstract class AggregatedTestCoverageAction : AntWorkAction<Params>() {

    override fun getActionName() = "aggregated-coverage"

    override fun execute(antBuilder: AntBuilderDelegate): Unit = with(antBuilder) {
        taskdef("jacocoReport", "io.github.gmazzo.test.aggregation.jacoco.GroupingReportTask")

        createNode("jacocoReport", emptyMap()) {
            createNode("structure", mapOf("name" to parameters.reportName.get())) {
                val variants = parameters.variants.get()

                when (variants.size) {
                    1 -> bindData(variants.values.single())
                    else -> for ((variantName, variant) in variants) {
                        createNode("group", mapOf("name" to variantName)) {
                            bindData(variant)
                        }
                    }
                }
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

    private fun AntBuilderDelegate.bindData(variant: TestAggregationCoverageReport.Variant) {
        createNode("classfiles", emptyMap()) {
            addFiles("resources", variant.classes.asFileTree)
        }
        createNode("sourcefiles", emptyMap()) {
            addFiles("resources", variant.sources.asFileTree)
        }
        createNode("executiondata", emptyMap()) {
            addFiles("resources", variant.coverageData.asFileTree)
        }
    }

    interface Params : AntWorkParameters {

        val reportName: Property<String>

        val variants: MapProperty<String, TestAggregationCoverageReport.Variant>

        val htmlOutputLocation: DirectoryProperty

        val xmlOutputLocation: RegularFileProperty

        val csvOutputLocation: RegularFileProperty

    }

}
