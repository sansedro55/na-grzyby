package pl.nagrzyby.app.ui

import pl.nagrzyby.app.data.model.ForestDistrict

data class ForestUiState(
    val searchQuery: String = "",
    val districts: List<ForestDistrict> = emptyList(),
    val favoriteDistricts: List<ForestDistrict> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val isLoadingLocation: Boolean = false,
    val isLoadingBdl: Boolean = false,
    val isSortedByDistance: Boolean = false,
    val snackbarMessage: String? = null,
    val notificationsEnabled: Boolean = false,
    val allHistory: List<RecentVerdictUi> = emptyList(),
    val isLoadingHistory: Boolean = false,
    /** np. „Miejscowość: Balczewo” gdy wynik z geokodowania */
    val placeSearchCaption: String? = null,
    val currentThemeMode: String = "system",
)
