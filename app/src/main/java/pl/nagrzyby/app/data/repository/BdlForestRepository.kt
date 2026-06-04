package pl.nagrzyby.app.data.repository

import pl.nagrzyby.app.data.model.ForestDistrict
import pl.nagrzyby.app.data.remote.BdlClient
import pl.nagrzyby.app.data.remote.bdl.BdlFeature
import pl.nagrzyby.app.data.remote.bdl.BdlOgcApi
import pl.nagrzyby.app.data.remote.bdl.GeometryCentroid
import pl.nagrzyby.app.data.remote.bdl.RdlpNames

class BdlForestRepository(
    private val forestRepository: ForestRepository,
    private val api: BdlOgcApi = BdlClient.api,
) {

    suspend fun fetchByName(query: String, limit: Int = 20): List<ForestDistrict> {
        if (query.length < 2) return emptyList()
        val escaped = query.trim().replace("'", "''")
        val filter = "inspectorate_name ILIKE '%$escaped%'"
        return fetchAndMap(limit = limit, filter = filter)
    }

    suspend fun fetchNear(
        latitude: Double,
        longitude: Double,
        radiusDegrees: Double = 0.45,
        limit: Int = 30,
    ): List<ForestDistrict> {
        val minLon = longitude - radiusDegrees
        val minLat = latitude - radiusDegrees
        val maxLon = longitude + radiusDegrees
        val maxLat = latitude + radiusDegrees
        val bbox = "$minLon,$minLat,$maxLon,$maxLat"
        return fetchAndMap(limit = limit, bbox = bbox)
    }

    suspend fun fetchAndCacheByName(query: String): List<ForestDistrict> {
        val remote = fetchByName(query)
        forestRepository.cacheBdlDistricts(remote)
        return remote
    }

    suspend fun fetchAndCacheNear(latitude: Double, longitude: Double): List<ForestDistrict> {
        val remote = fetchNear(latitude, longitude)
        forestRepository.cacheBdlDistricts(remote)
        return remote
    }

    private suspend fun fetchAndMap(
        limit: Int,
        bbox: String? = null,
        filter: String? = null,
    ): List<ForestDistrict> {
        val response = api.getDistrictItems(limit = limit, bbox = bbox, filter = filter)
        return response.features.orEmpty().mapNotNull { featureToDistrict(it) }
    }

    private fun featureToDistrict(feature: BdlFeature): ForestDistrict? {
        val props = feature.properties ?: return null
        val rawName = props.inspectorateName?.trim().orEmpty()
        if (rawName.isEmpty()) return null

        val coords = GeometryCentroid.fromGeometry(feature.geometry) ?: return null
        val (lat, lon) = coords
        val featureId = feature.id ?: rawName.hashCode()

        return ForestDistrict(
            id = "bdl_$featureId",
            name = "Nadleśnictwo $rawName",
            latitude = lat,
            longitude = lon,
            region = RdlpNames.fromRegionCode(props.regionCode),
        )
    }
}
