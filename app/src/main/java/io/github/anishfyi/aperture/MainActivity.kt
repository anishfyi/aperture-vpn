package io.github.anishfyi.aperture

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import io.github.anishfyi.aperture.ui.ApertureViewModel
import io.github.anishfyi.aperture.ui.navigation.ApertureNavHost
import io.github.anishfyi.aperture.ui.theme.ApertureTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ApertureViewModel by viewModels()

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        viewModel.onVpnPermissionResult(result.resultCode == RESULT_OK)
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.setVpnPermissionLauncher { intent ->
            vpnPermissionLauncher.launch(intent)
        }
        setContent {
            ApertureTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ApertureNavHost(viewModel = viewModel)
                }
            }
        }
        requestNotificationPermission()
        viewModel.bootstrap()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
