package pl.nagrzyby.app.data.remote.bdl

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class BdlLesnictwaCollectionResponse(
    val features: List<BdlLesnictwoFeature>? = null,
)

data class BdlLesnictwoFeature(
    val id: Int? = null,
    val properties: BdlLesnictwoProperties? = null,
    val geometry: JsonElement? = null,
)

data class BdlLesnictwoProperties(
    @SerializedName("forest_range_name")
    val forestRangeName: String? = null,
    @SerializedName("inspectorate_name")
    val inspectorateName: String? = null,
    @SerializedName("region_cd")
    val regionCode: String? = null,
    /** Kod głównego typu siedliskowego / gatunku — zależy od warstwy BDL. */
    @SerializedName("forest_habitat")
    val forestHabitat: String? = null,
    @SerializedName("dominant_species")
    val dominantSpecies: String? = null,
)
