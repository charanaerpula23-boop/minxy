package com.charanhyper.tech.minxy

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.charanhyper.tech.minxy.ui.AppDrawer
import com.charanhyper.tech.minxy.ui.HiddenAppsScreen
import com.charanhyper.tech.minxy.ui.HomeScreen
import com.charanhyper.tech.minxy.ui.SettingsScreen
import com.charanhyper.tech.minxy.ui.theme.MinxyTheme

enum class Screen { HOME, DRAWER, HIDDEN_APPS, SETTINGS }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        setContent {
            MinxyTheme {
                val vm: AppViewModel = viewModel()
                var screen by remember { mutableStateOf(Screen.HOME) }

                BackHandler(enabled = screen != Screen.HOME) {
                    screen = Screen.HOME
                }

                when (screen) {
                    Screen.HOME -> HomeScreen(
                        onOpenDrawer = { screen = Screen.DRAWER },
                        onOpenHiddenApps = { screen = Screen.HIDDEN_APPS }
                    )
                    Screen.DRAWER -> AppDrawer(
                        viewModel = vm,
                        onOpenSettings = { screen = Screen.SETTINGS },
                        onClose = { screen = Screen.HOME }
                    )
                    Screen.HIDDEN_APPS -> HiddenAppsScreen(
                        viewModel = vm,
                        onClose = { screen = Screen.HOME }
                    )
                    Screen.SETTINGS -> SettingsScreen(
                        viewModel = vm,
                        onClose = { screen = Screen.HOME }
                    )
                }
            }
        }
    }
}