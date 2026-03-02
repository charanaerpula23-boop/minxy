package com.charanhyper.tech.minxy

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object IconStore {

    private val ICON_PACK_URI = stringPreferencesKey("icon_pack_uri")
    private val ICON_OVERRIDES = stringSetPreferencesKey("icon_overrides") // "pkg::uri"

    fun getIconPackUri(context: Context): Flow<String?> =
        context.dataStore.data.map { it[ICON_PACK_URI] }

    fun getIconOverrides(context: Context): Flow<Map<String, String>> =
        context.dataStore.data.map { prefs ->
            (prefs[ICON_OVERRIDES] ?: emptySet()).associate { entry ->
                val idx = entry.indexOf("::")
                entry.substring(0, idx) to entry.substring(idx + 2)
            }
        }

    suspend fun setIconPackUri(context: Context, uri: String?) {
        context.dataStore.edit { prefs ->
            if (uri == null) prefs.remove(ICON_PACK_URI)
            else prefs[ICON_PACK_URI] = uri
        }
    }

    suspend fun setIconOverride(context: Context, pkg: String, uri: String) {
        context.dataStore.edit { prefs ->
            val current = (prefs[ICON_OVERRIDES] ?: emptySet())
                .filter { !it.startsWith("$pkg::") }.toSet()
            prefs[ICON_OVERRIDES] = current + "$pkg::$uri"
        }
    }

    suspend fun clearIconOverride(context: Context, pkg: String) {
        context.dataStore.edit { prefs ->
            prefs[ICON_OVERRIDES] = (prefs[ICON_OVERRIDES] ?: emptySet())
                .filter { !it.startsWith("$pkg::") }.toSet()
        }
    }
}
