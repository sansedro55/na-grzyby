package pl.nagrzyby.app.data.model

data class GeocodedPlace(
    val query: String,
    val displayName: String,
    val latitude: Double,
    val longitude: Double,
)
