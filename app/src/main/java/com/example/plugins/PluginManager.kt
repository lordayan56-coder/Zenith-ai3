package com.example.plugins

import kotlinx.coroutines.flow.MutableStateFlow

data class ZenithPlugin(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val isEnabled: Boolean = true,
    val requiresPermission: String? = null
)

class PluginManager {
    val plugins = MutableStateFlow<List<ZenithPlugin>>(
        listOf(
            ZenithPlugin("plugin_weather", "Atmospheric Weather", "Environment", "Live weather, forecasts, and storm alerts."),
            ZenithPlugin("plugin_calendar", "Calendar Sync", "Productivity", "Schedule meetings, read upcoming events.", requiresPermission = "CALENDAR"),
            ZenithPlugin("plugin_reminders", "System Reminders", "Tasks", "Create sticky reminders and status notifications.", requiresPermission = "NOTIFICATIONS"),
            ZenithPlugin("plugin_media", "Media Controller", "Device Control", "Play, pause, skip music and video playback."),
            ZenithPlugin("plugin_smarthome", "Smart Home Core", "IoT Control", "Control lights, security cameras, and HVAC simulation."),
            ZenithPlugin("plugin_research", "Web Knowledge Assistant", "AI Intelligence", "Search live web knowledge and summarize articles."),
            ZenithPlugin("plugin_notes", "Encrypted Notes", "Storage", "Store quick text memos in Room database.")
        )
    )

    fun togglePlugin(id: String) {
        val updated = plugins.value.map {
            if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it
        }
        plugins.value = updated
    }
}
