package com.ewan.triviaapp.models

data class UserProfile(
    val username: String,
    var password: String,
    var avatarResId: Int,
    var streak: Int = 0,
    var gems: Int = 0,
    var hardcoreUnlocked: Boolean = false,
    var gameHistory: MutableList<GameHistory> = mutableListOf(),
    var unlockedAvatars: MutableList<Int> = mutableListOf(),
    var pushNotificationsEnabled: Boolean = false,
    var triviaResetTime: String = "12:00 PM"
)
