package pl.nagrzyby.app.data.repository

import pl.nagrzyby.app.data.local.DistrictDao
import pl.nagrzyby.app.data.local.toEntity
import pl.nagrzyby.app.data.model.ForestDistrict
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Nadleśnictwa: lokalny seed + cache Room + uzupełnienia z API BDL.
 */
class ForestRepository(
    private val districtDao: DistrictDao,
) {

    private val bootstrapDistricts: List<ForestDistrict> = listOf(
        ForestDistrict("miradz", "Nadleśnictwo Miradz", 54.102, 18.938, "RDLP Gdańsk"),
        ForestDistrict("gniewkowo", "Nadleśnictwo Gniewkowo", 52.894, 18.403, "RDLP Toruń"),
        ForestDistrict("solec", "Nadleśnictwo Solec Kujawski", 53.082, 18.255, "RDLP Toruń"),
        ForestDistrict("bialowieza", "Nadleśnictwo Białowieża", 52.765, 23.872, "RDLP Białystok"),
        ForestDistrict("gizycko", "Nadleśnictwo Giżycko", 54.038, 21.764, "RDLP Olsztyn"),
        ForestDistrict("pisz", "Nadleśnictwo Pisz", 53.628, 21.812, "RDLP Olsztyn"),
        ForestDistrict("krakow", "Nadleśnictwo Kraków", 50.061, 19.937, "RDLP Kraków"),
        ForestDistrict("wroclaw", "Nadleśnictwo Wrocław", 51.107, 17.038, "RDLP Wrocław"),
        ForestDistrict("katowice", "Nadleśnictwo Katowice", 50.264, 19.023, "RDLP Katowice"),
        ForestDistrict("szczecinek", "Nadleśnictwo Szczecinek", 53.708, 16.699, "RDLP Szczecinek"),
        ForestDistrict("walbrzych", "Nadleśnictwo Wałbrzych", 50.771, 16.284, "RDLP Wrocław"),
        ForestDistrict("olsztyn", "Nadleśnictwo Olsztyn", 53.778, 20.480, "RDLP Olsztyn"),
        ForestDistrict("elblag", "Nadleśnictwo Elbląg", 54.153, 19.404, "RDLP Gdańsk"),
        ForestDistrict("konin", "Nadleśnictwo Konin", 52.223, 18.251, "RDLP Poznań"),
        ForestDistrict("szprotawa", "Nadleśnictwo Szprotawa", 51.565, 15.537, "RDLP Zielona Góra"),
    )

    suspend fun ensureSeeded() {
        if (districtDao.getAll().isNotEmpty()) return
        districtDao.upsertAll(
            bootstrapDistricts.map { district ->
                district.toEntity(
                    fromBdl = false,
                    dominantTreeSpecies = getDominantTreeSpecies(district.id),
                )
            },
        )
    }

    suspend fun getDistrictById(id: String): ForestDistrict? {
        ensureSeeded()
        return districtDao.getById(id)?.toForestDistrict()
            ?: bootstrapDistricts.find { it.id == id }
    }

    suspend fun getDominantTreeSpeciesForDistrict(districtId: String): String {
        ensureSeeded()
        districtDao.getById(districtId)?.dominantTreeSpecies?.let { return it }
        return getDominantTreeSpecies(districtId)
    }

    suspend fun searchLocal(query: String): List<ForestDistrict> {
        ensureSeeded()
        if (query.isBlank()) {
            return districtDao.getAll().map { it.toForestDistrict() }
        }
        val fromDb = districtDao.search(query).map { it.toForestDistrict() }
        if (fromDb.isNotEmpty()) return fromDb
        return filterBootstrap(query)
    }

    suspend fun cacheBdlDistricts(districts: List<ForestDistrict>) {
        if (districts.isEmpty()) return
        districtDao.upsertAll(
            districts.map { district ->
                val inspectorate = district.name.removePrefix("Nadleśnictwo ").trim()
                district.toEntity(
                    fromBdl = district.id.startsWith("bdl_"),
                    dominantTreeSpecies = getDominantTreeSpeciesForInspectorateName(inspectorate),
                )
            },
        )
    }

    fun mergeDistrictLists(vararg lists: List<ForestDistrict>): List<ForestDistrict> {
        val byKey = linkedMapOf<String, ForestDistrict>()
        for (district in lists.flatMap { it }) {
            val key = district.name.removePrefix("Nadleśnictwo ").trim().lowercase()
            val existing = byKey[key]
            if (existing == null || district.searchViaPlace != null) {
                byKey[key] = district
            }
        }
        return byKey.values.sortedBy { it.name }
    }

    fun getDominantTreeSpecies(districtId: String): String =
        dominantTreeSpecies[districtId] ?: "Las mieszany (dane BDL)"

    fun getDominantTreeSpeciesForInspectorateName(inspectorateName: String): String {
        val legacyId = inspectorateToLegacyId[inspectorateName]
        return if (legacyId != null) getDominantTreeSpecies(legacyId) else "Las mieszany (wydzielenia BDL)"
    }

    fun searchByName(query: String): List<ForestDistrict> = filterBootstrap(query)

    fun sortByDistanceFrom(
        userLatitude: Double,
        userLongitude: Double,
        source: List<ForestDistrict>,
    ): List<ForestDistrict> =
        source
            .map { district ->
                district.copy(
                    distanceKm = haversineKm(
                        userLatitude,
                        userLongitude,
                        district.latitude,
                        district.longitude,
                    ),
                )
            }
            .sortedBy { it.distanceKm }

    private fun filterBootstrap(query: String): List<ForestDistrict> {
        if (query.isBlank()) return bootstrapDistricts
        val normalized = query.trim().lowercase()
        return bootstrapDistricts.filter { district ->
            district.name.lowercase().contains(normalized) ||
                district.region.lowercase().contains(normalized)
        }
    }

    private val dominantTreeSpecies: Map<String, String> = mapOf(
        "miradz" to "Bory sosnowe i mieszane",
        "gniewkowo" to "Bory sosnowe z brzozą i dębem",
        "solec" to "Lasy liściaste (dąb, grab)",
        "bialowieza" to "Puszcza: dąb, świerk, lipa",
        "gizycko" to "Bory sosnowe i świerkowe",
        "pisz" to "Bory mieszane (sosna, świerk)",
        "krakow" to "Lasy podgórskie: buk, jodła, świerk",
        "wroclaw" to "Lasy nizinne: dąb, brzoza",
        "katowice" to "Lasy śródmiejskie i bory sosnowe",
        "szczecinek" to "Bory sosnowe",
        "walbrzych" to "Lasy górskie: świerk, buk",
        "olsztyn" to "Bory mieszane mazurskie",
        "elblag" to "Lasy nadmorskie: sosna, dąb",
        "konin" to "Bory sosnowe śródląskowe",
        "szprotawa" to "Bory sosnowe i świeże lasy liściaste",
    )

    private val inspectorateToLegacyId: Map<String, String> = mapOf(
        "Miradz" to "miradz",
        "Gniewkowo" to "gniewkowo",
        "Solec Kujawski" to "solec",
        "Białowieża" to "bialowieza",
        "Giżycko" to "gizycko",
        "Pisz" to "pisz",
        "Kraków" to "krakow",
        "Wrocław" to "wroclaw",
        "Katowice" to "katowice",
        "Szczecinek" to "szczecinek",
        "Wałbrzych" to "walbrzych",
        "Olsztyn" to "olsztyn",
        "Elbląg" to "elblag",
        "Konin" to "konin",
        "Szprotawa" to "szprotawa",
    )

    companion object {
        private const val EARTH_RADIUS_KM = 6371.0

        fun haversineKm(
            lat1: Double,
            lon1: Double,
            lat2: Double,
            lon2: Double,
        ): Double {
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return EARTH_RADIUS_KM * c
        }
    }
}
