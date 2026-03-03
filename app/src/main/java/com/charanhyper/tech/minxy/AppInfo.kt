package com.charanhyper.tech.minxy

import androidx.compose.ui.graphics.ImageBitmap

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: ImageBitmap? = null
)
