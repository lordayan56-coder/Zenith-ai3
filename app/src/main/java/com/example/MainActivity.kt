package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ZenithDashboardScreen
import com.example.ui.ZenithViewModel
import com.example.ui.theme.ZenithTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZenithTheme {
                val zenithViewModel: ZenithViewModel = viewModel()
                ZenithDashboardScreen(viewModel = zenithViewModel)
            }
        }
    }
}

