package pl.nagrzyby.app.data.model

/**
 * Reprezentuje nadleśnictwo / jednostkę leśnictwa.
 * Docelowo dane z API BDL (Bank Danych o Lasach).
 */
data class ForestDistrict(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val region: String,
    /** Odległość od użytkownika w km; null gdy nie sortowano po GPS. */
    val distanceKm: Double? = null,
    /** Uzupełniane przy wyszukiwaniu po miejscowości (np. „Balczewo”). */
    val searchViaPlace: String? = null,
)
