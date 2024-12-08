package com.ewan.triviaapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ewan.triviaapp.R
import com.ewan.triviaapp.adapters.CategoryAdapter
import com.ewan.triviaapp.models.TriviaCategory
import com.ewan.triviaapp.models.TriviaConstants
import com.ewan.triviaapp.models.TriviaResponse
import com.ewan.triviaapp.network.TriviaApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StartActivity : AppCompatActivity() {

    private var selectedCategory: TriviaCategory? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_select)

        // Set up difficulty buttons
        findViewById<Button>(R.id.btnEasy).setOnClickListener { showQuizOptionsDialog("easy") }
        findViewById<Button>(R.id.btnNormal).setOnClickListener { showQuizOptionsDialog("medium") }
        findViewById<Button>(R.id.btnHard).setOnClickListener { showQuizOptionsDialog("hard") }
        findViewById<Button>(R.id.btnHardcore).setOnClickListener { showHardcoreDialog() }
    }

    private fun showQuizOptionsDialog(difficulty: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_quiz_options, null)
        val rgQuestionType = dialogView.findViewById<RadioGroup>(R.id.rgQuestionType)
        val etNumberOfQuestions = dialogView.findViewById<android.widget.EditText>(R.id.etNumberOfQuestions)
        val rvCategories = dialogView.findViewById<RecyclerView>(R.id.rvCategories)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)

        val categories = getCategories()

        // Set up the adapter and RecyclerView
        val adapter = CategoryAdapter(categories) { category ->
            selectedCategory = category
            Log.d("CategorySelection", "Selected category: ${category.name}")
        }
        rvCategories.layoutManager = LinearLayoutManager(this)
        rvCategories.adapter = adapter

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

            if (selectedCategory == null) {
                Log.e("QuizOptions", "No category selected")
                return@setOnClickListener
            }

            val categoryId = TriviaConstants.categoryIdMap[selectedCategory!!.name] ?: 9

            fetchQuestions(numberOfQuestions, categoryId, difficulty, selectedQuestionType)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showHardcoreDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_hardcore_options, null)
        val rvCategories = dialogView.findViewById<RecyclerView>(R.id.rvCategories)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)

        val categories = getCategories()

        val adapter = CategoryAdapter(categories) { category ->
            selectedCategory = category
            Log.d("CategorySelection", "Selected category: ${category.name}")
        }
        rvCategories.layoutManager = LinearLayoutManager(this)
        rvCategories.adapter = adapter

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnConfirm.setOnClickListener {
            if (selectedCategory == null) {
                Log.e("HardcoreOptions", "No category selected")
                return@setOnClickListener
            }

            val categoryId = TriviaConstants.categoryIdMap[selectedCategory!!.name] ?: 9

            fetchQuestions(10, categoryId, "hard", "multiple", hardcoreMode = true)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun fetchQuestions(
        amount: Int,
        category: Int,
        difficulty: String,
        type: String,
        hardcoreMode: Boolean = false
    ) {
        TriviaApiService.api.getQuestions(amount, category, difficulty, type)
            .enqueue(object : Callback<TriviaResponse> {
                override fun onResponse(call: Call<TriviaResponse>, response: Response<TriviaResponse>) {
                    if (response.isSuccessful) {
                        val questions = response.body()?.results ?: emptyList()
                        if (questions.isNotEmpty()) {
                            val intent = Intent(this@StartActivity, TriviaActivity::class.java)
                            intent.putParcelableArrayListExtra("questions", ArrayList(questions))
                            intent.putExtra("difficulty", difficulty)

                            if (hardcoreMode) {
                                intent.putExtra("hardcoreMode", true)
                                intent.putExtra("lives", 1) // Set 1 life for hardcore mode
                            }
                            startActivity(intent)
                        } else {
                            Log.e("TriviaAPI", "No questions retrieved.")
                        }
                    } else {
                        Log.e("TriviaAPI", "Error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<TriviaResponse>, t: Throwable) {
                    Log.e("TriviaAPI", "Failed to fetch questions: ${t.message}")
                }
            })
    }

    private fun getCategories(): List<TriviaCategory> {
        return listOf(
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
    }
}
