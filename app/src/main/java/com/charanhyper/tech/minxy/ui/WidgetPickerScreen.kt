package com.charanhyper.tech.minxy.ui

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable

data class WidgetOption(
    val providerInfo: AppWidgetProviderInfo,
    val appLabel: String,
    val widgetLabel: String,
    val preview: Drawable?
)

@Composable
fun WidgetPickerScreen(
    onSelect: (AppWidgetProviderInfo) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager
    val widgetManager = remember { AppWidgetManager.getInstance(context) }
    val widgets = remember {
        widgetManager.installedProviders
            .map { info ->
                WidgetOption(
                    providerInfo = info,
                    appLabel = info.loadLabel(pm),
                    widgetLabel = info.loadLabel(pm),
                    preview = info.loadPreviewImage(context, 0)
                )
            }
            .sortedBy { it.appLabel.lowercase() }
    }

    // Group by app
    val grouped = remember(widgets) {
        widgets.groupBy { it.appLabel }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "←",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 22.sp,
                    modifier = Modifier
                        .clickable { onClose() }
                        .padding(end = 16.dp)
                )
                Text(
                    text = "Widgets",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Widget list grouped by app
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                grouped.forEach { (appName, options) ->
                    item(key = "header_$appName") {
                        Text(
                            text = appName,
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp, start = 4.dp)
                        )
                    }
                    items(options, key = { it.providerInfo.provider.flattenToString() }) { option ->
                        WidgetOptionItem(
                            option = option,
                            onClick = { onSelect(option.providerInfo) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetOptionItem(
    option: WidgetOption,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Preview or placeholder
        val previewBitmap = remember(option.preview) {
            option.preview?.let { drawableToBitmap(it) }
        }
        if (previewBitmap != null) {
            Image(
                bitmap = previewBitmap.asImageBitmap(),
                contentDescription = option.widgetLabel,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Text("📦", fontSize = 24.sp)
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.widgetLabel,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val minW = option.providerInfo.minWidth
            val minH = option.providerInfo.minHeight
            if (minW > 0 && minH > 0) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${minW}×${minH} dp",
                    color = Color.White.copy(alpha = 0.35f),
                    fontSize = 12.sp
                )
            }
        }

        Text(
            text = "+",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 22.sp,
            fontWeight = FontWeight.Light
        )
    }
}

private fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        return drawable.bitmap
    }
    val w = drawable.intrinsicWidth.coerceAtLeast(1)
    val h = drawable.intrinsicHeight.coerceAtLeast(1)
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    drawable.setBounds(0, 0, w, h)
    drawable.draw(canvas)
    return bmp
}
