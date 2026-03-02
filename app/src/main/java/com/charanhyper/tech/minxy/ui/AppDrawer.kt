package com.charanhyper.tech.minxy.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charanhyper.tech.minxy.AppInfo
import com.charanhyper.tech.minxy.AppViewModel

@Composable
fun AppDrawer(
    viewModel: AppViewModel,
    onClose: () -> Unit
) {
    val apps by viewModel.visibleApps.collectAsState()
    val query by viewModel.searchQuery.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080808))
    ) {
        // drag handle, swipe down to close
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 14.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount > 60f) onClose()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color(0xFF333333), shape = RoundedCornerShape(2.dp))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 12.dp)
            ) {
                BasicTextField(
                    value = query,
                    onValueChange = viewModel::updateSearch,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Light
                    ),
                    cursorBrush = SolidColor(Color.White),
                    decorationBox = { innerTextField ->
                        Column {
                            if (query.isEmpty()) {
                                Text(
                                    "Search apps…",
                                    color = Color(0xFF444444),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Light
                                )
                            }
                            innerTextField()
                            Spacer(Modifier.height(6.dp))
                            HorizontalDivider(color = Color(0xFF2A2A2A), thickness = 1.dp)
                        }
                    }
                )
            }

            Spacer(Modifier.height(4.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(apps, key = { it.packageName }) { app ->
                    AppItem(
                        app = app,
                        onClick = { viewModel.launchApp(app.packageName) },
                        onHide = { viewModel.hideApp(app.packageName) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppItem(
    app: AppInfo,
    onClick: () -> Unit,
    onHide: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        Text(
            text = app.name,
            color = Color(0xFFE0E0E0),
            fontSize = 18.sp,
            fontWeight = FontWeight.Light,
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showMenu = true }
                )
                .padding(vertical = 13.dp)
        )

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(Color(0xFF1C1C1C))
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        "Hide App",
                        color = Color(0xFFCCCCCC),
                        fontSize = 14.sp
                    )
                },
                onClick = {
                    showMenu = false
                    onHide()
                }
            )
        }
    }
}
