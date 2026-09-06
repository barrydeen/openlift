package dev.openlift.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.openlift.app.domain.WeightUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val UNIT = stringPreferencesKey("unit")
        val REST_SECONDS = longPreferencesKey("rest_seconds")
        val FAST_ADD_REST = longPreferencesKey("fast_add_rest")
    }

    val unit: Flow<WeightUnit> = context.dataStore.data
        .map { prefs -> WeightUnit.from(prefs[Keys.UNIT]) }
        .distinctUntilChanged()

    val restSeconds: Flow<Long> = context.dataStore.data
        .map { prefs -> prefs[Keys.REST_SECONDS] ?: DEFAULT_REST_SECONDS }
        .distinctUntilChanged()

    val fastAddRest: Flow<Long> = context.dataStore.data
        .map { prefs -> prefs[Keys.FAST_ADD_REST] ?: DEFAULT_EXPRESS_REST }
        .distinctUntilChanged()

    suspend fun setUnit(unit: WeightUnit) {
        context.dataStore.edit { it[Keys.UNIT] = unit.name }
    }

    suspend fun setRestSeconds(seconds: Long) {
        context.dataStore.edit { it[Keys.REST_SECONDS] = seconds.coerceIn(5L, 600L) }
    }

    companion object {
        const val DEFAULT_REST_SECONDS = 120L
        const val DEFAULT_EXPRESS_REST = 60L
    }
}
