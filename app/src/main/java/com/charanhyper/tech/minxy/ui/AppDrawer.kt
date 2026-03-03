package com.charanhyper.tech.minxy.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
    val opacityPct by viewModel.drawerOpacity.collectAsState()

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
            .background(Color.Black.copy(alpha = opacityPct / 100f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // ── drag pill ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount > 50f) onClose()
                        }
                    }
                    .padding(top = 10.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                )
            }

            // ── search bar (pill-shaped, Material You style) ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔍", fontSize = 16.sp, modifier = Modifier.padding(end = 12.dp))

                Box(modifier = Modifier.weight(1f)) {
                    BasicTextField(
                        value = query,
                        onValueChange = viewModel::updateSearch,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        cursorBrush = SolidColor(Color.White.copy(alpha = 0.6f)),
                        decorationBox = { inner ->
                            if (query.isEmpty()) {
                                Text(
                                    "Search apps",
                                    color = Color.White.copy(alpha = 0.35f),
                                    fontSize = 16.sp
                                )
                            }
                            inner()
                        }
                    )
                }

                // ⋮ menu
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("⋮", color = Color.White.copy(alpha = 0.45f), fontSize = 18.sp)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier
                            .background(Color(0xFF1E1E1E), RoundedCornerShape(16.dp))
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text("Settings", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
                            },
                            onClick = { menuExpanded = false; onOpenSettings() }
                        )
                    }
                }
            }

            // ── app grid ──
            AnimatedVisibility(
                visible = apps.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(apps, key = { it.packageName }) { app ->
                        GridAppItem(
                            app = app,
                            iconSizeDp = iconSize,
                            hasOverride = iconOverrides.containsKey(app.packageName),
                            onClick = { viewModel.launchApp(app.packageName) },
                            onHide = { viewModel.hideApp(app.packageName) },
                            onAddToHome = { viewModel.addHomeApp(app.packageName) },
                            onChangeIcon = {
                                pendingIconPkg = app.packageName
                                imagePicker.launch(arrayOf("image/*"))
                            },
                            onResetIcon = { viewModel.clearAppIcon(app.packageName) },
                            modifier = Modifier.animateItem()
                        )
                    }
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
    onAddToHome: () -> Unit,
    onChangeIcon: () -> Unit,
    onResetIcon: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(contentAlignment = Alignment.TopCenter, modifier = modifier) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showMenu = true }
                )
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            app.icon?.let { bmp ->
                Image(
                    bitmap = bmp,
                    contentDescription = app.name,
                    modifier = Modifier
                        .size(iconSizeDp.dp)
                        .clip(CircleShape)
                )
            } ?: Box(
                modifier = Modifier
                    .size(iconSizeDp.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = app.name,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(Color(0xFF1E1E1E), RoundedCornerShape(16.dp))
        ) {
            DropdownMenuItem(
                text = { Text("Hide", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp) },
                onClick = { showMenu = false; onHide() }
            )
            DropdownMenuItem(
                text = { Text("Add to Home", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp) },
                onClick = { showMenu = false; onAddToHome() }
            )
            DropdownMenuItem(
                text = { Text("Change Icon", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp) },
                onClick = { showMenu = false; onChangeIcon() }
            )
            if (hasOverride) {
                DropdownMenuItem(
                    text = { Text("Reset Icon", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp) },
                    onClick = { showMenu = false; onResetIcon() }
                )
            }
        }
    }
}
