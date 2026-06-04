package pl.nagrzyby.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.nagrzyby.app.NaGrzybyApplication
import pl.nagrzyby.app.data.model.ForestDistrict
import pl.nagrzyby.app.di.AppContainer
import pl.nagrzyby.app.location.LocationProvider
import pl.nagrzyby.app.logging.DownloadsErrorLogger
import pl.nagrzyby.app.notifications.NotificationScheduler

class ForestViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val container: AppContainer = (application as NaGrzybyApplication).container
    private val forestRepository = container.forestRepository
    private val bdlRepository = container.bdlForestRepository
    private val placeSearchRepository = container.placeForestSearchRepository
    private val userPreferences = container.userPreferences
    private val verdictHistoryRepository = container.verdictHistoryRepository
    private val locationProvider = LocationProvider(application)

    private val _uiState = MutableStateFlow(ForestUiState())
    val uiState: StateFlow<ForestUiState> = _uiState.asStateFlow()

    private var lastUserLatitude: Double? = null
    private var lastUserLongitude: Double? = null
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            try {
                forestRepository.ensureSeeded()
                refreshDistrictList()
                loadRecentVerdicts()
            } catch (e: Exception) {
                DownloadsErrorLogger.log(
                    context = application,
                    level = "ERROR",
                    tag = "ForestViewModel",
                    message = "Błąd inicjalizacji listy nadleśnictw",
                    throwable = e,
                )
            }
        }
        viewModelScope.launch {
            userPreferences.favoriteDistrictIds.collect { ids ->
                val favorites = ids.mapNotNull { forestRepository.getDistrictById(it) }
                _uiState.update { it.copy(favoriteIds = ids, favoriteDistricts = favorites) }
            }
        }
        viewModelScope.launch {
            userPreferences.notificationsEnabled.collect { enabled ->
                _uiState.update { it.copy(notificationsEnabled = enabled) }
            }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setNotificationsEnabled(enabled)
            if (enabled) {
                NotificationScheduler.schedule(getApplication())
            } else {
                NotificationScheduler.cancel(getApplication())
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            refreshDistrictList()
            if (query.trim().length >= 2) {
                delay(SEARCH_DEBOUNCE_MS)
                enrichFromBdlByName(query.trim())
            }
        }
    }

    fun findNearestDistricts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingLocation = true, snackbarMessage = null) }
            try {
                val (lat, lon) = locationProvider.getCurrentLocation()
                lastUserLatitude = lat
                lastUserLongitude = lon
                _uiState.update { it.copy(isLoadingBdl = true) }
                try {
                    bdlRepository.fetchAndCacheNear(lat, lon)
                } catch (e: Exception) {
                    DownloadsErrorLogger.log(
                        context = getApplication(),
                        level = "WARN",
                        tag = "ForestViewModel",
                        message = "BDL w pobliżu niedostępne — używam cache lokalnego",
                        throwable = e,
                    )
                }
                applyDistanceSort(lat, lon)
                _uiState.update {
                    it.copy(
                        isLoadingLocation = false,
                        isLoadingBdl = false,
                        isSortedByDistance = true,
                    )
                }
            } catch (e: Exception) {
                DownloadsErrorLogger.log(
                    context = getApplication(),
                    level = "ERROR",
                    tag = "ForestViewModel",
                    message = "Błąd pobierania lokalizacji",
                    throwable = e,
                )
                _uiState.update {
                    it.copy(
                        isLoadingLocation = false,
                        isLoadingBdl = false,
                        snackbarMessage = e.message ?: "Błąd pobierania lokalizacji",
                    )
                }
            }
        }
    }

    fun toggleFavorite(districtId: String) {
        viewModelScope.launch {
            userPreferences.toggleFavorite(districtId)
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private suspend fun enrichFromBdlByName(query: String) {
        _uiState.update { it.copy(isLoadingBdl = true, placeSearchCaption = null) }
        try {
            val local = forestRepository.searchLocal(query)
            val fromBdlName = try {
                bdlRepository.fetchAndCacheByName(query)
            } catch (e: Exception) {
                DownloadsErrorLogger.log(
                    context = getApplication(),
                    level = "WARN",
                    tag = "ForestViewModel",
                    message = "Wyszukiwanie BDL po nazwie nie powiodło się",
                    throwable = e,
                )
                emptyList()
            }

            val placeResult = if (shouldSearchByPlace(query)) {
                try {
                    placeSearchRepository.findDistrictsForLocality(query)
                } catch (e: Exception) {
                    DownloadsErrorLogger.log(
                        context = getApplication(),
                        level = "WARN",
                        tag = "ForestViewModel",
                        message = "Wyszukiwanie po miejscowości nie powiodło się",
                        throwable = e,
                    )
                    null
                }
            } else {
                null
            }

            val placeDistricts = placeResult?.districts.orEmpty()
            val caption = placeResult?.let {
                "Miejscowość: ${it.place.query} → najbliższe nadleśnictwo"
            }

            val merged = forestRepository.mergeDistrictLists(
                placeDistricts,
                local,
                fromBdlName,
            )
            val withDistance = applyDistanceIfNeeded(merged)
            _uiState.update {
                it.copy(
                    districts = withDistance,
                    isLoadingBdl = false,
                    placeSearchCaption = caption,
                )
            }
        } catch (e: Exception) {
            DownloadsErrorLogger.log(
                context = getApplication(),
                level = "WARN",
                tag = "ForestViewModel",
                message = "Wyszukiwanie BDL nie powiodło się",
                throwable = e,
            )
            _uiState.update { it.copy(isLoadingBdl = false) }
        }
    }

    private fun shouldSearchByPlace(query: String): Boolean {
        if (query.trim().length < 3) return false
        val normalized = query.trim().lowercase()
        return !normalized.startsWith("nadleś") && !normalized.contains("nadleśnictwo")
    }

    private suspend fun loadRecentVerdicts() {
        val entries = verdictHistoryRepository.getRecent(limit = 5)
        val ui = entries.map { entry ->
            RecentVerdictUi(
                districtId = entry.districtId,
                districtName = entry.districtName,
                scorePercent = entry.scorePercent,
                summary = entry.summary,
                createdAtEpochMs = entry.createdAtEpochMs,
            )
        }
        _uiState.update { it.copy(recentVerdicts = ui) }
    }

    fun refreshRecentVerdicts() {
        viewModelScope.launch { loadRecentVerdicts() }
    }

    private suspend fun refreshDistrictList() {
        val query = _uiState.value.searchQuery
        val list = forestRepository.searchLocal(query)
        _uiState.update {
            it.copy(
                districts = applyDistanceIfNeeded(list),
                placeSearchCaption = null,
            )
        }
    }

    private suspend fun applyDistanceSort(latitude: Double, longitude: Double) {
        val query = _uiState.value.searchQuery
        val local = forestRepository.searchLocal(query)
        val merged = forestRepository.mergeDistrictLists(local)
        val sorted = forestRepository.sortByDistanceFrom(latitude, longitude, merged)
        _uiState.update { it.copy(districts = sorted) }
    }

    private suspend fun applyDistanceIfNeeded(districts: List<ForestDistrict>): List<ForestDistrict> {
        val lat = lastUserLatitude
        val lon = lastUserLongitude
        if (_uiState.value.isSortedByDistance && lat != null && lon != null) {
            return forestRepository.sortByDistanceFrom(lat, lon, districts)
        }
        return districts
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 550L
    }
}
