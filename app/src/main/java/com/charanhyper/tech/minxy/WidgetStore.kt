package com.charanhyper.tech.minxy

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object WidgetStore {

    private val KEY = stringPreferencesKey("home_widgets")

    fun getWidgetIds(context: Context): Flow<List<Int>> =
        context.dataStore.data.map { prefs ->
            prefs[KEY]?.split(",")
                ?.mapNotNull { it.trim().toIntOrNull() }
                ?: emptyList()
        }

    suspend fun addWidgetId(context: Context, widgetId: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY]?.split(",")
                ?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList()
            if (widgetId !in current) {
                prefs[KEY] = (current + widgetId).joinToString(",")
            }
        }
    }

    suspend fun removeWidgetId(context: Context, widgetId: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY]?.split(",")
                ?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList()
            prefs[KEY] = current.filter { it != widgetId }.joinToString(",")
        }
    }
}
