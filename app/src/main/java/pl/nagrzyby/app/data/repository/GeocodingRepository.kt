package pl.nagrzyby.app.data.repository

import pl.nagrzyby.app.data.model.GeocodedPlace
import pl.nagrzyby.app.data.remote.NominatimClient
import pl.nagrzyby.app.data.remote.nominatim.NominatimApi

class GeocodingRepository(
    private val api: NominatimApi = NominatimClient.api,
) {

    suspend fun geocodeLocality(query: String): GeocodedPlace? {
        val trimmed = query.trim()
        if (trimmed.length < 2) return null

        val searchQuery = if (trimmed.contains(",")) {
            trimmed
        } else {
            "$trimmed, Polska"
        }

        val results = api.search(query = searchQuery, limit = 1)
        val first = results.firstOrNull() ?: return null
        val lat = first.lat?.toDoubleOrNull() ?: return null
        val lon = first.lon?.toDoubleOrNull() ?: return null

        return GeocodedPlace(
            query = trimmed,
            displayName = first.displayName ?: trimmed,
            latitude = lat,
            longitude = lon,
        )
    }
}
