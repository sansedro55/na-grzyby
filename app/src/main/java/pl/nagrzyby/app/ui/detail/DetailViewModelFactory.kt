package pl.nagrzyby.app.ui.detail

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DetailViewModelFactory(
    private val application: Application,
    private val districtId: String,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            return DetailViewModel(application, districtId) as T
        }
        throw IllegalArgumentException("Nieznany ViewModel: ${modelClass.name}")
    }
}
