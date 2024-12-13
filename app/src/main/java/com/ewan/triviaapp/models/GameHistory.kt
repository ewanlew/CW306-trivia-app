package com.ewan.triviaapp.models

import java.util.Date

data class GameHistory(
    val score: String,
    val grade: String,
    val dateCompleted: Date,
    val difficulty: String,
    val category: String
)
