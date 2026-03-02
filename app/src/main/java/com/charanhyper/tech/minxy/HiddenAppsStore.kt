package com.charanhyper.tech.minxy

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "minxy_prefs")

object HiddenAppsStore {

    private val HIDDEN_APPS_KEY = stringSetPreferencesKey("hidden_apps")

    fun getHiddenApps(context: Context): Flow<Set<String>> =
        context.dataStore.data.map { prefs ->
            prefs[HIDDEN_APPS_KEY] ?: emptySet()
        }

    suspend fun hideApp(context: Context, packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[HIDDEN_APPS_KEY] ?: emptySet()
            prefs[HIDDEN_APPS_KEY] = current + packageName
        }
    }

    suspend fun unhideApp(context: Context, packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[HIDDEN_APPS_KEY] ?: emptySet()
            prefs[HIDDEN_APPS_KEY] = current - packageName
        }
    }
}
