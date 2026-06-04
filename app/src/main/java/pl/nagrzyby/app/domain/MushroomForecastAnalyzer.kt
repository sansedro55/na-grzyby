package pl.nagrzyby.app.domain

import pl.nagrzyby.app.data.model.ForestEnvironmentData
import pl.nagrzyby.app.data.model.MushroomForecastVerdict
import java.util.Calendar

/**
 * Algorytm v1 — pogoda, wilgotność, drzewostan i sezon grzybobrania w PL.
 */
object MushroomForecastAnalyzer {

    fun analyze(data: ForestEnvironmentData): MushroomForecastVerdict {
        val rain = data.rainfallLast4DaysMm
        val temp = data.averageTemperatureCelsius
        val moisture = data.litterMoisturePercent

        if (rain == null || temp == null || moisture == null) {
            return MushroomForecastVerdict(
                scorePercent = null,
                summary = "Brak pełnych danych pogodowych",
                recommendation = "Odśwież dane i spróbuj ponownie.",
            )
        }

        var score = 45

        score += when {
            rain in 12.0..40.0 -> 28
            rain in 5.0..12.0 -> 12
            rain in 40.0..60.0 -> 5
            rain < 5.0 -> -18
            else -> -8
        }

        score += when {
            temp in 11.0..18.0 -> 22
            temp in 8.0..11.0 || temp in 18.0..21.0 -> 8
            temp < 5.0 || temp > 24.0 -> -15
            else -> -5
        }

        score += when {
            moisture >= 75.0 -> 18
            moisture >= 55.0 -> 10
            moisture >= 40.0 -> 0
            else -> -12
        }

        val treeBonus = when {
            data.dominantTreeSpecies?.contains("dąb", ignoreCase = true) == true -> 5
            data.dominantTreeSpecies?.contains("buk", ignoreCase = true) == true -> 4
            data.dominantTreeSpecies?.contains("sosna", ignoreCase = true) == true -> 2
            else -> 0
        }
        score += treeBonus
        score += seasonBonus(Calendar.getInstance().get(Calendar.MONTH) + 1)

        val finalScore = score.coerceIn(0, 100)

        val summary = when {
            finalScore >= 75 -> "Bardzo dobre warunki na grzybobranie"
            finalScore >= 55 -> "Umiarkowanie dobre warunki"
            finalScore >= 35 -> "Słabe warunki — warto poczekać na deszcz"
            else -> "Niekorzystne warunki"
        }

        val recommendation = buildRecommendation(rain, temp, moisture, finalScore)

        return MushroomForecastVerdict(
            scorePercent = finalScore,
            summary = summary,
            recommendation = recommendation,
        )
    }

    /** Miesiąc 1–12; szczyt sezonu grzybowego w PL: lato–jesień. */
    private fun seasonBonus(month: Int): Int = when (month) {
        in 8..10 -> 12
        in 6..7, 11 -> 6
        5, 12 -> 0
        in 1..4 -> -10
        else -> 0
    }

    private fun buildRecommendation(
        rain: Double,
        temp: Double,
        moisture: Double,
        score: Int,
    ): String = buildString {
        if (rain < 8.0) append("Za mało opadów w ostatnich dniach. ")
        if (rain > 55.0) append("Duże opady — sprawdź dostępność leśnictw. ")
        if (temp < 8.0) append("Niska temperatura ogranicza wzrost owocników. ")
        if (temp > 20.0) append("Wysoka temperatura — szukaj wilgotnych zacienionych miejsc. ")
        if (moisture < 50.0) append("Niska wilgotność podłoża. ")
        if (isEmpty()) {
            when {
                score >= 70 -> append("Wybierz znane miejsca w wybranym nadleśnictwie i sprawdź lokalne regulaminy.")
                else -> append("Monitoruj pogodę przez kolejne 2–3 dni.")
            }
        }
    }.trim()
}
