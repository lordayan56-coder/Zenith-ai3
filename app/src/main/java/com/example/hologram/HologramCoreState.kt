package com.example.hologram

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ZenithCyanPrimary
import com.example.ui.theme.ZenithGoldAction
import com.example.ui.theme.ZenithGreenSuccess
import com.example.ui.theme.ZenithRedAlert
import com.example.ui.theme.ZenithVioletSecondary

enum class HologramCoreState(val primaryColor: Color, val secondaryColor: Color, val label: String) {
    IDLE(ZenithCyanPrimary, ZenithVioletSecondary, "IDLE / BREATHING"),
    LISTENING(ZenithCyanPrimary, ZenithGoldAction, "LISTENING / CAPTURING"),
    THINKING(ZenithVioletSecondary, ZenithCyanPrimary, "THINKING / REASONING"),
    SPEAKING(ZenithCyanPrimary, Color(0xFF00FFFF), "SPEAKING / SYNTHESIZING"),
    ACTION(ZenithGoldAction, ZenithCyanPrimary, "EXECUTING ACTION"),
    WARNING(ZenithGoldAction, ZenithRedAlert, "PERMISSION REQUIRED"),
    SUCCESS(ZenithGreenSuccess, ZenithCyanPrimary, "ACTION VERIFIED"),
    ERROR(ZenithRedAlert, Color(0xFFFF5500), "DIAGNOSTIC FAULT")
}
