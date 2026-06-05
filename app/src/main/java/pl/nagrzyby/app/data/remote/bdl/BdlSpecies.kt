package pl.nagrzyby.app.data.remote.bdl

data class SpeciesInfo(
    val code: String,
    val name: String,
    val mushroomBonus: Int,
)

object BdlSpecies {

    private val speciesMap: Map<String, SpeciesInfo> = mapOf(
        "SO" to SpeciesInfo("SO", "Sosna", 2),
        "DB" to SpeciesInfo("DB", "Dąb", 5),
        "BK" to SpeciesInfo("BK", "Buk", 4),
        "BRZ" to SpeciesInfo("BRZ", "Brzoza", 0),
        "OL" to SpeciesInfo("OL", "Olsza", 0),
        "ŚW" to SpeciesInfo("ŚW", "Świerk", 0),
        "JD" to SpeciesInfo("JD", "Jodła", 3),
        "GB" to SpeciesInfo("GB", "Grab", 0),
        "LP" to SpeciesInfo("LP", "Lipa", 0),
        "MD" to SpeciesInfo("MD", "Modrzew", 1),
        "OS" to SpeciesInfo("OS", "Osika", 0),
        "DB_B" to SpeciesInfo("DB_B", "Dąb bezszypułkowy", 5),
        "DB_S" to SpeciesInfo("DB_S", "Dąb szypułkowy", 5),
        "KL" to SpeciesInfo("KL", "Klon", 0),
        "JS" to SpeciesInfo("JS", "Jesion", 0),
        "WB" to SpeciesInfo("WB", "Wiąz", 0),
        "AK" to SpeciesInfo("AK", "Akacja", 0),
        "TP" to SpeciesInfo("TP", "Topola", 0),
    )

    fun getSpeciesInfo(code: String): SpeciesInfo =
        speciesMap[code.uppercase()] ?: SpeciesInfo(code, code, 0)

    fun getPolishName(code: String): String = getSpeciesInfo(code).name

    fun getMushroomBonus(code: String): Int = getSpeciesInfo(code).mushroomBonus

    fun calculateWeightedBonus(speciesComposition: List<BdlSpeciesEntry>): Int {
        if (speciesComposition.isEmpty()) return 0
        val totalArea = speciesComposition.sumOf { it.areaHa }.coerceAtLeast(0.01)
        return speciesComposition.sumOf { entry ->
            val share = entry.areaHa / totalArea
            (getMushroomBonus(entry.speciesCode) * share).toInt()
        }
    }

    fun describeComposition(speciesComposition: List<BdlSpeciesEntry>): String {
        if (speciesComposition.isEmpty()) return "Brak danych"
        val totalArea = speciesComposition.sumOf { it.areaHa }.coerceAtLeast(0.01)
        return speciesComposition
            .sortedByDescending { it.areaHa }
            .mapNotNull { entry ->
                val pct = (entry.areaHa / totalArea * 100).toInt()
                if (pct == 0) null else {
                    val name = getPolishName(entry.speciesCode)
                    "$name $pct%"
                }
            }
            .joinToString(", ")
            .ifBlank { "Brak danych" }
    }
}

data class BdlSpeciesEntry(
    val speciesCode: String,
    val areaHa: Double,
)
