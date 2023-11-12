@file:Suppress("PrivateAPI", "ObjectPropertyName")

/**
 * Helper to access AGP internal API as the required wiring to get coverage data is not public
 */

package io.github.gmazzo.android.test.aggregation

import com.android.build.api.artifact.Artifact
import com.android.build.api.artifact.Artifacts
import com.android.build.api.artifact.impl.ArtifactsImpl
import com.android.build.api.artifact.impl.InternalScopedArtifact
import com.android.build.api.artifact.impl.ScopedArtifactsImpl
import com.android.build.api.component.analytics.AnalyticsEnabledArtifacts
import com.android.build.api.component.analytics.AnalyticsEnabledScopedArtifacts
import com.android.build.api.variant.ScopedArtifacts
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import kotlin.reflect.full.functions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.typeOf

internal fun <FILE_TYPE : FileSystemLocation> Artifacts.get(
    type: Artifact.Single<FILE_TYPE>
): Provider<FILE_TYPE> = when (this) {
    is ArtifactsImpl -> get(type)
    is AnalyticsEnabledArtifacts -> delegate.get(type)
    else -> error("Unknown `Artifacts` type: $this")
}

internal fun ScopedArtifacts.get(type: InternalScopedArtifact): ConfigurableFileCollection =
    when (this) {
        is ScopedArtifactsImpl -> _getScopedArtifactsContainer(type).finalScopedContent
        is AnalyticsEnabledScopedArtifacts -> _delegate.get(type)
        else -> error("Unknown `ScopedArtifacts` type: $this")
    }

private fun ScopedArtifactsImpl._getScopedArtifactsContainer(type: InternalScopedArtifact) =
    ScopedArtifactsImpl::class
        .functions
        .firstOrNull {
            it.name == "getScopedArtifactsContainer" &&
                    it.parameters.size == 2 &&
                    it.parameters[0].type == typeOf<ScopedArtifactsImpl>() &&
                    it.parameters[1].type == typeOf<InternalScopedArtifact>()
        }
        ?.apply { isAccessible = true }
        ?.call(this, type) as? ScopedArtifactsImpl.ScopedArtifactsContainer
        ?: error("Can't get `InternalScopedArtifact` type `$type`")

private val AnalyticsEnabledScopedArtifacts._delegate
    get() = AnalyticsEnabledScopedArtifacts::class
        .memberProperties
        .firstOrNull { it.name == "delegate" }
        ?.apply { isAccessible = true }
        ?.get(this) as ScopedArtifacts?
        ?: error("Can't get `AnalyticsEnabledScopedArtifacts`'s delegate")
