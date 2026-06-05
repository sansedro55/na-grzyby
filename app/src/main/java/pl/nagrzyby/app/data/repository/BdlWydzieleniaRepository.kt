package pl.nagrzyby.app.data.repository

import pl.nagrzyby.app.data.model.ForestDistrict
import pl.nagrzyby.app.data.remote.BdlClient
import pl.nagrzyby.app.data.remote.bdl.BdlOgcApi
import pl.nagrzyby.app.data.remote.bdl.BdlSpeciesEntry
import pl.nagrzyby.app.data.remote.bdl.BdlSpecies

/**
 * Pobiera wydzielenia (dane drzewostanu) z API BDL OGC.
 * Agreguje gatunki drzew (% udziału powierzchni) dla nadleśnictwa.
 */
class BdlWydzieleniaRepository(
    private val api: BdlOgcApi = BdlClient.api,
) {

    suspend fun fetchSpeciesComposition(district: ForestDistrict): List<BdlSpeciesEntry> {
        val collectionName = collectionIdForRegion(district.region) ?: return emptyList()
        val bbox = bboxAround(district.latitude, district.longitude)

        val response = try {
            api.getWydzieleniaItems(
                collectionName = collectionName,
                bbox = bbox,
                limit = 200,
            )
        } catch (_: Exception) {
            return emptyList()
        }

        val features = response.features.orEmpty()
        if (features.isEmpty()) return emptyList()

        val bySpecies = mutableMapOf<String, Double>()
        for (feature in features) {
            val props = feature.properties ?: continue
            val code = props.speciesCode?.trim()?.uppercase() ?: continue
            val area = props.subAreaHa ?: continue
            if (code.isBlank() || area <= 0.0) continue
            bySpecies[code] = (bySpecies[code] ?: 0.0) + area
        }

        return bySpecies.map { (code, area) ->
            BdlSpeciesEntry(speciesCode = code, areaHa = area)
        }
    }

    suspend fun fetchSpeciesSummary(district: ForestDistrict): String {
        val composition = fetchSpeciesComposition(district)
        if (composition.isEmpty()) return ""
        return BdlSpecies.describeComposition(composition)
    }

    private fun collectionIdForRegion(region: String): String? {
        val rdlp = region.removePrefix("RDLP ").trim()
        if (rdlp.isBlank()) return null
        return "RDLP_${normalize(rdlp)}_wydzielenia"
    }

    private fun bboxAround(lat: Double, lon: Double, radius: Double = 0.12): String {
        val minLon = lon - radius
        val minLat = lat - radius
        val maxLon = lon + radius
        val maxLat = lat + radius
        return "$minLon,$minLat,$maxLon,$maxLat"
    }

    private fun normalize(name: String): String {
        return name
            .replace("Ł", "L")
            .replace("ł", "l")
            .replace("ń", "n")
            .replace("ó", "o")
            .replace("ą", "a")
            .replace("ś", "s")
            .replace("ć", "c")
            .replace("ź", "z")
            .replace("ż", "z")
            .replace(" ", "_")
    }
}
