package com.charanhyper.tech.minxy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charanhyper.tech.minxy.AppViewModel

@Composable
fun HiddenAppsScreen(
    viewModel: AppViewModel,
    onClose: () -> Unit
) {
    val hiddenApps by viewModel.hiddenAppsList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 28.dp)
    ) {
        Spacer(Modifier.height(56.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "←",
                color = Color.White,
                fontSize = 22.sp,
                modifier = Modifier
                    .clickable(onClick = onClose)
                    .padding(end = 16.dp, top = 4.dp, bottom = 4.dp)
            )
            Text(
                text = "Hidden Apps",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 1.sp
            )
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = Color(0xFF1E1E1E))
        Spacer(Modifier.height(16.dp))

        if (hiddenApps.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No hidden apps",
                    color = Color(0xFF444444),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Light
                )
            }
        } else {
            Text(
                text = "Tap 'Show' to restore an app",
                color = Color(0xFF444444),
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            LazyColumn {
                items(hiddenApps, key = { it.packageName }) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = app.name,
                            color = Color(0xFF777777),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Light
                        )
                        Text(
                            text = "Show",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier
                                .clickable { viewModel.unhideApp(app.packageName) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    HorizontalDivider(color = Color(0xFF161616))
                }
            }
        }
    }
}
