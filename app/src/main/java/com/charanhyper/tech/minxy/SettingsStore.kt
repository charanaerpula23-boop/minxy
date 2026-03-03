package com.charanhyper.tech.minxy

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object SettingsStore {

    private val GRID_COLUMNS   = intPreferencesKey("grid_columns")
    private val ICON_SIZE_DP   = intPreferencesKey("icon_size_dp")
    private val DRAWER_OPACITY = intPreferencesKey("drawer_opacity")

    fun getGridColumns(context: Context): Flow<Int> =
        context.dataStore.data.map { it[GRID_COLUMNS] ?: 5 }

    fun getIconSizeDp(context: Context): Flow<Int> =
        context.dataStore.data.map { it[ICON_SIZE_DP] ?: 52 }

    fun getDrawerOpacity(context: Context): Flow<Int> =
        context.dataStore.data.map { it[DRAWER_OPACITY] ?: 92 }

    suspend fun setGridColumns(context: Context, columns: Int) {
        context.dataStore.edit { it[GRID_COLUMNS] = columns }
    }

    suspend fun setIconSizeDp(context: Context, size: Int) {
        context.dataStore.edit { it[ICON_SIZE_DP] = size }
    }

    suspend fun setDrawerOpacity(context: Context, opacity: Int) {
        context.dataStore.edit { it[DRAWER_OPACITY] = opacity.coerceIn(0, 100) }
    }
}
