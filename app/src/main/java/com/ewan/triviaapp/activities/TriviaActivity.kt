package com.ewan.triviaapp.activities

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ewan.triviaapp.R
import com.ewan.triviaapp.models.TriviaQuestion
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.Rotation
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.core.models.Size
import nl.dionsegijn.konfetti.xml.KonfettiView
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
    private var isHardcoreMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trivia_questions)

        txtQuestion = findViewById(R.id.txtQuestion)
        txtQuestionNo = findViewById(R.id.txtQuestionNo)
        txtGemsEarned = findViewById(R.id.txtCoinsEarned)
        linLayAnswers = findViewById(R.id.linLayAnswers)
        btnConfirmAnswer = findViewById(R.id.btnConfirmAnswer)
        txtLivesCount = findViewById(R.id.txtLivesCount)
        konfettiView = findViewById(R.id.konfettiView)

        // Retrieve data
        questions = intent.getParcelableArrayListExtra<TriviaQuestion>("questions") ?: listOf()
        val difficulty = intent.getStringExtra("difficulty") ?: "easy"
        val isHardcoreMode = intent.getBooleanExtra("hardcoreMode", false) // Get hardcoreMode flag

        gemValue = when (difficulty) {
            "easy" -> 50
            "medium" -> 100
            "hard" -> 150
            "hardcore" -> 200
            else -> 50
        }

        // Calculate lives based on mode
        lives = if (isHardcoreMode) {
            1 // Hardcore mode has exactly 1 life
        } else {
            calculateLives(questions.size) // Normal calculation for other modes
        }
        updateLivesDisplay()

        // Set initial gem count
        txtGemsEarned.text = getString(R.string.gems_earned, 0)

        if (questions.isEmpty()) {
            Toast.makeText(this, "No questions available.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load the first question
        loadQuestion()

        btnConfirmAnswer.setOnClickListener {
            checkAnswer()
        }
    }


    private fun calculateLives(numQuestions: Int): Int {
        return ((numQuestions / 5) * 2).coerceAtLeast(2) // Lives are 2 per 5 questions, with a minimum of 2
    }




    private fun updateLivesDisplay() {
        txtLivesCount.text = "x$lives"
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

    private fun loadQuestion() {
        if (currentQuestionIndex >= questions.size) {
            Toast.makeText(this, "Quiz complete! Total gems: $gems", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val question = questions[currentQuestionIndex]

        // Display question and number
        txtQuestionNo.text = getString(R.string.question_number, currentQuestionIndex + 1)
        txtQuestion.text = Html.fromHtml(question.question)

        // Clear previous answers
        linLayAnswers.removeAllViews()

        // Dynamically load answers
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
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
            showConfetti()
            currentQuestionIndex++
            loadQuestion()
        } else {
            lives--
            selectedOption.setTextColor(Color.RED)
            selectedOption.setTypeface(null, Typeface.BOLD)
            selectedOption.paintFlags = selectedOption.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

            updateLivesDisplay()

            Toast.makeText(this, "Incorrect! Try again.", Toast.LENGTH_SHORT).show()

            if (lives <= 0) {
                Toast.makeText(this, "Game Over!", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}
