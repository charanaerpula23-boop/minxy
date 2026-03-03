package com.charanhyper.tech.minxy

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object HomeAppsStore {

    // Stored as a comma-separated ordered list of package names
    private val HOME_APPS_KEY = stringPreferencesKey("home_apps")

    fun getHomeApps(context: Context): Flow<List<String>> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[HOME_APPS_KEY] ?: ""
            if (raw.isEmpty()) emptyList() else raw.split(",")
        }

    suspend fun addApp(context: Context, packageName: String) {
        context.dataStore.edit { prefs ->
            val current = (prefs[HOME_APPS_KEY] ?: "").split(",").filter { it.isNotEmpty() }
            if (packageName !in current) {
                prefs[HOME_APPS_KEY] = (current + packageName).joinToString(",")
            }
        }
    }

    suspend fun removeApp(context: Context, packageName: String) {
        context.dataStore.edit { prefs ->
            val current = (prefs[HOME_APPS_KEY] ?: "").split(",").filter { it.isNotEmpty() }
            prefs[HOME_APPS_KEY] = current.filter { it != packageName }.joinToString(",")
        }
    }
}
