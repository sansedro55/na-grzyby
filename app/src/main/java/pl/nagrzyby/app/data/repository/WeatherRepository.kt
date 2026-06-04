package pl.nagrzyby.app.data.repository

import pl.nagrzyby.app.data.local.WeatherCacheDao
import pl.nagrzyby.app.data.local.WeatherCacheEntity
import pl.nagrzyby.app.data.model.ForestEnvironmentData
import pl.nagrzyby.app.data.remote.OpenMeteoClient
import pl.nagrzyby.app.data.remote.openmeteo.HourlyUnits
import pl.nagrzyby.app.data.remote.openmeteo.OpenMeteoApi

class WeatherRepository(
    private val weatherCacheDao: WeatherCacheDao,
    private val forestRepository: ForestRepository,
    private val api: OpenMeteoApi = OpenMeteoClient.api,
) {

    suspend fun fetchEnvironmentData(
        districtId: String,
        latitude: Double,
        longitude: Double,
        forceRefresh: Boolean = false,
    ): ForestEnvironmentData {
        if (!forceRefresh) {
            getValidCache(districtId)?.let { return it }
        }

        val response = api.getForecast(latitude = latitude, longitude = longitude)
        val daily = response.daily
            ?: throw WeatherDataException("Brak danych dziennych w odpowiedzi Open-Meteo")

        val precipitation = daily.precipitationSum.orEmpty()
        val temperatures = daily.temperature2mMean.orEmpty()
        val pastDayCount = minOf(PAST_DAYS, precipitation.size, temperatures.size)

        if (pastDayCount == 0) {
            throw WeatherDataException("Niekompletne dane pogodowe dla ostatnich dni")
        }

        val rainfallSum = precipitation.take(pastDayCount).mapNotNull { it }.sum()
        val avgTemp = temperatures.take(pastDayCount).mapNotNull { it }.average().takeIf { !it.isNaN() }
        val litterMoisture = estimateLitterMoisturePercent(response.hourly)
        val treeSpecies = forestRepository.getDominantTreeSpeciesForDistrict(districtId)

        val data = ForestEnvironmentData(
            districtId = districtId,
            rainfallLast4DaysMm = rainfallSum,
            litterMoisturePercent = litterMoisture,
            averageTemperatureCelsius = avgTemp,
            dominantTreeSpecies = treeSpecies,
            isLoading = false,
            errorMessage = null,
        )

        cache(data)
        return data
    }

    private suspend fun getValidCache(districtId: String): ForestEnvironmentData? {
        val cached = weatherCacheDao.get(districtId) ?: return null
        val ageMs = System.currentTimeMillis() - cached.cachedAtEpochMs
        if (ageMs > CACHE_TTL_MS) {
            weatherCacheDao.delete(districtId)
            return null
        }
        return ForestEnvironmentData(
            districtId = districtId,
            rainfallLast4DaysMm = cached.rainfallLast4DaysMm,
            litterMoisturePercent = cached.litterMoisturePercent,
            averageTemperatureCelsius = cached.averageTemperatureCelsius,
            dominantTreeSpecies = cached.dominantTreeSpecies,
            isLoading = false,
            errorMessage = null,
            fromCache = true,
        )
    }

    private suspend fun cache(data: ForestEnvironmentData) {
        val rain = data.rainfallLast4DaysMm ?: return
        val moisture = data.litterMoisturePercent ?: return
        val temp = data.averageTemperatureCelsius ?: return
        weatherCacheDao.upsert(
            WeatherCacheEntity(
                districtId = data.districtId,
                rainfallLast4DaysMm = rain,
                litterMoisturePercent = moisture,
                averageTemperatureCelsius = temp,
                dominantTreeSpecies = data.dominantTreeSpecies,
                cachedAtEpochMs = System.currentTimeMillis(),
            ),
        )
    }

    private fun estimateLitterMoisturePercent(hourly: HourlyUnits?): Double? {
        hourly ?: return null

        val soilValues = hourly.soilMoisture0To7cm
            ?.takeLast(HOURLY_WINDOW)
            ?.mapNotNull { it }
            .orEmpty()

        if (soilValues.isNotEmpty()) {
            return (soilValues.average() * 100).coerceIn(0.0, 100.0)
        }

        val humidityValues = hourly.relativeHumidity2m
            ?.takeLast(HOURLY_WINDOW)
            ?.mapNotNull { it }
            .orEmpty()

        return humidityValues.average().takeIf { !it.isNaN() }
    }

    companion object {
        private const val PAST_DAYS = 4
        private const val HOURLY_WINDOW = 96
        private const val CACHE_TTL_MS = 30 * 60 * 1000L
    }
}

class WeatherDataException(message: String) : Exception(message)
