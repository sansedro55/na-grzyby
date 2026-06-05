package pl.nagrzyby.app.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.nagrzyby.app.NaGrzybyApplication
import pl.nagrzyby.app.di.AppContainer
import pl.nagrzyby.app.location.LocationProvider
import pl.nagrzyby.app.logging.DownloadsErrorLogger
import pl.nagrzyby.app.map.OsmdroidInitializer
import pl.nagrzyby.app.util.hasLocationPermission

class MapViewModel(
    application: Application,
    private val districtId: String?,
    container: AppContainer = (application as NaGrzybyApplication).container,
) : AndroidViewModel(application) {

    private val forestRepository = container.forestRepository
    private val bdlForestRepository = container.bdlForestRepository
    private val locationProvider = LocationProvider(application)

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        OsmdroidInitializer.init(application)
        if (districtId != null) {
            viewModelScope.launch {
                try {
                    val district = forestRepository.getDistrictById(districtId)
                    if (district == null) {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = "Nie znaleziono nadleśnictwa")
                        }
                        return@launch
                    }
                    _uiState.update {
                        it.copy(district = district, isLoading = false)
                    }
                } catch (e: Exception) {
                    DownloadsErrorLogger.log(
                        context = application,
                        level = "ERROR",
                        tag = "MapViewModel",
                        message = "Błąd ładowania nadleśnictwa na mapie",
                        throwable = e,
                    )
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "Błąd mapy")
                    }
                }
            }
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onMapTapped(latitude: Double, longitude: Double) {
        _uiState.update {
            it.copy(
                tappedLatitude = latitude,
                tappedLongitude = longitude,
                nearbyDistricts = emptyList(),
            )
        }
    }

    fun searchNearby() {
        val lat = _uiState.value.tappedLatitude ?: return
        val lon = _uiState.value.tappedLongitude ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSearchingNearby = true) }
            try {
                val results = bdlForestRepository.fetchNearContainingFirst(lat, lon)
                _uiState.update {
                    it.copy(nearbyDistricts = results, isSearchingNearby = false)
                }
            } catch (e: Exception) {
                DownloadsErrorLogger.log(
                    context = getApplication(),
                    level = "WARN",
                    tag = "MapViewModel",
                    message = "Błąd wyszukiwania nadleśnictw w pobliżu punktu",
                    throwable = e,
                )
                _uiState.update { it.copy(isSearchingNearby = false) }
            }
        }
    }

    fun clearTappedPoint() {
        _uiState.update { it.copy(tappedLatitude = null, tappedLongitude = null, nearbyDistricts = emptyList()) }
    }

    fun loadUserLocationIfPermitted() {
        if (!getApplication<Application>().hasLocationPermission()) {
            _uiState.update { it.copy(locationDenied = true) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingUserLocation = true, locationDenied = false) }
            try {
                val (lat, lon) = locationProvider.getCurrentLocation()
                _uiState.update {
                    it.copy(
                        userLatitude = lat,
                        userLongitude = lon,
                        isLoadingUserLocation = false,
                    )
                }
            } catch (e: Exception) {
                DownloadsErrorLogger.log(
                    context = getApplication(),
                    level = "WARN",
                    tag = "MapViewModel",
                    message = "Nie udało się pobrać lokalizacji na mapie",
                    throwable = e,
                )
                _uiState.update { it.copy(isLoadingUserLocation = false) }
            }
        }
    }
}
