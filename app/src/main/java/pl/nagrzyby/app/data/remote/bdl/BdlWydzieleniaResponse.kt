package pl.nagrzyby.app.data.remote.bdl

import com.google.gson.annotations.SerializedName

data class BdlWydzieleniaCollectionResponse(
    val features: List<BdlWydzielenieFeature>? = null,
)

data class BdlWydzielenieFeature(
    val id: Int? = null,
    val properties: BdlWydzielenieProperties? = null,
)

data class BdlWydzielenieProperties(
    @SerializedName("species_cd")
    val speciesCode: String? = null,
    @SerializedName("sub_area")
    val subAreaHa: Double? = null,
    @SerializedName("spec_age")
    val specAge: Int? = null,
    @SerializedName("site_type")
    val siteType: String? = null,
    @SerializedName("adr_for")
    val adrFor: String? = null,
)
