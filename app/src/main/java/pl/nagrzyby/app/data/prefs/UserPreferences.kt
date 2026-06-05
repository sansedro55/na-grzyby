package pl.nagrzyby.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "na_grzyby_prefs",
)

class UserPreferences(private val context: Context) {

    val favoriteDistrictIds: Flow<Set<String>> =
        context.dataStore.data.map { prefs ->
            prefs[FAVORITE_IDS] ?: emptySet()
        }

    val notificationsEnabled: Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[NOTIFICATIONS_ENABLED] ?: false
        }

    val themeMode: Flow<String> =
        context.dataStore.data.map { prefs ->
            prefs[THEME_MODE] ?: "system"
        }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE] = mode
        }
    }

    suspend fun toggleFavorite(districtId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[FAVORITE_IDS]?.toMutableSet() ?: mutableSetOf()
            if (!current.add(districtId)) {
                current.remove(districtId)
            }
            prefs[FAVORITE_IDS] = current
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun getFavoriteIdsSnapshot(): Set<String> =
        favoriteDistrictIds.first()

    suspend fun areNotificationsEnabled(): Boolean =
        notificationsEnabled.first()

    companion object {
        private val FAVORITE_IDS = stringSetPreferencesKey("favorite_district_ids")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
