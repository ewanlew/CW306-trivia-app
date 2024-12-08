package com.ewan.triviaapp.models

import android.os.Parcel
import android.os.Parcelable

data class TriviaQuestion(
    val category: String,
    val type: String,
    val difficulty: String,
    val question: String,
    val correct_answer: String,
    val incorrect_answers: List<String>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(category)
        parcel.writeString(type)
        parcel.writeString(difficulty)
        parcel.writeString(question)
        parcel.writeString(correct_answer)
        parcel.writeStringList(incorrect_answers)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<TriviaQuestion> {
        override fun createFromParcel(parcel: Parcel): TriviaQuestion {
            return TriviaQuestion(parcel)
        }

        override fun newArray(size: Int): Array<TriviaQuestion?> {
            return arrayOfNulls(size)
        }
    }
}
