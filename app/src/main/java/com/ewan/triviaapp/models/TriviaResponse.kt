package com.ewan.triviaapp.models

data class TriviaResponse(
    val response_code: Int,
    val results: List<TriviaQuestion>
)
