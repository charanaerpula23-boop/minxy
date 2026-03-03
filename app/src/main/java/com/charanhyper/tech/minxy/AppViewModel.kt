package com.charanhyper.tech.minxy

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val hiddenApps: StateFlow<Set<String>> = HiddenAppsStore
        .getHiddenApps(application)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val iconPackUri: StateFlow<String?> = IconStore
        .getIconPackUri(application)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val iconOverrides: StateFlow<Map<String, String>> = IconStore
        .getIconOverrides(application)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val gridColumns: StateFlow<Int> = SettingsStore
        .getGridColumns(application)
        .stateIn(viewModelScope, SharingStarted.Eagerly, 5)

    val iconSizeDp: StateFlow<Int> = SettingsStore
        .getIconSizeDp(application)
        .stateIn(viewModelScope, SharingStarted.Eagerly, 52)

    // resolved custom bitmaps: override > pack > null (falls back to system icon in AppInfo)
    private val _customIcons = MutableStateFlow<Map<String, Bitmap>>(emptyMap())

    private val allApps: List<AppInfo> = loadSystemApps()

    val visibleApps: StateFlow<List<AppInfo>> =
        combine(_searchQuery, hiddenApps, _customIcons) { query, hidden, custom ->
            allApps
                .filter { it.packageName !in hidden }
                .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
                .map { app -> custom[app.packageName]?.let { app.copy(icon = it) } ?: app }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val hiddenAppsList: StateFlow<List<AppInfo>> = hiddenApps.map { hidden ->
        allApps.filter { it.packageName in hidden }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            combine(iconPackUri, iconOverrides) { pack, overrides -> pack to overrides }
                .collect { (pack, overrides) -> reloadCustomIcons(pack, overrides) }
        }
    }

    fun updateSearch(query: String) { _searchQuery.value = query }

    fun hideApp(packageName: String) = viewModelScope.launch {
        HiddenAppsStore.hideApp(getApplication(), packageName)
    }

    fun unhideApp(packageName: String) = viewModelScope.launch {
        HiddenAppsStore.unhideApp(getApplication(), packageName)
    }

    fun launchApp(packageName: String) {
        val intent = getApplication<Application>()
            .packageManager.getLaunchIntentForPackage(packageName) ?: return
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        getApplication<Application>().startActivity(intent)
    }

    fun setIconPack(treeUri: Uri) = viewModelScope.launch {
        getApplication<Application>().contentResolver
            .takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        IconStore.setIconPackUri(getApplication(), treeUri.toString())
    }

    fun clearIconPack() = viewModelScope.launch {
        IconStore.setIconPackUri(getApplication(), null)
    }

    fun setAppIcon(packageName: String, imageUri: Uri) = viewModelScope.launch {
        getApplication<Application>().contentResolver
            .takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        IconStore.setIconOverride(getApplication(), packageName, imageUri.toString())
    }

    fun clearAppIcon(packageName: String) = viewModelScope.launch {
        IconStore.clearIconOverride(getApplication(), packageName)
    }

    fun setGridColumns(columns: Int) = viewModelScope.launch {
        SettingsStore.setGridColumns(getApplication(), columns)
    }

    fun setIconSizeDp(size: Int) = viewModelScope.launch {
        SettingsStore.setIconSizeDp(getApplication(), size)
    }

    private suspend fun reloadCustomIcons(packUri: String?, overrides: Map<String, String>) =
        withContext(Dispatchers.IO) {
            val ctx = getApplication<Application>()
            val result = mutableMapOf<String, Bitmap>()
            // load icon pack folder
            if (packUri != null) {
                try {
                    DocumentFile.fromTreeUri(ctx, Uri.parse(packUri))
                        ?.listFiles()
                        ?.forEach { file ->
                            val pkg = file.name?.substringBeforeLast(".") ?: return@forEach
                            val bmp = ctx.contentResolver.openInputStream(file.uri)
                                ?.use { BitmapFactory.decodeStream(it) }
                            if (bmp != null) result[pkg] = bmp
                        }
                } catch (_: Exception) { }
            }
            // per-app overrides (higher priority, overwrite pack)
            overrides.forEach { (pkg, uriStr) ->
                try {
                    val bmp = ctx.contentResolver.openInputStream(Uri.parse(uriStr))
                        ?.use { BitmapFactory.decodeStream(it) }
                    if (bmp != null) result[pkg] = bmp
                } catch (_: Exception) { }
            }
            _customIcons.value = result
        }

    private fun loadSystemApps(): List<AppInfo> {
        val pm = getApplication<Application>().packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        return pm.queryIntentActivities(intent, PackageManager.GET_META_DATA)
            .map { ri ->
                val icon = try { drawableToBitmap(pm.getApplicationIcon(ri.activityInfo.packageName)) } catch (_: Exception) { null }
                AppInfo(ri.loadLabel(pm).toString(), ri.activityInfo.packageName, icon)
            }
            .filter { it.packageName != "com.charanhyper.tech.minxy" }
            .sortedBy { it.name.lowercase() }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) return drawable.bitmap
        val w = drawable.intrinsicWidth.coerceAtLeast(1)
        val h = drawable.intrinsicHeight.coerceAtLeast(1)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, w, h)
        drawable.draw(canvas)
        return bmp
    }
}
