package com.charanhyper.tech.minxy

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val hiddenApps: StateFlow<Set<String>> = HiddenAppsStore
        .getHiddenApps(application)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    private val allApps: List<AppInfo> = loadApps()

    val visibleApps: StateFlow<List<AppInfo>> =
        combine(_searchQuery, hiddenApps) { query, hidden ->
            allApps
                .filter { it.packageName !in hidden }
                .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val hiddenAppsList: StateFlow<List<AppInfo>> = hiddenApps.map { hidden ->
        allApps.filter { it.packageName in hidden }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun hideApp(packageName: String) = viewModelScope.launch {
        HiddenAppsStore.hideApp(getApplication(), packageName)
    }

    fun unhideApp(packageName: String) = viewModelScope.launch {
        HiddenAppsStore.unhideApp(getApplication(), packageName)
    }

    fun launchApp(packageName: String) {
        val intent = getApplication<Application>()
            .packageManager
            .getLaunchIntentForPackage(packageName) ?: return
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        getApplication<Application>().startActivity(intent)
    }

    private fun loadApps(): List<AppInfo> {
        val pm = getApplication<Application>().packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return pm.queryIntentActivities(intent, PackageManager.GET_META_DATA)
            .map { ri ->
                AppInfo(
                    name = ri.loadLabel(pm).toString(),
                    packageName = ri.activityInfo.packageName
                )
            }
            .filter { it.packageName != "com.charanhyper.tech.minxy" }
            .sortedBy { it.name.lowercase() }
    }
}
