package pl.nagrzyby.app.domain

import pl.nagrzyby.app.data.remote.bdl.BdlSpeciesEntry
import java.util.Calendar

data class PredictedMushroom(
    val species: MushroomSpecies,
    val likelihoodPercent: Int,
    val matchReasons: List<String>,
)

object MushroomPredictor {

    fun predict(
        speciesComposition: List<BdlSpeciesEntry>,
        temperature: Double,
        moisturePercent: Double,
        month: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    ): List<PredictedMushroom> {
        val presentCodes = if (speciesComposition.isEmpty()) {
            emptyMap()
        } else {
            val totalArea = speciesComposition.sumOf { it.areaHa }.coerceAtLeast(0.01)
            speciesComposition.associate { (code, area) -> code to area / totalArea }
        }

        return MushroomDatabase.allSpecies.map { species ->
            val reasons = mutableListOf<String>()
            var score = 0

            score += scoreTreeMatch(species, presentCodes, reasons)
            score += scoreTemperature(species, temperature, reasons)
            score += scoreSeason(species, month, reasons)
            score += scoreMoisture(species, moisturePercent, reasons)

            PredictedMushroom(
                species = species,
                likelihoodPercent = score.coerceIn(0, 100),
                matchReasons = reasons,
            )
        }
            .filter { it.likelihoodPercent >= 30 }
            .sortedByDescending { it.likelihoodPercent }
    }

    private fun scoreTreeMatch(
        species: MushroomSpecies,
        presentWithShare: Map<String, Double>,
        reasons: MutableList<String>,
    ): Int {
        val matchShare = presentWithShare
            .filter { (code, _) -> code in species.preferredTreeCodes }
            .values
            .sum()
        if (matchShare <= 0.0) return 0
        val sharePct = (matchShare * 100).toInt()
        reasons.add("Drzewa: +${sharePct}pkt")
        return (matchShare * 40).toInt().coerceIn(0, 40)
    }

    private fun scoreTemperature(
        species: MushroomSpecies,
        temp: Double,
        reasons: MutableList<String>,
    ): Int {
        val mid = (species.tempMin + species.tempMax) / 2.0
        val range = (species.tempMax - species.tempMin) / 2.0
        if (range <= 0.0) return 0
        val distance = kotlin.math.abs(temp - mid)
        val ratio = (1.0 - (distance / (range * 1.5))).coerceIn(0.0, 1.0)
        val pts = (ratio * 30).toInt()
        if (pts > 0) reasons.add("Temperatura: +${pts}pkt")
        return pts
    }

    private fun scoreSeason(
        species: MushroomSpecies,
        month: Int,
        reasons: MutableList<String>,
    ): Int {
        val inSeason = month in species.seasonStartMonth..species.seasonEndMonth
        val edge = month in (species.seasonStartMonth - 1)..(species.seasonEndMonth + 1)
        val pts = when {
            inSeason -> 20
            edge -> 8
            else -> 0
        }
        if (pts > 0) reasons.add("Sezon: +${pts}pkt")
        return pts
    }

    private fun scoreMoisture(
        species: MushroomSpecies,
        moisturePercent: Double,
        reasons: MutableList<String>,
    ): Int {
        val pts = when (species.moisturePreference) {
            "high" -> if (moisturePercent >= 70) 10 else if (moisturePercent >= 50) 4 else 0
            "medium" -> if (moisturePercent in 40.0..85.0) 10 else if (moisturePercent >= 30) 4 else 0
            "low" -> if (moisturePercent <= 50) 10 else if (moisturePercent <= 65) 4 else 0
            else -> 0
        }
        if (pts > 0) reasons.add("Wilgotność: +${pts}pkt")
        return pts
    }
}

data class MushroomPredictionResult(
    val predictedMushrooms: List<PredictedMushroom>,
    val summary: String?,
)
