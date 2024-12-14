package com.ewan.triviaapp.models

object TriviaConstants {
    val categoryIdMap = mapOf(
        "General Knowledge" to 9,
        "Books" to 10,
        "Film" to 11,
        "Music" to 12,
        "Musicals & Theatres" to 13,
        "Television" to 14,
        "Video Games" to 15,
        "Board Games" to 16,
        "Science & Nature" to 17,
        "Computers" to 18,
        "Mathematics" to 19,
        "Mythology" to 20,
        "Sports" to 21,
        "Geography" to 22,
        "History" to 23,
        "Politics" to 24,
        "Art" to 25,
        "Celebrities" to 26,
        "Animals" to 27,
        "Vehicles" to 28,
        "Comics" to 29,
        "Gadgets" to 30,
        "Japanese Anime & Manga" to 31,
        "Cartoon & Animations" to 32
    )

    val idToCategoryMap = categoryIdMap.entries.associateBy({ it.value }, { it.key })

}
