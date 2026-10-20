package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.TelemetryDashboardScreen
import com.example.ui.TelemetryViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val telemetryViewModel: TelemetryViewModel = viewModel()
            MyApplicationTheme(pureAmoled = true) {
                TelemetryDashboardScreen(viewModel = telemetryViewModel)
            }
        }
    }
}
