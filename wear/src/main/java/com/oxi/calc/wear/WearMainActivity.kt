package com.oxi.calc.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.wear.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.oxi.calc.shared.CalculatorViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class WearMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: CalculatorViewModel = viewModel()
            val context = LocalContext.current
            
            // Following system Material You colors on Wear OS
            // Simplified MaterialTheme call for Wear
            MaterialTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    WearCalculatorScreen(viewModel = viewModel)
                }
            }
        }
    }
}
