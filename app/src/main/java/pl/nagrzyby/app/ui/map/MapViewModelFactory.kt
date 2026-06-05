package pl.nagrzyby.app.ui.map

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MapViewModelFactory(
    private val application: Application,
    private val districtId: String?,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(application, districtId) as T
        }
        throw IllegalArgumentException("Nieznany ViewModel: ${modelClass.name}")
    }
}
