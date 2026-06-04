package pl.nagrzyby.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import pl.nagrzyby.app.data.model.ForestDistrict

@Entity(tableName = "districts")
data class DistrictEntity(
    @PrimaryKey val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val region: String,
    val fromBdl: Boolean,
    val dominantTreeSpecies: String? = null,
) {
    fun toForestDistrict(distanceKm: Double? = null) = ForestDistrict(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        region = region,
        distanceKm = distanceKm,
    )
}

fun ForestDistrict.toEntity(fromBdl: Boolean, dominantTreeSpecies: String? = null) = DistrictEntity(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude,
    region = region,
    fromBdl = fromBdl,
    dominantTreeSpecies = dominantTreeSpecies,
)
