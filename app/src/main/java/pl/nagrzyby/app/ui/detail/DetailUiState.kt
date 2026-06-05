package pl.nagrzyby.app.ui.detail

import pl.nagrzyby.app.data.local.VerdictHistoryEntity
import pl.nagrzyby.app.data.model.ForestDistrict
import pl.nagrzyby.app.data.model.ForestEnvironmentData
import pl.nagrzyby.app.data.model.MushroomForecastVerdict
import pl.nagrzyby.app.data.remote.bdl.BdlSpeciesEntry

data class DetailUiState(
    val district: ForestDistrict? = null,
    val environment: ForestEnvironmentData = ForestEnvironmentData(districtId = ""),
    val verdict: MushroomForecastVerdict? = null,
    val isAnalyzing: Boolean = false,
    val isFavorite: Boolean = false,
    val verdictHistory: List<VerdictHistoryEntity> = emptyList(),
    val bdlForestSummary: String? = null,
    val isLoadingBdlForest: Boolean = false,
    val speciesComposition: String? = null,
    val speciesCompositionEntries: List<BdlSpeciesEntry> = emptyList(),
    val isLoadingSpecies: Boolean = false,
)
