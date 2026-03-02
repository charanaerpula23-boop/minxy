package com.charanhyper.tech.minxy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
    onOpenSettings: () -> Unit
) {
    var currentTime by remember { mutableStateOf(getTime()) }
    var currentDate by remember { mutableStateOf(getDate()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = getTime()
            currentDate = getDate()
        }
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
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentTime,
                color = Color.White,
                fontSize = 76.sp,
                fontWeight = FontWeight.Thin,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = currentDate,
                color = Color(0xFF777777),
                fontSize = 15.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 1.sp
            )
        }

        Text(
            text = "↑",
            color = Color(0xFF3A3A3A),
            fontSize = 22.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )

        // long-press ⋮ → settings
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 52.dp, end = 20.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { onOpenSettings() })
                }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⋮",
                color = Color(0xFF3A3A3A),
                fontSize = 24.sp
            )
        }
    }
}

private fun getTime(): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

private fun getDate(): String =
    SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
