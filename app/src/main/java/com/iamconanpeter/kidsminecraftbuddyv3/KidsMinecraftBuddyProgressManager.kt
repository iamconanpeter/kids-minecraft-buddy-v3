package com.iamconanpeter.kidsminecraftbuddyv3

import android.content.Context

class KidsMinecraftBuddyProgressManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): KidsMinecraftBuddyEngine.ProgressSnapshot {
        return KidsMinecraftBuddyEngine.ProgressSnapshot(
            totalStars = prefs.getInt(KEY_TOTAL_STARS, 0),
            missionsCompleted = prefs.getInt(KEY_MISSIONS_COMPLETED, 0),
            currentMissionIndex = prefs.getInt(KEY_MISSION_INDEX, 0),
            selectedBlockName = prefs.getString(KEY_SELECTED_BLOCK, "GRASS") ?: "GRASS"
        )
    }

    fun save(snapshot: KidsMinecraftBuddyEngine.ProgressSnapshot) {
        prefs.edit()
            .putInt(KEY_TOTAL_STARS, snapshot.totalStars)
            .putInt(KEY_MISSIONS_COMPLETED, snapshot.missionsCompleted)
            .putInt(KEY_MISSION_INDEX, snapshot.currentMissionIndex)
            .putString(KEY_SELECTED_BLOCK, snapshot.selectedBlockName)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "kids_minecraft_buddy_v3"
        private const val KEY_TOTAL_STARS = "total_stars"
        private const val KEY_MISSIONS_COMPLETED = "missions_completed"
        private const val KEY_MISSION_INDEX = "mission_index"
        private const val KEY_SELECTED_BLOCK = "selected_block"
    }
}
