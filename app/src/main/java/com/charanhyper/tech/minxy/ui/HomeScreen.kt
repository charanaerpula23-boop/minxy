package com.charanhyper.tech.minxy.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onOpenDrawer: () -> Unit,
    onOpenHiddenApps: () -> Unit  // secret: 5-tap top-left
) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(getTime()) }
    var currentDate by remember { mutableStateOf(getDate()) }
    var battery by remember { mutableIntStateOf(-1) }
    var secretTapCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = getTime()
            currentDate = getDate()
        }
    }

    // reset secret tap count after 2s of inactivity
    LaunchedEffect(secretTapCount) {
        if (secretTapCount > 0) {
            delay(2000)
            secretTapCount = 0
        }
    }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) battery = (level * 100f / scale).toInt()
            }
        }
        val sticky = context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        sticky?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level >= 0 && scale > 0) battery = (level * 100f / scale).toInt()
        }
        onDispose { context.unregisterReceiver(receiver) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // transparent — wallpaper shows through
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -60f) onOpenDrawer()
                }
            }
    ) {
        // secret 5-tap zone top-left
        Box(
            modifier = Modifier
                .size(72.dp)
                .align(Alignment.TopStart)
                .pointerInput(Unit) {
                    detectTapGestures {
                        secretTapCount++
                        if (secretTapCount >= 5) {
                            secretTapCount = 0
                            onOpenHiddenApps()
                        }
                    }
                }
        )

        // clock widget — centered vertically like Pixel launcher
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentTime,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 86.sp,
                    fontWeight = FontWeight.Thin,
                    letterSpacing = (-2).sp,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = Offset(0f, 2f),
                        blurRadius = 8f
                    )
                )
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = currentDate,
                style = TextStyle(
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 0.5.sp,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.4f),
                        offset = Offset(0f, 1f),
                        blurRadius = 6f
                    )
                )
            )
            if (battery >= 0) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "$battery%",
                    style = TextStyle(
                        color = batteryColor(battery),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.3f),
                            offset = Offset(0f, 1f),
                            blurRadius = 4f
                        )
                    )
                )
            }
        }

        // swipe-up cue
        Text(
            text = "⌃",
            color = Color.White.copy(alpha = 0.25f),
            fontSize = 22.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 12.dp)
        )
    }
}

private fun batteryColor(level: Int) = when {
    level <= 15 -> Color(0xFFEF4444)
    level <= 30 -> Color(0xFFFBBF24)
    else        -> Color.White.copy(alpha = 0.7f)
}

private fun getTime(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
private fun getDate(): String = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())

