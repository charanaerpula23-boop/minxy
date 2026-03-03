package com.charanhyper.tech.minxy.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charanhyper.tech.minxy.AppInfo
import com.charanhyper.tech.minxy.AppViewModel

@Composable
fun AppDrawer(
    viewModel: AppViewModel,
    onOpenSettings: () -> Unit,
    onClose: () -> Unit
) {
    val apps by viewModel.visibleApps.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val iconOverrides by viewModel.iconOverrides.collectAsState()
    val columns by viewModel.gridColumns.collectAsState()
    val iconSize by viewModel.iconSizeDp.collectAsState()

    var menuExpanded by remember { mutableStateOf(false) }
    var pendingIconPkg by remember { mutableStateOf<String?>(null) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        val pkg = pendingIconPkg
        if (uri != null && pkg != null) viewModel.setAppIcon(pkg, uri)
        pendingIconPkg = null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xEE050505))
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 80f) onClose()
                }
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // drag pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp, bottom = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color(0xFF333333), RoundedCornerShape(2.dp))
                )
            }

            // search bar row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    BasicTextField(
                        value = query,
                        onValueChange = viewModel::updateSearch,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = TextStyle(color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Light),
                        cursorBrush = SolidColor(Color.White),
                        decorationBox = { inner ->
                            Column {
                                if (query.isEmpty()) {
                                    Text("Search apps…", color = Color(0xFF444444), fontSize = 15.sp, fontWeight = FontWeight.Light)
                                }
                                inner()
                                Spacer(Modifier.height(4.dp))
                                HorizontalDivider(color = Color(0xFF2A2A2A), thickness = 1.dp)
                            }
                        }
                    )
                }
                Spacer(Modifier.width(4.dp))
                // ⋮ settings menu
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Text("⋮", color = Color(0xFF777777), fontSize = 20.sp)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(Color(0xFF111111))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Settings", color = Color(0xFFCCCCCC), fontSize = 14.sp) },
                            onClick = { menuExpanded = false; onOpenSettings() }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // app grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(apps, key = { it.packageName }) { app ->
                    GridAppItem(
                        app = app,
                        iconSizeDp = iconSize,
                        hasOverride = iconOverrides.containsKey(app.packageName),
                        onClick = { viewModel.launchApp(app.packageName) },
                        onHide = { viewModel.hideApp(app.packageName) },
                        onChangeIcon = { pendingIconPkg = app.packageName; imagePicker.launch(arrayOf("image/*")) },
                        onResetIcon = { viewModel.clearAppIcon(app.packageName) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GridAppItem(
    app: AppInfo,
    iconSizeDp: Int,
    hasOverride: Boolean,
    onClick: () -> Unit,
    onHide: () -> Unit,
    onChangeIcon: () -> Unit,
    onResetIcon: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .combinedClickable(onClick = onClick, onLongClick = { showMenu = true })
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            app.icon?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = app.name,
                    modifier = Modifier.size(iconSizeDp.dp)
                )
            } ?: Box(
                modifier = Modifier
                    .size(iconSizeDp.dp)
                    .background(Color(0xFF222222), RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = app.name,
                color = Color(0xFFDDDDDD),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(Color(0xFF1A1A1A))
        ) {
            DropdownMenuItem(
                text = { Text("Hide", color = Color(0xFFCCCCCC), fontSize = 13.sp) },
                onClick = { showMenu = false; onHide() }
            )
            DropdownMenuItem(
                text = { Text("Change Icon", color = Color(0xFFCCCCCC), fontSize = 13.sp) },
                onClick = { showMenu = false; onChangeIcon() }
            )
            if (hasOverride) {
                DropdownMenuItem(
                    text = { Text("Reset Icon", color = Color(0xFF666666), fontSize = 13.sp) },
                    onClick = { showMenu = false; onResetIcon() }
                )
            }
        }
    }
}
