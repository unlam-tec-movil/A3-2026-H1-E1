package ar.edu.unlam.mobile.scaffolding.infraestructure.adapters.device.sensor

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class StepCounterDataSource(
    private val context: Context,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("step_counter_prefs", Context.MODE_PRIVATE)

    fun getStepsFlow(): Flow<Int> =
        callbackFlow {
            trySend(prefs.getInt("steps_today", 0))

            val listener =
                SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                    if (key == "steps_today") {
                        trySend(sharedPreferences.getInt("steps_today", 0))
                    }
                }
            prefs.registerOnSharedPreferenceChangeListener(listener)
            awaitClose {
                prefs.unregisterOnSharedPreferenceChangeListener(listener)
            }
        }

    fun getStepsToday(): Int = prefs.getInt("steps_today", 0)

    fun saveStepsToday(steps: Int) {
        prefs.edit().putInt("steps_today", steps).apply()
    }

    fun getLastSensorValue(): Float = prefs.getFloat("last_sensor_value", -1f)

    fun saveLastSensorValue(value: Float) {
        prefs.edit().putFloat("last_sensor_value", value).apply()
    }

    fun getLastResetDate(): String? = prefs.getString("last_reset_date", null)

    fun saveLastResetDate(date: String) {
        prefs.edit().putString("last_reset_date", date).apply()
    }

    fun clearData() {
        prefs.edit().clear().apply()
    }
}
