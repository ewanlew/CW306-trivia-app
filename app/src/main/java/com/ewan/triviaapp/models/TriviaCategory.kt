package com.ewan.triviaapp.models

data class TriviaCategory(
    val name: String,
    val emoji: String,
    var isSelected: Boolean = false
)


