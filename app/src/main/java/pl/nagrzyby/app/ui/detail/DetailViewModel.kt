package pl.nagrzyby.app.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.nagrzyby.app.NaGrzybyApplication
import pl.nagrzyby.app.data.local.VerdictHistoryEntity
import pl.nagrzyby.app.data.model.ForestEnvironmentData
import pl.nagrzyby.app.di.AppContainer
import pl.nagrzyby.app.domain.MushroomForecastAnalyzer
import pl.nagrzyby.app.logging.DownloadsErrorLogger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailViewModel(
    application: Application,
    private val districtId: String,
    container: AppContainer = (application as NaGrzybyApplication).container,
) : AndroidViewModel(application) {

    private val forestRepository = container.forestRepository
    private val weatherRepository = container.weatherRepository
    private val userPreferences = container.userPreferences
    private val verdictHistoryRepository = container.verdictHistoryRepository
    private val bdlLesnictwaRepository = container.bdlLesnictwaRepository

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferences.favoriteDistrictIds.collect { ids ->
                _uiState.update { it.copy(isFavorite = ids.contains(districtId)) }
            }
        }
        viewModelScope.launch {
            val district = forestRepository.getDistrictById(districtId)
            if (district == null) {
                _uiState.update {
                    it.copy(
                        environment = ForestEnvironmentData(
                            districtId = districtId,
                            errorMessage = "Nie znaleziono nadleśnictwa",
                        ),
                    )
                }
            } else {
                _uiState.update { it.copy(district = district) }
                loadVerdictHistory()
                loadBdlForestInfo(district)
                loadWeather(district.latitude, district.longitude, forceRefresh = false)
            }
        }
    }

    private suspend fun loadVerdictHistory() {
        val history = verdictHistoryRepository.getForDistrict(districtId)
        _uiState.update { it.copy(verdictHistory = history) }
    }

    private fun loadBdlForestInfo(district: pl.nagrzyby.app.data.model.ForestDistrict) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingBdlForest = true) }
            try {
                val summary = bdlLesnictwaRepository.fetchForestSummaryNear(
                    latitude = district.latitude,
                    longitude = district.longitude,
                    inspectorateName = district.name,
                )
                _uiState.update {
                    it.copy(bdlForestSummary = summary, isLoadingBdlForest = false)
                }
            } catch (e: Exception) {
                DownloadsErrorLogger.log(
                    context = getApplication(),
                    level = "WARN",
                    tag = "DetailViewModel",
                    message = "BDL leśnictwa niedostępne",
                    throwable = e,
                )
                _uiState.update { it.copy(isLoadingBdlForest = false) }
            }
        }
    }

    fun refreshWeather() {
        val district = _uiState.value.district ?: return
        loadWeather(district.latitude, district.longitude, forceRefresh = true)
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            userPreferences.toggleFavorite(districtId)
        }
    }

    fun analyze() {
        val environment = _uiState.value.environment
        if (environment.isLoading || environment.errorMessage != null) return

        _uiState.update { it.copy(isAnalyzing = true, verdict = null) }
        val verdict = MushroomForecastAnalyzer.analyze(environment)
        val district = _uiState.value.district
        viewModelScope.launch {
            if (district != null) {
                verdictHistoryRepository.save(district.id, district.name, verdict)
                loadVerdictHistory()
            }
        }
        _uiState.update { it.copy(isAnalyzing = false, verdict = verdict) }
    }

    fun formatHistoryDate(epochMs: Long): String =
        SimpleDateFormat("d MMM yyyy, HH:mm", Locale("pl", "PL")).format(Date(epochMs))

    fun shareVerdictText(): String? {
        val district = _uiState.value.district ?: return null
        val verdict = _uiState.value.verdict ?: return null
        val env = _uiState.value.environment
        return buildString {
            appendLine("Na grzyby — ${district.name}")
            verdict.scorePercent?.let { appendLine("Szansa: $it%") }
            verdict.summary?.let { appendLine(it) }
            env.rainfallLast4DaysMm?.let { appendLine("Opady (4 dni): ${"%.1f".format(it)} mm") }
            env.averageTemperatureCelsius?.let { appendLine("Śr. temp.: ${"%.1f".format(it)} °C") }
            verdict.recommendation?.let { appendLine(it) }
        }.trim()
    }

    private fun loadWeather(latitude: Double, longitude: Double, forceRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    environment = it.environment.copy(
                        districtId = districtId,
                        isLoading = true,
                        errorMessage = null,
                    ),
                    verdict = null,
                )
            }
            try {
                val data = weatherRepository.fetchEnvironmentData(
                    districtId = districtId,
                    latitude = latitude,
                    longitude = longitude,
                    forceRefresh = forceRefresh,
                )
                _uiState.update { it.copy(environment = data) }
            } catch (e: Exception) {
                DownloadsErrorLogger.log(
                    context = getApplication(),
                    level = "ERROR",
                    tag = "DetailViewModel",
                    message = "Błąd Open-Meteo dla nadleśnictwa $districtId",
                    throwable = e,
                )
                _uiState.update {
                    it.copy(
                        environment = it.environment.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Nie udało się pobrać pogody",
                        ),
                    )
                }
            }
        }
    }
}
