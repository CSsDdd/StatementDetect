package com.example.statement_detect.data.datastore

// SettingsDataStore.kt
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object SettingsKeys {
    val WORK_DURATION = intPreferencesKey("work_duration")
    val RELAX_DURATION = intPreferencesKey("relax_duration")
    val ROUND_COUNT = intPreferencesKey("round_count")
    val SEGMENT_COUNT = intPreferencesKey("segment_count")

}

class SettingsRepository(private val context: Context) {

    val timerDuration: Flow<Int> = context.dataStore.data
        .map { it[SettingsKeys.WORK_DURATION] ?: 1500 }  // 默认25分钟

    val relaxDuration: Flow<Int> = context.dataStore.data
        .map { it[SettingsKeys.RELAX_DURATION] ?: 300 }   // 默认5分钟

    val roundCount: Flow<Int> = context.dataStore.data
        .map { it[SettingsKeys.ROUND_COUNT] ?: 1 }

    val SegmentCount: Flow<Int> = context.dataStore.data
        .map { it[SettingsKeys.SEGMENT_COUNT] ?: 2 }

    suspend fun saveWorkDuration(value: Int) {
        context.dataStore.edit { it[SettingsKeys.WORK_DURATION] = value }
    }

    suspend fun saveRelaxDuration(value: Int) {
        context.dataStore.edit { it[SettingsKeys.RELAX_DURATION] = value }
    }

    suspend fun saveRoundCount(value: Int) {
        context.dataStore.edit { it[SettingsKeys.ROUND_COUNT] = value }
    }

    suspend fun saveSegmentCount(value: Int) {
        context.dataStore.edit { it[SettingsKeys.SEGMENT_COUNT] = value }
    }
}