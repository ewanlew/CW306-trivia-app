package com.ewan.triviaapp.activities

import com.ewan.triviaapp.network.TriviaApiService
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ewan.triviaapp.adapters.CategoryAdapter
import com.ewan.triviaapp.R
import com.ewan.triviaapp.models.TriviaCategory
import com.ewan.triviaapp.models.TriviaConstants
import com.ewan.triviaapp.models.TriviaResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StartActivity : AppCompatActivity() {

    private var selectedCategory: TriviaCategory? = null // Declare selectedCategory as a class-level variable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_select)

        val username = intent.getStringExtra("username")
        val avatarResId = intent.getIntExtra("avatarResId", R.drawable.avi_default)

        val startButton = findViewById<Button>(R.id.btnEasy)
        startButton.setOnClickListener {
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

        // Set up the adapter and RecyclerView
        val adapter = CategoryAdapter(categories) { category ->
            selectedCategory = category // Update selectedCategory when a category is selected
            Log.d("CategorySelection", "Selected category: ${category.name}")
        }
        rvCategories.layoutManager = LinearLayoutManager(this)
        rvCategories.adapter = adapter

        // Set up the dialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnConfirm.setOnClickListener {
            val selectedQuestionType = when (rgQuestionType.checkedRadioButtonId) {
                R.id.rbTrueFalse -> "boolean"
                R.id.rbMultipleChoice -> "multiple"
                else -> "multiple"
            }
            val numberOfQuestions = etNumberOfQuestions.text.toString().toIntOrNull() ?: 10

            // Check if a category was selected
            if (selectedCategory == null) {
                Log.e("QuizOptions", "No category selected")
                return@setOnClickListener
            }

            // Get the category ID
            val categoryId = TriviaConstants.categoryIdMap[selectedCategory!!.name] ?: 9 // Default to General Knowledge

            // Make the API call
            fetchQuestions(numberOfQuestions, categoryId, "medium", selectedQuestionType)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun fetchQuestions(amount: Int, category: Int, difficulty: String, type: String) {
        TriviaApiService.api.getQuestions(amount, category, difficulty, type)
            .enqueue(object : Callback<TriviaResponse> {
                override fun onResponse(call: Call<TriviaResponse>, response: Response<TriviaResponse>) {
                    if (response.isSuccessful) {
                        val questions = response.body()?.results ?: emptyList()
                        Log.d("TriviaAPI", "Questions Retrieved: $questions")

                        // Navigate to quiz screen with questions (implement navigation logic)
                    } else {
                        Log.e("TriviaAPI", "Error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<TriviaResponse>, t: Throwable) {
                    Log.e("TriviaAPI", "Failed to fetch questions: ${t.message}")
                }
            })
    }
}
