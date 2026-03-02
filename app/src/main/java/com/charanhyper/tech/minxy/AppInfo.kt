package com.charanhyper.tech.minxy

import android.graphics.Bitmap

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Bitmap? = null
)
