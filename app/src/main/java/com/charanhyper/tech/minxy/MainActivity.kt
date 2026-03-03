package com.charanhyper.tech.minxy

import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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

    private val roleRequestLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* result handled by onResume re-check */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        setContent {
            MinxyTheme {
                val vm: AppViewModel = viewModel()
                var screen by remember { mutableStateOf(Screen.HOME) }

                BackHandler(enabled = screen != Screen.HOME) {
                    screen = Screen.HOME
                }

                when (screen) {
                    Screen.HOME -> HomeScreen(
                        viewModel = vm,
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

    override fun onResume() {
        super.onResume()
        if (!isDefaultLauncher()) {
            requestDefaultLauncherRole()
        }
    }

    private fun isDefaultLauncher(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
        val info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return info?.activityInfo?.packageName == packageName
    }

    private fun requestDefaultLauncherRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_HOME)
            ) {
                roleRequestLauncher.launch(
                    roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                )
            }
        } else {
            // Android 9 and below: open home app picker in settings
            val intent = Intent(android.provider.Settings.ACTION_HOME_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }
    }
}