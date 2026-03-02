package com.charanhyper.tech.minxy.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charanhyper.tech.minxy.AppViewModel

@Composable
fun IconPacksScreen(
    viewModel: AppViewModel,
    onClose: () -> Unit
) {
    val apps by viewModel.visibleApps.collectAsState()
    val iconPackUri by viewModel.iconPackUri.collectAsState()
    val iconOverrides by viewModel.iconOverrides.collectAsState()

    // pending pkg for per-app image pick
    var pendingPkg by remember { mutableStateOf<String?>(null) }

    val folderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) viewModel.setIconPack(uri)
    }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        val pkg = pendingPkg
        if (uri != null && pkg != null) viewModel.setAppIcon(pkg, uri)
        pendingPkg = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(56.dp))

        // header
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
                text = "Icon Packs",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 1.sp
            )
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = Color(0xFF1E1E1E))
        Spacer(Modifier.height(20.dp))

        // --- icon pack folder section ---
        Text("Icon Pack Folder", color = Color(0xFF888888), fontSize = 11.sp, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (iconPackUri != null) uriDisplayName(iconPackUri!!)
                       else "No folder selected",
                color = if (iconPackUri != null) Color(0xFFCCCCCC) else Color(0xFF444444),
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Choose",
                color = Color.White,
                fontSize = 13.sp,
                modifier = Modifier
                    .clickable { folderPicker.launch(null) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
            if (iconPackUri != null) {
                Text(
                    text = "Clear",
                    color = Color(0xFF666666),
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clickable { viewModel.clearIconPack() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            "Name images as package name, e.g. com.whatsapp.png",
            color = Color(0xFF3A3A3A),
            fontSize = 11.sp
        )

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = Color(0xFF1A1A1A))
        Spacer(Modifier.height(16.dp))

        // --- per-app overrides ---
        Text("Per-App Icons", color = Color(0xFF888888), fontSize = 11.sp, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(10.dp))

        LazyColumn {
            items(apps, key = { it.packageName }) { app ->
                val hasOverride = iconOverrides.containsKey(app.packageName)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    app.icon?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = app.name,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                    }
                    Text(
                        text = app.name,
                        color = if (hasOverride) Color.White else Color(0xFF999999),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Light,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Change",
                        color = Color(0xFF777777),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .clickable {
                                pendingPkg = app.packageName
                                imagePicker.launch(arrayOf("image/*"))
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                    if (hasOverride) {
                        Text(
                            text = "Reset",
                            color = Color(0xFF444444),
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clickable { viewModel.clearAppIcon(app.packageName) }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
                HorizontalDivider(color = Color(0xFF111111))
            }
        }
    }
}

private fun uriDisplayName(uri: String): String =
    Uri.parse(uri).lastPathSegment?.substringAfterLast(":") ?: uri
