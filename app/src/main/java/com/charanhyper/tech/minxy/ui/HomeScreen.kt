package com.charanhyper.tech.minxy.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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
    onOpenHiddenApps: () -> Unit
) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(getTime()) }
    var currentDate by remember { mutableStateOf(getDate()) }
    var battery by remember { mutableIntStateOf(-1) }
    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = getTime()
            currentDate = getDate()
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
            .background(Color.Black)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -60f) onOpenDrawer()
                }
            }
    ) {
        // center: clock / date / battery
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentTime,
                color = Color.White,
                fontSize = 80.sp,
                fontWeight = FontWeight.Thin,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = currentDate,
                color = Color(0xFF666666),
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 1.sp
            )
            if (battery >= 0) {
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "$battery%",
                    color = batteryColor(battery),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // top-right 3-dots menu
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 8.dp)
        ) {
            IconButton(onClick = { menuExpanded = true }) {
                Text("⋮", color = Color(0xFF555555), fontSize = 22.sp)
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier.background(Color(0xFF111111))
            ) {
                DropdownMenuItem(
                    text = { Text("Hidden Apps", color = Color(0xFFCCCCCC), fontSize = 14.sp) },
                    onClick = { menuExpanded = false; onOpenHiddenApps() }
                )
                DropdownMenuItem(
                    text = { Text("Super User", color = Color(0xFFCCCCCC), fontSize = 14.sp) },
                    onClick = {
                        menuExpanded = false
                        context.startActivity(
                            Intent(Settings.ACTION_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                        )
                    }
                )
            }
        }

        Text(
            text = "↑",
            color = Color(0xFF2A2A2A),
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 30.dp)
        )
    }
}

private fun batteryColor(level: Int) = when {
    level <= 15 -> Color(0xFFFF4444)
    level <= 30 -> Color(0xFFFF9900)
    else -> Color(0xFF555555)
}

private fun getTime(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
private fun getDate(): String = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
