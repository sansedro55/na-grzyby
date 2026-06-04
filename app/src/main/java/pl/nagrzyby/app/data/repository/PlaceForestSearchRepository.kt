package pl.nagrzyby.app.data.repository

import pl.nagrzyby.app.data.model.ForestDistrict
import pl.nagrzyby.app.data.model.GeocodedPlace

/**
 * Wyszukiwanie nadleśnictwa po nazwie miejscowości (geokodowanie + BDL w punkcie).
 */
class PlaceForestSearchRepository(
    private val geocodingRepository: GeocodingRepository = GeocodingRepository(),
    private val bdlForestRepository: BdlForestRepository,
    private val forestRepository: ForestRepository,
) {

    data class PlaceSearchResult(
        val place: GeocodedPlace,
        val districts: List<ForestDistrict>,
    )

    suspend fun findDistrictsForLocality(placeName: String): PlaceSearchResult? {
        val place = geocodingRepository.geocodeLocality(placeName) ?: return null

        val fromBdl = bdlForestRepository.fetchNear(
            latitude = place.latitude,
            longitude = place.longitude,
            radiusDegrees = PLACE_BBOX_RADIUS,
            limit = 15,
        )
        forestRepository.cacheBdlDistricts(fromBdl)

        val sorted = forestRepository.sortByDistanceFrom(
            userLatitude = place.latitude,
            userLongitude = place.longitude,
            source = fromBdl,
        ).map { district ->
            district.copy(searchViaPlace = place.query)
        }

        if (sorted.isEmpty()) return null

        return PlaceSearchResult(place = place, districts = sorted)
    }

    companion object {
        /** ~15 km — wystarczające, by trafić w granice nadleśnictwa w BDL. */
        private const val PLACE_BBOX_RADIUS = 0.12
    }
}
