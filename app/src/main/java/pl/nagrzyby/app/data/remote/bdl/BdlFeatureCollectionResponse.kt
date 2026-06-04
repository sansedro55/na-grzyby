package pl.nagrzyby.app.data.remote.bdl

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class BdlFeatureCollectionResponse(
    val features: List<BdlFeature>? = null,
)

data class BdlFeature(
    val id: Int? = null,
    val properties: BdlFeatureProperties? = null,
    val geometry: JsonElement? = null,
)

data class BdlFeatureProperties(
    @SerializedName("inspectorate_name")
    val inspectorateName: String? = null,
    @SerializedName("region_cd")
    val regionCode: String? = null,
    @SerializedName("inspectorate_cd")
    val inspectorateCode: String? = null,
)
