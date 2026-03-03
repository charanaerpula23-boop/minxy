package com.charanhyper.tech.minxy.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charanhyper.tech.minxy.AppViewModel

@Composable
fun SettingsScreen(
    viewModel: AppViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val gridColumns by viewModel.gridColumns.collectAsState()
    val iconSizeDp by viewModel.iconSizeDp.collectAsState()
    val iconPackUri by viewModel.iconPackUri.collectAsState()

    val folderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            viewModel.setIconPack(it)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080808))
            .verticalScroll(rememberScrollState())
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 8.dp, end = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFFCCCCCC)
                )
            }
            Text(
                text = "Settings",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        HorizontalDivider(color = Color(0xFF1A1A1A))
        Spacer(Modifier.height(24.dp))

        // ── Grid columns ──────────────────────────────
        SectionLabel("Grid columns")
        Stepper(
            value = gridColumns,
            onDecrement = { if (gridColumns > 3) viewModel.setGridColumns(gridColumns - 1) },
            onIncrement = { if (gridColumns < 7) viewModel.setGridColumns(gridColumns + 1) },
            range = "3 – 7"
        )

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = Color(0xFF1A1A1A), modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(24.dp))

        // ── Icon size ──────────────────────────────────
        SectionLabel("Icon size (dp)")
        Stepper(
            value = iconSizeDp,
            onDecrement = { if (iconSizeDp > 36) viewModel.setIconSizeDp(iconSizeDp - 4) },
            onIncrement = { if (iconSizeDp < 72) viewModel.setIconSizeDp(iconSizeDp + 4) },
            range = "36 – 72"
        )

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = Color(0xFF1A1A1A), modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(24.dp))

        // ── Icon pack ──────────────────────────────────
        SectionLabel("Icon pack folder")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (iconPackUri != null) "Folder selected  ✓" else "No folder selected",
                color = if (iconPackUri != null) Color(0xFF88BB66) else Color(0xFF555555),
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = { folderPicker.launch(null) }) {
                Text("Choose", color = Color(0xFF7799CC), fontSize = 13.sp)
            }
            if (iconPackUri != null) {
                TextButton(onClick = { viewModel.clearIconPack() }) {
                    Text("Clear", color = Color(0xFF885555), fontSize = 13.sp)
                }
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun SectionLabel(label: String) {
    Text(
        text = label.uppercase(),
        color = Color(0xFF444444),
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(start = 28.dp, bottom = 12.dp)
    )
}

@Composable
private fun Stepper(
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    range: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = range,
            color = Color(0xFF444444),
            fontSize = 12.sp
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF111111))
                .padding(horizontal = 4.dp)
        ) {
            IconButton(
                onClick = onDecrement,
                modifier = Modifier.size(40.dp)
            ) {
                Text("−", color = Color(0xFFAAAAAA), fontSize = 20.sp)
            }
            Text(
                text = value.toString(),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.width(36.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            IconButton(
                onClick = onIncrement,
                modifier = Modifier.size(40.dp)
            ) {
                Text("+", color = Color(0xFFAAAAAA), fontSize = 20.sp)
            }
        }
    }
}
