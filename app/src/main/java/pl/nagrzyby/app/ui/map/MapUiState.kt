package pl.nagrzyby.app.ui.map

import pl.nagrzyby.app.data.model.ForestDistrict

data class MapUiState(
    val district: ForestDistrict? = null,
    val userLatitude: Double? = null,
    val userLongitude: Double? = null,
    val isLoading: Boolean = true,
    val isLoadingUserLocation: Boolean = false,
    val locationDenied: Boolean = false,
    val errorMessage: String? = null,
)
