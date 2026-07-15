package io.github.anishfyi.aperture

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
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
        viewModel.bootstrap()
    }
}
