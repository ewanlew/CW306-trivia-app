package com.ewan.triviaapp.activities

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ewan.triviaapp.R
import com.ewan.triviaapp.models.GameHistory
import com.ewan.triviaapp.models.TriviaQuestion
import com.google.gson.Gson
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.Rotation
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.core.models.Size
import nl.dionsegijn.konfetti.xml.KonfettiView
import org.json.JSONObject
import java.io.File
import java.util.Date
import java.util.concurrent.TimeUnit

class CustomTriviaActivity : AppCompatActivity() {

    private lateinit var txtQuestion: TextView
    private lateinit var txtQuestionNo: TextView
    private lateinit var linLayAnswers: RadioGroup
    private lateinit var btnConfirmAnswer: TextView
    private lateinit var tvTriviaTitle: TextView
    private lateinit var konfettiView: KonfettiView

    private var questions: List<TriviaQuestion> = listOf()
    private var currentQuestionIndex = 0
    private var correctAnswers = 0
    private var username: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_trivia)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        txtQuestion = findViewById(R.id.txtQuestion)
        txtQuestionNo = findViewById(R.id.txtQuestionNo)
        linLayAnswers = findViewById(R.id.linLayAnswers)
        btnConfirmAnswer = findViewById(R.id.btnConfirmAnswer)
        tvTriviaTitle = findViewById(R.id.tvTriviaTitle)
        konfettiView = findViewById(R.id.konfettiView)

        username = intent.getStringExtra("username") ?: return

        val fileUri = intent.getStringExtra("fileUri")
        if (fileUri != null) {
            val fileContent = readJsonFromUri(fileUri)
            if (fileContent != null) {
                parseAndLoadQuestions(fileContent)
            } else {
                Toast.makeText(this, "Failed to read trivia file.", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            Toast.makeText(this, "No file provided.", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnConfirmAnswer.setOnClickListener {
            checkAnswer()
        }
    }

    private fun readJsonFromUri(fileUri: String): String? {
        return try {
            val uri = Uri.parse(fileUri)
            contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() }
        } catch (e: Exception) {
            Log.e("CustomTriviaActivity", "Error reading file: ${e.message}")
            null
        }
    }


    private fun parseAndLoadQuestions(jsonContent: String) {
        try {
            val jsonObject = JSONObject(jsonContent)
            val results = jsonObject.getJSONArray("results")
            questions = Gson().fromJson(results.toString(), Array<TriviaQuestion>::class.java).toList()

            val title = questions.firstOrNull()?.category ?: "Custom Trivia"
            tvTriviaTitle.text = title

            if (questions.isEmpty()) {
                Toast.makeText(this, "No questions available in the file.", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            loadQuestion()
        } catch (e: Exception) {
            Log.e("CustomTriviaActivity", "Error parsing trivia JSON: ${e.message}")
            Toast.makeText(this, "Invalid trivia file.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadQuestion() {
        if (currentQuestionIndex >= questions.size) {
            finishQuiz()
            return
        }

        val question = questions[currentQuestionIndex]
        txtQuestionNo.text = getString(R.string.question_number, currentQuestionIndex + 1)
        txtQuestion.text = question.question

        linLayAnswers.removeAllViews()

        if (question.type == "boolean") {
            val trueOption = RadioButton(this).apply {
                text = "True"
                id = RadioButton.generateViewId()
            }
            val falseOption = RadioButton(this).apply {
                text = "False"
                id = RadioButton.generateViewId()
            }
            linLayAnswers.addView(trueOption)
            linLayAnswers.addView(falseOption)
        } else {
            val allAnswers = question.incorrect_answers.toMutableList().apply {
                add(question.correct_answer)
                shuffle()
            }
            allAnswers.forEach { answer ->
                val radioButton = RadioButton(this).apply {
                    text = answer
                    id = RadioButton.generateViewId()
                }
                linLayAnswers.addView(radioButton)
            }
        }
    }

    private fun showConfetti() {
        konfettiView.start(
            Party(
                angle = 0,
                speed = 10f,
                maxSpeed = 30f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.BLUE),
                shapes = listOf(Shape.Circle, Shape.Square),
                fadeOutEnabled = true,
                delay = 0,
                rotation = Rotation.enabled(),
                emitter = Emitter(duration = 500, TimeUnit.MILLISECONDS).perSecond(50),
                size = listOf(Size.SMALL, Size.MEDIUM, Size.LARGE),
                timeToLive = 2000L,
                position = Position.Relative(0.5, 0.5)
            )
        )
    }

    private fun checkAnswer() {
        val selectedOptionId = linLayAnswers.checkedRadioButtonId
        if (selectedOptionId == -1) {
            Toast.makeText(this, "Please select an answer.", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedOption = findViewById<RadioButton>(selectedOptionId)
        val correctAnswer = questions[currentQuestionIndex].correct_answer

        if (selectedOption.text.toString() == correctAnswer) {
            correctAnswers++
            showConfetti()
        } else {
            Toast.makeText(this, "Incorrect Answer!", Toast.LENGTH_SHORT).show()
        }
        currentQuestionIndex++
        loadQuestion()
    }

    private fun finishQuiz() {
        saveToHistory()
        Toast.makeText(this, "Trivia completed!", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun saveToHistory() {
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        val score = "$correctAnswers/${questions.size}"
        val grade = calculateGrade(correctAnswers, questions.size)
        val title = questions.firstOrNull()?.category ?: "Custom Trivia"

        val newGameHistory = GameHistory(
            score = score,
            grade = grade,
            dateCompleted = Date(System.currentTimeMillis()),
            difficulty = "Custom",
            category = title
        )

        val userProfileJson = sharedPref.getString("$username:gameHistory", "[]")
        val gameHistory = Gson().fromJson(userProfileJson, Array<GameHistory>::class.java).toMutableList()

        gameHistory.add(newGameHistory)
        editor.putString("$username:gameHistory", Gson().toJson(gameHistory))
        editor.apply()
    }

    private fun calculateGrade(correctAnswers: Int, totalQuestions: Int): String {
        val percentage = (correctAnswers.toDouble() / totalQuestions) * 100
        return when {
            percentage >= 90 -> "S"
            percentage >= 80 -> "A"
            percentage >= 70 -> "B"
            percentage >= 60 -> "C"
            else -> "D"
        }
    }
}
