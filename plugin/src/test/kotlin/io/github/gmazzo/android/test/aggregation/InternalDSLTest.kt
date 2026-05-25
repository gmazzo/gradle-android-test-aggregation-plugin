package io.github.gmazzo.android.test.aggregation

import com.android.build.api.dsl.BuildType
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.Variant
import com.android.build.gradle.internal.dsl.ProductFlavor
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.kotlin.dsl.aggregateTestCoverage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InternalDSLTest {

    fun arguments() = listOf(
        arrayOf(true, true, emptyList<Boolean>()),
        arrayOf(true, null, emptyList<Boolean>()),
        arrayOf(false, false, emptyList<Boolean>()),

        arrayOf(true, true, listOf(true)),
        arrayOf(true, null, listOf(null)),
        arrayOf(false, false, listOf(false)),

        arrayOf(true, true, listOf(true, null)),
        arrayOf(false, true, listOf(true, false)),
        arrayOf(false, false, listOf(true, true)),
    )

    @ParameterizedTest(name = "buildType={1}, flavors={2}, expected={0}")
    @MethodSource("arguments")
    fun testShouldAggregate(
        expectedResult: Boolean,
        buildTypeValue: Boolean?,
        productFlavorValues: List<Boolean?>
    ) = mockkStatic(BuildType::aggregateTestCoverage) {

        val buildTypes: NamedDomainObjectContainer<BuildType> = mockk {
            every { getByName("aBuildType") } returns mockk {
                every { aggregateTestCoverage } returns mockk {
                    every { orNull } returns buildTypeValue
                }
            }
            every { size } returns 1
        }

        val productFlavors: NamedDomainObjectContainer<ProductFlavor> = mockk {
            productFlavorValues.forEachIndexed { i, it ->
                every { getByName("flavor$i") } returns mockk {
                    every { aggregateTestCoverage } returns mockk {
                        every { orNull } returns it
                    }
                }
            }
            every { size } returns productFlavorValues.size
        }

        val extension: CommonExtension = mockk extension@{
            every { this@extension.buildTypes } returns buildTypes
            every { this@extension.productFlavors } returns productFlavors
        }

        val variant: Variant = mockk variant@{
            every { this@variant.buildType } returns "aBuildType"
            every { this@variant.productFlavors } returns List(productFlavors.size) { i -> "dimension$i" to "flavor$i" }
        }

        val actualResult = with(extension) { shouldAggregate(variant) }

        assertEquals(expectedResult, actualResult)
    }

}
