package pl.nagrzyby.app.data.repository

import pl.nagrzyby.app.data.remote.BdlClient
import pl.nagrzyby.app.data.remote.bdl.BdlLesnictwoFeature
import pl.nagrzyby.app.data.remote.bdl.BdlOgcApi

/**
 * Pobiera leśnictwa z BDL w okolicy nadleśnictwa (bbox).
 */
class BdlLesnictwaRepository(
    private val api: BdlOgcApi = BdlClient.api,
) {

    suspend fun fetchForestSummaryNear(
        latitude: Double,
        longitude: Double,
        inspectorateName: String?,
        radiusDegrees: Double = 0.25,
    ): String? {
        val minLon = longitude - radiusDegrees
        val minLat = latitude - radiusDegrees
        val maxLon = longitude + radiusDegrees
        val maxLat = latitude + radiusDegrees
        val bbox = "$minLon,$minLat,$maxLon,$maxLat"

        val response = try {
            api.getLesnictwaItems(bbox = bbox, limit = 20)
        } catch (_: Exception) {
            return null
        }

        val features = response.features.orEmpty()
        if (features.isEmpty()) return null

        val filtered = if (!inspectorateName.isNullOrBlank()) {
            val short = inspectorateName.removePrefix("Nadleśnictwo ").trim()
            features.filter { feature ->
                val insp = feature.properties?.inspectorateName
                insp == null || insp.contains(short, ignoreCase = true)
            }.ifEmpty { features }
        } else {
            features
        }

        val rangeNames = filtered
            .mapNotNull { it.properties?.forestRangeName?.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(5)

        val habitats = filtered
            .mapNotNull { habitatLabel(it) }
            .distinct()
            .take(4)

        return buildString {
            if (rangeNames.isNotEmpty()) {
                append("Leśnictwa w okolicy: ")
                append(rangeNames.joinToString(", "))
            }
            if (habitats.isNotEmpty()) {
                if (isNotEmpty()) append("\n")
                append("Siedliska / gatunki (BDL): ")
                append(habitats.joinToString(", "))
            }
        }.takeIf { it.isNotBlank() }
    }

    private fun habitatLabel(feature: BdlLesnictwoFeature): String? {
        val props = feature.properties ?: return null
        return props.dominantSpecies?.trim()?.takeIf { it.isNotEmpty() }
            ?: props.forestHabitat?.trim()?.takeIf { it.isNotEmpty() }
    }
}
