package com.ewan.triviaapp.models

data class TriviaCategory(

    val name: String,
    val emoji: String,
    var isSelected: Boolean = false

)

data class TriviaResponse(
    val response_code: Int,
    val results: List<TriviaQuestion>
)

data class TriviaQuestion(
    val category: String,
    val type: String,
    val difficulty: String,
    val question: String,
    val correct_answer: String,
    val incorrect_answers: List<String>
)