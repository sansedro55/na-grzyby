package pl.nagrzyby.app.data.model

/**
 * Model pod przyszłe API pogodowe i leśne (BDL + serwis pogodowy).
 * Na razie używany jako kontrakt dla ekranu szczegółów.
 */
data class ForestEnvironmentData(
    val districtId: String,
    val rainfallLast4DaysMm: Double? = null,
    val litterMoisturePercent: Double? = null,
    val averageTemperatureCelsius: Double? = null,
    val dominantTreeSpecies: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val fromCache: Boolean = false,
)

/**
 * Wynik algorytmu oceny szans na grzybobranie (kolejny etap).
 */
data class MushroomForecastVerdict(
    val scorePercent: Int? = null,
    val summary: String? = null,
    val recommendation: String? = null,
)
