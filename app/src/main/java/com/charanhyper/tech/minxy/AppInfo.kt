package com.charanhyper.tech.minxy

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.ImageBitmap

@Stable
data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: ImageBitmap? = null
)
