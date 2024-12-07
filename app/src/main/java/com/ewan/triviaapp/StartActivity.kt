package com.ewan.triviaapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_select)

        val username = intent.getStringExtra("username")
        val avatarResId = intent.getIntExtra("avatarResId", R.drawable.avi_default)

        val startButton = findViewById<Button>(R.id.btnEasy)
        startButton.setOnClickListener(){
            showQuizOptionsDialog()
        }
        }

    private fun showQuizOptionsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_quiz_options, null)
        val rgQuestionType = dialogView.findViewById<RadioGroup>(R.id.rgQuestionType)
        val etNumberOfQuestions = dialogView.findViewById<EditText>(R.id.etNumberOfQuestions)
        val rvCategories = dialogView.findViewById<RecyclerView>(R.id.rvCategories)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)

        val categories = listOf(
            TriviaCategory("General Knowledge", "\uD83C\uDF10"),
            TriviaCategory("Books", "\uD83D\uDCDA"),
            TriviaCategory("Film", "\uD83C\uDFAC"),
            TriviaCategory("Music", "\uD83C\uDFB5"),
            TriviaCategory("Musicals & Theatres", "\uD83C\uDFAD"),
            TriviaCategory("Television", "\uD83D\uDCFA"),
            TriviaCategory("Video Games", "\uD83C\uDFAE"),
            TriviaCategory("Board Games", "\uD83C\uDFB2"),
            TriviaCategory("Science & Nature", "\uD83D\uDD2C"),
            TriviaCategory("Computers", "\uD83D\uDCBB"),
            TriviaCategory("Mathematics", "\uD83D\uDCCA"),
            TriviaCategory("Mythology", "\uD83C\uDF07"),
            TriviaCategory("Sports", "\u26BD"),
            TriviaCategory("Geography", "\uD83C\uDF0D"),
            TriviaCategory("History", "\uD83D\uDD16"),
            TriviaCategory("Politics", "\uD83D\uDCDA"),
            TriviaCategory("Art", "\uD83C\uDFA8"),
            TriviaCategory("Celebrities", "\uD83C\uDF1F"),
            TriviaCategory("Animals", "\uD83D\uDC3E"),
            TriviaCategory("Vehicles", "\uD83D\uDE95"),
            TriviaCategory("Comics", "\uD83C\uDF08"),
            TriviaCategory("Gadgets", "\uD83D\uDD27"),
            TriviaCategory("Japanese Anime & Manga", "\uD83D\uDC09"),
            TriviaCategory("Cartoon & Animations", "\uD83C\uDF83")
        )

        var selectedCategory: TriviaCategory? = null

        val adapter = CategoryAdapter(categories) { category ->
        }
        rvCategories.layoutManager = LinearLayoutManager(this)
        rvCategories.adapter = adapter

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnConfirm.setOnClickListener {
            val selectedQuestionType = when (rgQuestionType.checkedRadioButtonId) {
                R.id.rbTrueFalse -> "True/False"
                R.id.rbMultipleChoice -> "Multiple Choice"
                else -> "Unknown"
            }
            val numberOfQuestions = etNumberOfQuestions.text.toString().toIntOrNull() ?: 0

            Log.d("QuizOptions", "Type: $selectedQuestionType")
            Log.d("QuizOptions", "Questions: $numberOfQuestions")
            Log.d("QuizOptions", "Category: ${selectedCategory?.name ?: "None"}")

            dialog.dismiss()
        }

        dialog.show()
    }

}