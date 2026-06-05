package pl.nagrzyby.app.data.model

import pl.nagrzyby.app.data.remote.bdl.BdlSpeciesEntry
import pl.nagrzyby.app.domain.PredictedMushroom

data class ForecastDay(
    val date: String,
    val precipitationSum: Double?,
    val temperatureMean: Double?,
)

data class ForestEnvironmentData(
    val districtId: String,
    val rainfallLast4DaysMm: Double? = null,
    val litterMoisturePercent: Double? = null,
    val averageTemperatureCelsius: Double? = null,
    val dominantTreeSpecies: String? = null,
    val treeSpeciesComposition: List<BdlSpeciesEntry> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val fromCache: Boolean = false,
    val forecast: List<ForecastDay> = emptyList(),
)

/**
 * Wynik algorytmu oceny szans na grzybobranie (kolejny etap).
 */
data class MushroomForecastVerdict(
    val scorePercent: Int? = null,
    val summary: String? = null,
    val recommendation: String? = null,
    val predictedMushrooms: List<PredictedMushroom> = emptyList(),
)
