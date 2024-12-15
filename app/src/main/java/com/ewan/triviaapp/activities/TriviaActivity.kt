package com.ewan.triviaapp.activities

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ewan.triviaapp.R
import com.ewan.triviaapp.models.GameHistory
import com.ewan.triviaapp.models.TriviaConstants
import com.ewan.triviaapp.models.TriviaQuestion
import com.ewan.triviaapp.models.UserProfile
import com.google.gson.Gson
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.Rotation
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.core.models.Size
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.Date
import java.util.concurrent.TimeUnit

class TriviaActivity : AppCompatActivity() {

    private lateinit var txtQuestion: TextView
    private lateinit var txtQuestionNo: TextView
    private lateinit var txtGemsEarned: TextView
    private lateinit var linLayAnswers: RadioGroup
    private lateinit var btnConfirmAnswer: View
    private lateinit var txtLivesCount: TextView
    private lateinit var konfettiView: KonfettiView

    private var questions: List<TriviaQuestion> = listOf()
    private var currentQuestionIndex = 0
    private var lives = 0
    private var gems = 0
    private var gemValue = 0

    private lateinit var username: String
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trivia_questions)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        txtQuestion = findViewById(R.id.txtQuestion)
        txtQuestionNo = findViewById(R.id.txtQuestionNo)
        txtGemsEarned = findViewById(R.id.txtCoinsEarned)
        linLayAnswers = findViewById(R.id.linLayAnswers)
        btnConfirmAnswer = findViewById(R.id.btnConfirmAnswer)
        txtLivesCount = findViewById(R.id.txtLivesCount)
        konfettiView = findViewById(R.id.konfettiView)

        username = intent.getStringExtra("username") ?: return
        questions = intent.getParcelableArrayListExtra<TriviaQuestion>("questions") ?: listOf()
        val difficulty = intent.getStringExtra("difficulty") ?: "easy"
        val isHardcoreMode = intent.getBooleanExtra("hardcoreMode", false)

        gemValue = when (difficulty) {
            "easy" -> 50
            "medium" -> 100
            "hard" -> 150
            "hardcore" -> 200
            else -> 50
        }

        lives = if (isHardcoreMode) {
            1
        } else {
            calculateLives(questions.size)
        }
        updateLivesDisplay()

        txtGemsEarned.text = getString(R.string.gems_earned, 0)

        if (questions.isEmpty()) {
            Toast.makeText(this, "No questions available.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadQuestion()

        btnConfirmAnswer.setOnClickListener {
            checkAnswer()
        }
    }

    /**
     * Calculate the number of lives based on the number of questions
     */
    private fun calculateLives(numQuestions: Int): Int {
        return ((numQuestions / 5) * 2).coerceAtLeast(2) // Lives are 2 per 5 questions, with a minimum of 2
    }

    /**
     * Update the lives display
     */
    private fun updateLivesDisplay() {
        txtLivesCount.text = "x$lives"
    }

    /**
     * Show the confetti
     */
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

    /**
     * Loads the next question
     */
    private fun loadQuestion() {
        if (currentQuestionIndex >= questions.size) {
            finishQuiz()
            return
        }

        val question = questions[currentQuestionIndex]

        txtQuestionNo.text = getString(R.string.question_number, currentQuestionIndex + 1)
        txtQuestion.text = Html.fromHtml(question.question)

        linLayAnswers.removeAllViews()

        if (question.type == "boolean") {
            val trueOption = RadioButton(this).apply {
                text = "True"
                id = View.generateViewId()
            }
            val falseOption = RadioButton(this).apply {
                text = "False"
                id = View.generateViewId()
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
                    text = Html.fromHtml(answer)
                    id = View.generateViewId()
                }
                linLayAnswers.addView(radioButton)
            }
        }
    }

    /**
     * Checks the answer
     */
    private fun checkAnswer() {
        val selectedOptionId = linLayAnswers.checkedRadioButtonId

        if (selectedOptionId == -1) {
            Toast.makeText(this, "Please select an answer.", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedOption = findViewById<RadioButton>(selectedOptionId)
        val correctAnswer = Html.fromHtml(questions[currentQuestionIndex].correct_answer).toString()

        if (selectedOption.text.toString() == correctAnswer) {
            gems += gemValue
            txtGemsEarned.text = getString(R.string.gems_earned, gems)
            showConfetti()
            currentQuestionIndex++
            loadQuestion()
        } else {
            lives--
            selectedOption.setTextColor(Color.RED)
            selectedOption.setTypeface(null, Typeface.BOLD)
            selectedOption.paintFlags = selectedOption.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            updateLivesDisplay()

            if (lives <= 0) {
                finishQuiz()
            }
        }
    }

    /**
     * Finish the quiz
     */
    private fun finishQuiz() {
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        val newProfile = loadUserProfileFromPrefs(username, sharedPref)

        newProfile.gems += gems
        newProfile.streak += 1

        val selectedCategoryName = intent.getIntExtra("selectedCategoryName", 9)
        val newGameHistory = GameHistory(
            score = "$currentQuestionIndex/${questions.size}",
            grade = calculateGrade(currentQuestionIndex, questions.size),
            dateCompleted = Date(System.currentTimeMillis()),
            difficulty = intent.getStringExtra("difficulty") ?: "easy",
            category = TriviaConstants.idToCategoryMap[selectedCategoryName] ?: "Unknown Category"
        )
        newProfile.gameHistory.add(newGameHistory)

        editor.putInt("$username:currentGems", newProfile.gems)
        editor.putInt("$username:currentStreak", newProfile.streak)
        editor.putString("$username:gameHistory", gson.toJson(newProfile.gameHistory))
        editor.apply()

        Toast.makeText(this, "Quiz complete! Total gems earned: $gems", Toast.LENGTH_LONG).show()
        finish()
    }

    /**
     * Calculate the grade based on the number of correct answers
     */
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

    /**
     * Load the user profile from shared preferences
     */
    fun loadUserProfileFromPrefs(username: String, sharedPref: SharedPreferences): UserProfile {
        val defaultGameHistory = mutableListOf<GameHistory>()
        val defaultUnlockedAvatars = mutableListOf<Int>()

        return UserProfile(
            username = username,
            password = sharedPref.getString("$username:password", "") ?: "",
            avatarResId = sharedPref.getInt("$username:avatar", R.drawable.avi_default), // Provide a default avatar
            streak = sharedPref.getInt("$username:currentStreak", 0),
            gems = sharedPref.getInt("$username:currentGems", 0),
            hardcoreUnlocked = sharedPref.getBoolean("$username:hardcoreUnlocked", false),
            gameHistory = sharedPref.getString("$username:gameHistory", "[]")
                ?.let { json ->
                    Gson().fromJson(json, Array<GameHistory>::class.java).toMutableList()
                } ?: defaultGameHistory,
            unlockedAvatars = sharedPref.getString("$username:unlockedAvatars", "[]")
                ?.let { json ->
                    Gson().fromJson(json, Array<Int>::class.java).toMutableList()
                } ?: defaultUnlockedAvatars,
            pushNotificationsEnabled = sharedPref.getBoolean("$username:pushNotificationsEnabled", false),
            triviaResetTime = sharedPref.getString("$username:triviaResetTime", "12:00 PM") ?: "12:00 PM"
        )
    }

}
