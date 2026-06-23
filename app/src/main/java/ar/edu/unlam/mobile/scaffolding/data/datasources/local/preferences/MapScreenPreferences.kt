package ar.edu.unlam.mobile.scaffolding.data.datasources.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.mapDataStore by preferencesDataStore(name = "map_preferences")

class MapScreenPreferences(
    private val context: Context,
) {
    companion object {
        private val LAST_CLINIC_ID = intPreferencesKey("last_clinic_id")
    }

    val lastClinicId: Flow<Int?> = context.mapDataStore.data.map { it[LAST_CLINIC_ID] }

    suspend fun saveLastClinicId(clinicId: Int?) {
        context.mapDataStore.edit { prefs ->
            if (clinicId != null) {
                prefs[LAST_CLINIC_ID] = clinicId
            } else {
                prefs.remove(LAST_CLINIC_ID)
            }
        }
    }
}
