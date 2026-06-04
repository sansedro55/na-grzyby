package pl.nagrzyby.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val districtId: String,
    val rainfallLast4DaysMm: Double,
    val litterMoisturePercent: Double,
    val averageTemperatureCelsius: Double,
    val dominantTreeSpecies: String?,
    val cachedAtEpochMs: Long,
)
